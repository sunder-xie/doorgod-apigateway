package com.ymatou.doorgod.apigateway.utils;

import com.ymatou.doorgod.apigateway.reverseproxy.SystemMetricsController;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by tuwenjie on 2016/9/7.
 */
public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static volatile String localIp;

    private Utils() {};

    /**
     * 获取来源IP
     * @return
     */
    public static String getOriginalIp(HttpServerRequest req ) {
        String result = req.headers().get("Cdn-Src-Ip");
        if ( result != null && result.trim().length() > 0 ) {
            return result.trim();
        }
        result = req.headers().get("X-Forwarded-For");
        if ( result == null || result.trim().length() == 0) {
            return req.remoteAddress().host();
        } else {
            Set<String> ips = splitByComma(result);
            return ips.iterator().next();
        }
    }

    public static Set<String> splitByComma( String text ) {
        Set<String> result = new HashSet<String>( );
        if (StringUtils.hasText(text)) {
            text = text.trim();
            String[] splits = text.split(",");
            for (String split : splits ) {
                if (StringUtils.hasText(split.trim())) {
                    result.add(split.trim());
                }
            }
        }
        return result;
    }

    public static String getCurrentTimeStr( ) {
        LocalDateTime dateTime = LocalDateTime.now();
        return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static Date parseDate( String date ) {
        LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String localIp( ) {
        if (localIp != null) {
            return localIp;
        }
        synchronized (Utils.class) {
            if (localIp == null) {
                try {
                    Enumeration<NetworkInterface> netInterfaces = NetworkInterface
                            .getNetworkInterfaces();

                    while (netInterfaces.hasMoreElements() && localIp == null) {
                        NetworkInterface ni = netInterfaces.nextElement();
                        if (!ni.isLoopback() && ni.isUp() && !ni.isVirtual()) {
                            Enumeration<InetAddress> address = ni.getInetAddresses();

                            while (address.hasMoreElements() && localIp == null) {
                                InetAddress addr = address.nextElement();

                                if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()
                                        && !(addr.getHostAddress().indexOf(":") > -1)) {
                                    localIp = addr.getHostAddress();

                                }
                            }
                        }
                    }

                } catch (Throwable t) {
                    localIp = "127.0.0.1";
                    LOGGER.error("Failed to extract local ip. use 127.0.0.1 instead. {}", t.getMessage(), t);
                }
            }

            if (localIp == null ) {
                localIp = "127.0.0.1";
                LOGGER.error("Failed to extract local ip. use 127.0.0.1 instead");
            }

            return localIp;
        }
    }

    private static String buildFullDoorGodHeaderName( String name ) {
        return Constants.HEADER_DOOR_GOD_PREFIX + name;
    }

    public static void addDoorGodHeader( HttpServerRequest req, String headerWithoutPrefix, String value) {
        String header = buildFullDoorGodHeaderName(headerWithoutPrefix);
        req.headers().add(header, value);
    }

    public static void addDoorGodHeader( HttpServerRequest req, String headerWithoutPrefix, List<String> values) {
        String header = buildFullDoorGodHeaderName(headerWithoutPrefix);
        req.headers().add(header, values);
    }

    public static String getDoorGodHeader(HttpServerRequest req, String headerWithoutPrefix ) {
        String header = buildFullDoorGodHeaderName(headerWithoutPrefix);
        return req.headers().get(header);
    }

    public static List<String> getDoorGodHeaderAll(HttpServerRequest req, String headerWithoutPrefix ) {
        String header = buildFullDoorGodHeaderName(headerWithoutPrefix);
        return req.headers().getAll(header);
    }

    public static boolean containDoorGodHeader( HttpServerRequest req, String headerWithoutPrefix) {
        return req.headers().contains(buildFullDoorGodHeaderName(headerWithoutPrefix));
    }

    public static String buildFullUri( HttpServerRequest req ) {
        return req.host() + req.uri();
    }

    public static String buildFullPath( HttpServerRequest req ) {
        return req.host() + req.path();
    }

    public static String readVersion( ) {
        try {
            return new String(Files.readAllBytes(
                    Paths.get(Utils.class.getResource("/version.txt").toURI())), Charset.forName("UTF-8"));
        } catch (Exception e) {
            LOGGER.error("Failed to read version. {}", e.getMessage(), e);
            return "Failed to read version:" + e.getMessage();
        }
    }
}
