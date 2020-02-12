package com.payneteasy.libreofficewrapper.server.servlet;

import com.payneteasy.libreofficewrapper.server.Main;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VersionServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest aRequest, final HttpServletResponse aResponse) throws IOException {
        try (final InputStream resources = Main.class.getResourceAsStream("/META-INF/maven/com.payneteasy/libreoffice-api-wrapper-server/pom.properties")) {
            if (resources != null) {
                final Properties properties = new Properties();
                properties.load(resources);
                aResponse.getWriter().print(properties.getProperty("version"));
            } else {
                aResponse.sendError(500);
            }
        }
    }
}
