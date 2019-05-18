package edu.scu.dubbos.registry;

import edu.scu.dubbos.common.Constants;
import edu.scu.dubbos.common.URL;
import edu.scu.dubbos.rpc.RpcException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryOneTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 15:43
 */
public class ZookeeperRegistry {

    private static final Logger logger = LogManager.getLogger(ZookeeperRegistry.class);
    private URL url;
    private final CuratorFramework client;
    private static List<CuratorFramework> createdClients=new ArrayList<>();
    private ConnectionState connectionState = ConnectionState.LOST;

    public ZookeeperRegistry(URL url) {
        this.url = url;
        try {
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                    .connectString(url.getParameter(Constants.REGISTRY_KEY))
                    .retryPolicy(new RetryOneTime(1000))
                    .connectionTimeoutMs(5000);
            client = builder.build();
            client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    connectionState = newState;
                    logger.info("Zookeeper connect state changed to " + newState);
                }
            });
            client.start();
            createdClients.add(client);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void register(URL url) {
        try {
            createEphemeral(toRegisterUrlPath(url));
        } catch (Throwable e) {
            throw new RpcException("Failed to register " + url.getParameter(Constants.REGISTRY_KEY)
                    + " to zookeeper " + url + ", cause: " + e.getMessage(), e);
        }
    }

    public List<String> subscribe(URL url, ChildListener listener) {
        String path = toSubscribeUrlPath(url);
        createEphemeral(path);
        try {
            return client.getChildren().usingWatcher(new CuratorWatcherImpl(listener)).forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private String toRegisterUrlPath(URL url) {
        StringBuffer path = new StringBuffer();
        path.append(getRootPath(url));
        path.append("/");
        path.append(url.removeParameter(Constants.REGISTRY_KEY).toZkString());
        return path.toString();
    }

    private String toSubscribeUrlPath(URL url) {
        StringBuffer path = new StringBuffer();
        path.append(getRootPath(url));
        return path.toString();
    }

    private String getRootPath(URL url) {
        StringBuffer path = new StringBuffer("/dubbos/");
        path.append(url.getParameter(Constants.APPLICATION_KEY));
        path.append("/");
        path.append(url.getParameter(Constants.INTERFACE_KEY));
        return path.toString();
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public void createPersistent(String path) {
        try {
            client.create().creatingParentsIfNeeded().forPath(path);
        } catch (KeeperException.NodeExistsException e) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void createEphemeral(String path) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (KeeperException.NodeExistsException e) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void delete(String path) {
        try {
            client.delete().forPath(path);
        } catch (KeeperException.NoNodeException e) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public boolean checkExists(String path) {
        try {
            if (client.checkExists().forPath(path) != null) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean isConnected() {
        return client.getZookeeperClient().isConnected();
    }

    public void doClose() {
        client.close();
    }

    public CuratorWatcher createTargetChildListener(String path, ChildListener listener) {
        return new CuratorWatcherImpl(listener);
    }

    public List<String> addTargetChildListener(String path, CuratorWatcher listener) {
        try {
            return client.getChildren().usingWatcher(listener).forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void removeTargetChildListener(String path, CuratorWatcher listener) {
        ((CuratorWatcherImpl) listener).unwatch();
    }

    private class CuratorWatcherImpl implements CuratorWatcher {

        private volatile ChildListener listener;

        public CuratorWatcherImpl(ChildListener listener) {
            this.listener = listener;
        }

        public void unwatch() {
            this.listener = null;
        }

        @Override
        public void process(WatchedEvent event) throws Exception {
            if (listener != null) {
                String path = event.getPath() == null ? "" : event.getPath();
                listener.childChanged(path,
                        // if path is null, curator using watcher will throw NullPointerException.
                        // if client connect or disconnect to server, zookeeper will queue
                        // watched event(Watcher.Event.EventType.None, .., path = null).
                        path != null && path.length() > 0
                                ? client.getChildren().usingWatcher(this).forPath(path)
                                : Collections.<String>emptyList());
            }
        }
    }
    
    public static void destroyClients(){
        for (CuratorFramework client:createdClients) {
            logger.info("Closing zookeeper client:["+client+"]");
            client.close();
        }
    }
}
