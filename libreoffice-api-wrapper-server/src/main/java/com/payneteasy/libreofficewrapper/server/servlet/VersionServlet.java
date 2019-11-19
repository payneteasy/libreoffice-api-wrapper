package com.payneteasy.libreofficewrapper.server.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VersionServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest aRequest, final HttpServletResponse aResponse) throws IOException {
        aResponse.getWriter().print(VersionServlet.class.getPackage().getImplementationVersion());
    }
}
