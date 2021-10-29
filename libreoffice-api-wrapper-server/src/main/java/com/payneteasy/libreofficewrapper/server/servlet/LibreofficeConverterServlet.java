package com.payneteasy.libreofficewrapper.server.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFamily;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;
import com.payneteasy.libreofficewrapper.server.config.ILibreofficeServiceConfiguration;
import com.payneteasy.startup.parameters.StartupParametersFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S1989")
public class LibreofficeConverterServlet extends HttpServlet {

    private static final String TEXT_PLAIN_CONTENT_TYPE = "text/plain;charset=UTF-8";

    private final Logger logger = LoggerFactory.getLogger(LibreofficeConverterServlet.class);

    private final DefaultDocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();

    private final Set<String> availableDocumentFormats = new HashSet<>();
    private final Set<String> availableOutputFormats = new HashSet<>();

    private final ILibreofficeServiceConfiguration libreofficeServiceConfiguration =
        StartupParametersFactory.getStartupParameters(ILibreofficeServiceConfiguration.class);

    public LibreofficeConverterServlet() {
        formatRegistry.addDocumentFormat(
            new DocumentFormat(
                "DOCX",
                DocumentFamily.TEXT,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "docx"
            )
        );

        formatRegistry.addDocumentFormat(
            new DocumentFormat(
                "XLSX",
                DocumentFamily.SPREADSHEET,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "xlsx"
            )
        );

        availableDocumentFormats.add("csv");
        availableDocumentFormats.add("doc");
        availableDocumentFormats.add("docx");
        availableDocumentFormats.add("xls");
        availableDocumentFormats.add("xlsx");

        availableOutputFormats.add("pdf");

        logger.info(
            "Libreoffice server configuration: host='{}', port={}\n" +
                "Available document convert formats: {} -> {}",
            libreofficeServiceConfiguration.getLibreofficeHost(),
            libreofficeServiceConfiguration.getLibreofficePort(),
            availableDocumentFormats,
            availableOutputFormats
        );
    }

    @Override
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        logger.info("Received POST request to {}", request.getServletPath());

        final String inputFormat = request.getParameter("inputFormat");
        final String outputFormat = request.getParameter("outputFormat");

        if (inputFormat == null || !availableDocumentFormats.contains(inputFormat)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(TEXT_PLAIN_CONTENT_TYPE);
            response
                .getWriter()
                .printf(
                    "Invalid input document format %s, expected one of %s",
                    inputFormat,
                    availableDocumentFormats
                );
            return;
        }

        if (outputFormat == null || !availableOutputFormats.contains(outputFormat)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(TEXT_PLAIN_CONTENT_TYPE);
            response
                .getWriter()
                .printf(
                    "Invalid output document format %s, expected one of %s",
                    outputFormat,
                    availableOutputFormats
                );
            return;
        }

        logger.info("Converting request input from {} to {}", inputFormat, outputFormat);

        response.setContentType(
            formatRegistry
                .getFormatByFileExtension(outputFormat)
                .getMimeType()
        );

        OpenOfficeConnection connection = null;
        try {
            connection = new SocketOpenOfficeConnection(
                libreofficeServiceConfiguration.getLibreofficeHost(),
                libreofficeServiceConfiguration.getLibreofficePort()
            );

            connection.connect();

            final DocumentConverter converter = new StreamOpenOfficeDocumentConverter(
                connection,
                formatRegistry
            );

            converter.convert(
                request.getInputStream(),
                formatRegistry.getFormatByFileExtension(inputFormat),
                response.getOutputStream(),
                formatRegistry.getFormatByFileExtension(outputFormat)
            );

            logger.info("Converted document from {} to {}", inputFormat, outputFormat);
        } catch (Exception e) {
            logger.error("Cannot convert file to {} format", outputFormat, e);

            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(TEXT_PLAIN_CONTENT_TYPE);
            response
                .getWriter()
                .printf(
                    "Unexpected error while converting document from %s to %s: %s",
                    inputFormat,
                    outputFormat,
                    e.getMessage()
                );
        } finally {
            if (connection != null && connection.isConnected()) {
                connection.disconnect();
            }
        }
    }
}
