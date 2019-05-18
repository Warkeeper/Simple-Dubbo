package edu.scu.dubbos.config;

import edu.scu.dubbos.registry.ZookeeperRegistry;
import edu.scu.dubbos.remoting.exchange.HeaderExchangeClient;
import edu.scu.dubbos.rpc.protocol.DubboProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: 杨斌
 * @Date: 2019/5/6 10:30
 */
public class DubbosShutdownHook extends Thread {
    private static final Logger logger = LogManager.getLogger(DubbosShutdownHook.class);
    private static final DubbosShutdownHook dubbosShutdownHook = new DubbosShutdownHook("DubbosShutdownHook");
    private final AtomicBoolean added = new AtomicBoolean(false);

    public static DubbosShutdownHook getDubbosShutdownHook() {
        return dubbosShutdownHook;
    }

    private DubbosShutdownHook(String threadName) {
        super(threadName);
    }

    public void register() {
        if(!added.compareAndSet(false,true)){
            return;
        }
        logger.info("Adding shutting down hook");
        Runtime.getRuntime().addShutdownHook(getDubbosShutdownHook());
    }

    @Override
    public void run() {
        logger.info("Shutting down dubbos framework gracefully");
        ZookeeperRegistry.destroyClients();
        HeaderExchangeClient.destroyAll();
        DubboProtocol.getInstance().destroy();
    }
}
