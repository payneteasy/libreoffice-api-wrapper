package com.payneteasy.libreofficewrapper.server.servlet;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFamily;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;
import com.payneteasy.libreofficewrapper.server.config.ILibreofficeServiceConfiguration;
import com.payneteasy.startup.parameters.StartupParametersFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibreofficeConverterServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(LibreofficeConverterServlet.class);

    private final DefaultDocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();

    private final Set<String> availableDocumentFormats = new HashSet<>();
    private final Set<String> availableOutputFormats = new HashSet<>();

    private final ILibreofficeServiceConfiguration libreofficeServiceConfiguration;

    public LibreofficeConverterServlet() {
        libreofficeServiceConfiguration = StartupParametersFactory.getStartupParameters(ILibreofficeServiceConfiguration.class);

        // docx and xlsx formats are not in DefaultFormatRegistry, add manually
        final DocumentFormat docxFormat = new DocumentFormat("DOCX", DocumentFamily.TEXT, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        formatRegistry.addDocumentFormat(docxFormat);
        final DocumentFormat xlsxFormat = new DocumentFormat("XLSX", DocumentFamily.SPREADSHEET, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
        formatRegistry.addDocumentFormat(xlsxFormat);

        availableDocumentFormats.add("csv");
        availableDocumentFormats.add("doc");
        availableDocumentFormats.add("docx");
        availableDocumentFormats.add("xls");
        availableDocumentFormats.add("xlsx");

        availableOutputFormats.add("pdf");

        logger.info("Libreoffice server configuration: host='{}', port={}", libreofficeServiceConfiguration.getLibreofficeHost(), libreofficeServiceConfiguration.getLibreofficePort());
        logger.info("Available document convert formats: {} -> {} ", availableDocumentFormats, availableOutputFormats);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        logger.info("Received POST request to {}", req.getServletPath());
        final String inputFormat = req.getParameter("inputFormat");
        final String outputFormat = req.getParameter("outputFormat");

        if (inputFormat == null || !availableDocumentFormats.contains(inputFormat)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().print("Invalid input document format " + inputFormat + ", expected one of " + availableDocumentFormats.toString());
            return;
        }
        if (outputFormat == null || !availableOutputFormats.contains(outputFormat)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().print("Invalid output document format " + outputFormat + ", expected one of " + availableDocumentFormats.toString());
            return;
        }

        logger.info("Converting request input from {} to {}", inputFormat, outputFormat);
        resp.setContentType(formatRegistry.getFormatByFileExtension(outputFormat).getMimeType());

        OpenOfficeConnection connection = null;
        try {
            connection = new SocketOpenOfficeConnection(libreofficeServiceConfiguration.getLibreofficeHost(), libreofficeServiceConfiguration.getLibreofficePort());
            connection.connect();
            final DocumentConverter converter = new StreamOpenOfficeDocumentConverter(connection, formatRegistry);
            converter.convert(req.getInputStream(), formatRegistry.getFormatByFileExtension(inputFormat), resp.getOutputStream(), formatRegistry.getFormatByFileExtension(outputFormat));
            logger.info("Converted document from {} to {}", inputFormat, outputFormat);
        } catch (final Exception e) {
            logger.error("Cannot convert file to {} format", outputFormat, e);
            resp.reset();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().print("Unexpected error while converting document from " + inputFormat + " to " + outputFormat + ": " + e.getMessage());
        } finally {
            if (connection != null && connection.isConnected()) {
                connection.disconnect();
            }
        }
    }
}
