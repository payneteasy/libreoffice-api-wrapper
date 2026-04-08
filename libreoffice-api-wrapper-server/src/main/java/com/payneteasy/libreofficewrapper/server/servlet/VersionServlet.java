package com.payneteasy.libreofficewrapper.server.servlet;

import com.payneteasy.libreofficewrapper.server.Main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class VersionServlet extends HttpServlet {

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        try (
            final InputStream is = Main.class.getResourceAsStream(
                "/META-INF/maven/com.payneteasy/libreoffice-api-wrapper-server/pom.properties"
            )
        ) {
            if (is != null) {
                final Properties properties = new Properties();
                properties.load(is);
                response.getWriter().print(properties.getProperty("version"));
            } else {
                response.sendError(500);
            }
        }
    }
}
