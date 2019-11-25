package com.payneteasy.libreofficewrapper.server.config;

import com.payneteasy.startup.parameters.AStartupParameter;

public interface ILibreofficeServiceConfiguration {

    String LIBREOFFICE_HOST = "LIBREOFFICE_HOST";
    String LIBREOFFICE_PORT = "LIBREOFFICE_PORT";

    String DEFAULT_LIBREOFFICE_HOST = "localhost";
    String DEFAULT_LIBREOFFICE_PORT = "5888";

    @AStartupParameter(name = LIBREOFFICE_HOST, value = DEFAULT_LIBREOFFICE_HOST)
    String getLibreofficeHost();

    @AStartupParameter(name = LIBREOFFICE_PORT, value = DEFAULT_LIBREOFFICE_PORT)
    int getLibreofficePort();
}
