package com.payneteasy.libreofficewrapper.server.config;

public class JettyConfiguration {

    public static final String SERVER_PORT = "SERVER_PORT";
    public static final String SERVER_SERVLET_PATH = "SERVER_SERVLET_PATH";

    private static final int DEFAULT_SERVER_PORT = 8080;
    private static final String DEFAULT_SERVLET_PATH = "/convert";

    private static int port = -1;
    private static String servletPath;

    private JettyConfiguration() {
    }

    public static int getPort() {
        if (port == -1) {
            final String portAsString = System.getenv(SERVER_PORT);
            if (portAsString == null || portAsString.trim().isEmpty()) {
                port = DEFAULT_SERVER_PORT;
            } else {
                port = Integer.parseInt(portAsString);
            }
        }
        return port;
    }

    public static String getServletPath() {
        if (servletPath == null) {
            servletPath = System.getenv(SERVER_SERVLET_PATH);
            if (servletPath == null || servletPath.trim().isEmpty()) {
                servletPath = DEFAULT_SERVLET_PATH;
            }
        }
        return servletPath;
    }
}
