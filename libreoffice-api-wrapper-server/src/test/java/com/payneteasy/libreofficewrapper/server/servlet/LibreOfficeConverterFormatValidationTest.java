package com.payneteasy.libreofficewrapper.server.servlet;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Format validation of {@link LibreofficeConverterServlet}, which happens before the request body
 * is read, so these cases need the wrapper running but not the libreoffice backend.
 *
 * Attention!
 * You must start java! See more information in readme.md
 */
@RunWith(Parameterized.class)
public class LibreOfficeConverterFormatValidationTest {

    private static final String CONVERT_URL = "http://localhost:8080/converter/convert";

    @Parameters(name = "inputFormat={0}, outputFormat={1}")
    public static Collection<Object[]> unsupportedFormats() {
        return Arrays.asList(
            new Object[][]{
                {null, "pdf", "Invalid input document format null"},
                {"txt", "pdf", "Invalid input document format txt"},
                {"pdf", "pdf", "Invalid input document format pdf"},
                {"docx", null, "Invalid output document format null"},
                {"docx", "docx", "Invalid output document format docx"}
            }
        );
    }

    private final String inputFormat;
    private final String outputFormat;
    private final String expectedMessage;

    public LibreOfficeConverterFormatValidationTest(
        String inputFormat,
        String outputFormat,
        String expectedMessage
    ) {
        this.inputFormat = inputFormat;
        this.outputFormat = outputFormat;
        this.expectedMessage = expectedMessage;
    }

    @Test
    public void rejectsUnsupportedFormat() throws Exception {
        final URIBuilder uriBuilder = new URIBuilder(CONVERT_URL);
        if (inputFormat != null) {
            uriBuilder.addParameter("inputFormat", inputFormat);
        }
        if (outputFormat != null) {
            uriBuilder.addParameter("outputFormat", outputFormat);
        }

        try (final CloseableHttpClient httpClient = HttpClients.createDefault();
             final CloseableHttpResponse response = httpClient.execute(new HttpPost(uriBuilder.build()))
        ) {
            final String body = response.getEntity() == null
                ? ""
                : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            assertEquals(body, 400, response.getStatusLine().getStatusCode());
            assertTrue("unexpected body: " + body, body.startsWith(expectedMessage));

            final Header contentType = response.getFirstHeader("Content-Type");
            assertTrue(
                "unexpected content type " + contentType,
                contentType != null && contentType.getValue().startsWith("text/plain")
            );
        }
    }
}
