package com.payneteasy.libreofficewrapper.server.config;

import com.payneteasy.startup.parameters.AStartupParameter;

public interface IJettyConfiguration {

    String SERVER_PORT = "SERVER_PORT";
    String SERVER_SERVLET_PATH = "SERVER_SERVLET_PATH";
    String SERVER_SERVLET_CONTEXT = "SERVER_SERVLET_CONTEXT";


    String DEFAULT_SERVER_PORT = "8080";
    String DEFAULT_SERVLET_PATH = "/convert";
    String DEFAULT_SERVLET_CONTEXT = "/converter";

    @AStartupParameter(name = SERVER_PORT, value = DEFAULT_SERVER_PORT)
    int getPort();

    @AStartupParameter(name = SERVER_SERVLET_PATH, value = DEFAULT_SERVLET_PATH)
    String getServletPath();

    @AStartupParameter(name = SERVER_SERVLET_CONTEXT, value = DEFAULT_SERVLET_CONTEXT)
    String getServletContext();
}
