package com.payneteasy.libreofficewrapper.server.config;

public class LibreofficeServiceConfiguration {

    public static final String LIBREOFFICE_HOST = "LIBREOFFICE_HOST";
    public static final String LIBREOFFICE_PORT = "LIBREOFFICE_PORT";

    private static final String DEFAULT_LIBREOFFICE_HOST = "localhost";
    private static final int DEFAULT_LIBREOFFICE_PORT = 8100;

    private static String libreofficeHost;
    private static int libreofficePort = -1;

    private LibreofficeServiceConfiguration() {
    }

    public static String getLibreofficeHost() {
        if (libreofficeHost == null) {
            libreofficeHost = System.getenv(LIBREOFFICE_HOST);
            if (libreofficeHost == null || libreofficeHost.trim().isEmpty()) {
                libreofficeHost = DEFAULT_LIBREOFFICE_HOST;
            }
        }
        return libreofficeHost;
    }

    public static int getLibreofficePort() {
        if (libreofficePort == -1) {
            final String strPort = System.getenv(LIBREOFFICE_PORT);
            if (strPort == null || strPort.trim().isEmpty()) {
                libreofficePort = DEFAULT_LIBREOFFICE_PORT;
            } else {
                libreofficePort = Integer.parseInt(strPort);
            }
        }
        return libreofficePort;
    }
}
