package com.payneteasy.libreofficewrapper.server;

import com.payneteasy.libreofficewrapper.server.config.JettyConfiguration;
import com.payneteasy.libreofficewrapper.server.servlet.LibreofficeConverterServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) throws Exception {
        final Server server = createServer();
        server.start();
        LOGGER.info("Server started.");
        server.join();
        LOGGER.info("Server stopped.");
    }

    private static Server createServer() {
        LOGGER.info("Starting Jetty server at port {}", JettyConfiguration.getPort());
        final Server server = new Server(JettyConfiguration.getPort());

        final ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);

        LOGGER.info("Adding servlet mapping to servlet path {}", JettyConfiguration.getServletPath());
        servletHandler.addServletWithMapping(LibreofficeConverterServlet.class, JettyConfiguration.getServletPath())
            .setInitOrder(1);

        return server;
    }
}
