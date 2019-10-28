package com.payneteasy.libreofficewrapper.server.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibreofficeConverterServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(LibreofficeConverterServlet.class);

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final Map<String, String[]> requestParams = req.getParameterMap();
        logger.info("Received POST request to {}", req.getServletPath());
        requestParams.forEach((name, values) -> logger.info("Request parameter: {}={}", name, values == null ? "<null>" : Arrays.asList(values)));

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/plain");
        resp.getWriter().print("OK");
    }
}
