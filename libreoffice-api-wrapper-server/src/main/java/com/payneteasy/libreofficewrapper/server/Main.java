package com.payneteasy.libreofficewrapper.server;

import com.payneteasy.libreofficewrapper.server.config.IJettyConfiguration;
import com.payneteasy.libreofficewrapper.server.servlet.LibreofficeConverterServlet;
import com.payneteasy.libreofficewrapper.server.servlet.SvgToPngConverterServlet;
import com.payneteasy.libreofficewrapper.server.servlet.VersionServlet;
import com.payneteasy.startup.parameters.StartupParametersFactory;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
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
        final IJettyConfiguration jettyConfiguration = StartupParametersFactory.getStartupParameters(IJettyConfiguration.class);

        LOGGER.info("Starting Jetty server at port {}", jettyConfiguration.getPort());
        final Server server = new Server(jettyConfiguration.getPort());

        final ServletContextHandler context = new ServletContextHandler();
        context.setContextPath(jettyConfiguration.getServletContext());

        server.setHandler(context);

        LOGGER.info("Adding servlet mapping to servlet path {}", jettyConfiguration.getServletPath());
        context.addServlet(LibreofficeConverterServlet.class, jettyConfiguration.getServletPath())
            .setInitOrder(1);
        context.addServlet(SvgToPngConverterServlet.class, "/convert/svg-to-png")
            .setInitOrder(1);
        context.addServlet(VersionServlet.class, "/management/version.txt")
            .setInitOrder(1);

        return server;
    }
}
