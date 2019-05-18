package edu.scu.dubbos.utils;

import edu.scu.dubbos.common.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @Author: 杨斌
 * @Date: 2019/4/16 12:33
 */
public class NetUtils {
    private static final Logger logger = LogManager.getLogger(NetUtils.class);
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
    public static final String LOCALHOST = "127.0.0.1";
    public static final String ANYHOST = "0.0.0.0";
    private static volatile InetAddress LOCAL_ADDRESS = null;

    public static boolean isInvalidLocalHost(String host) {
        return host == null
                || host.length() == 0
                || host.equalsIgnoreCase(Constants.LOCALHOST_KEY)
                || host.equals(Constants.ANYHOST_VALUE)
                || (LOCAL_IP_PATTERN.matcher(host).matches());
    }

    public static String getLocalHost() {
        InetAddress address = getLocalAddress();
        return address == null ? LOCALHOST : address.getHostAddress();
    }

    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }
        InetAddress localAddress = getLocalAddress0();
        LOCAL_ADDRESS = localAddress;
        return localAddress;
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
            logger.warn(e);
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable e) {
                                    logger.warn(e);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        logger.warn(e);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn(e);
        }
        return localAddress;
    }

    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress())
            return false;
        String name = address.getHostAddress();
        return (name != null
                && !ANYHOST.equals(name)
                && !LOCALHOST.equals(name)
                && IP_PATTERN.matcher(name).matches());
    }

}
