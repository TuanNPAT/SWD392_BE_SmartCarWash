package com.swd392.smartcarwash.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";

    private IpUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getClientIpAddress() {
        ServletRequestAttributes attr =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attr == null) {
            return UNKNOWN;
        }

        return getClientIpAddress(attr.getRequest());
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = getHeaderIp(request, "X-Forwarded-For");
        if (isValidIp(ip)) return ip;

        ip = getHeaderIp(request, "X-Real-IP");
        if (isValidIp(ip)) return ip;

        ip = getHeaderIp(request, "Proxy-Client-IP");
        if (isValidIp(ip)) return ip;

        ip = getHeaderIp(request, "WL-Proxy-Client-IP");
        if (isValidIp(ip)) return ip;

        String remoteAddr = request.getRemoteAddr();

        if (LOCALHOST_IPV6.equals(remoteAddr)) {
            return LOCALHOST_IPV4;
        }

        return remoteAddr != null ? remoteAddr : UNKNOWN;
    }

    public static String getUserAgent() {
        ServletRequestAttributes attr =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attr == null) {
            return UNKNOWN;
        }

        return getUserAgent(attr.getRequest());
    }

    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String userAgent = request.getHeader("User-Agent");
        return isValidIp(userAgent) ? userAgent : UNKNOWN;
    }

    private static String getHeaderIp(HttpServletRequest request, String header) {
        String ip = request.getHeader(header);

        if (ip == null || ip.isBlank()) {
            return null;
        }

        String firstIp = ip.split(",")[0].trim();

        return UNKNOWN.equalsIgnoreCase(firstIp) ? null : firstIp;
    }

    private static boolean isValidIp(String value) {
        return value != null && !value.isBlank() && !UNKNOWN.equalsIgnoreCase(value);
    }
}