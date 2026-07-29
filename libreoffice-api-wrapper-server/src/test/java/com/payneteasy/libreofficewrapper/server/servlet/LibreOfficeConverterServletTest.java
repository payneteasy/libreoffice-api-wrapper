package com.payneteasy.libreofficewrapper.server.servlet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Attention!
 * You must start docker with libreoffice and start java! See more information in readme.md
 */
public class LibreOfficeConverterServletTest {

    private static final String BASE_URL = "http://localhost:8080/converter";

    private static final ContentType DOCX_CONTENT_TYPE = ContentType.create(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'};

    @Test
    public void testVersionController() throws IOException, URISyntaxException {
        final HttpURLConnection connection = (HttpURLConnection) new URI(
            BASE_URL + "/management/version.txt"
        ).toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        assertEquals(200, connection.getResponseCode());

        final String version;
        try (final BufferedReader in = new BufferedReader(
            new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
        )) {
            version = in.lines().collect(Collectors.joining());
        }

        System.out.println("Version is " + version);

        assertTrue(
            "expected a version like 3.1.8, got '" + version + "'",
            version.matches("\\d+(\\.\\d+)+.*")
        );
    }

    @Test
    public void testLibreofficeConverter() throws Exception {
        final Response response = post(
            new URIBuilder(BASE_URL + "/convert")
                .addParameter("inputFormat", "docx")
                .addParameter("outputFormat", "pdf")
                .build(),
            "src/test/resources/example.docx",
            DOCX_CONTENT_TYPE
        );

        assertEquals(describe(response), 200, response.status);
        assertTrue(
            "unexpected content type " + response.contentType,
            response.contentType != null && response.contentType.startsWith("application/pdf")
        );
        assertTrue(
            "expected a pdf, got " + describe(response),
            response.body.length > PDF_SIGNATURE.length
        );
        assertArrayEquals(
            "expected a pdf, got " + describe(response),
            PDF_SIGNATURE,
            Arrays.copyOf(response.body, PDF_SIGNATURE.length)
        );
        assertTrue(
            "suspiciously small pdf: " + response.body.length + " bytes",
            response.body.length > 1000
        );

        write(response, "src/test/resources/example.pdf");
    }

    @Test
    public void testSvgToPngConverter() throws Exception {
        final Response response = post(
            new URI(BASE_URL + "/convert/svg-to-png"),
            "src/test/resources/example.svg",
            ContentType.create("text/plain")
        );

        assertEquals(describe(response), 200, response.status);
        assertTrue(
            "unexpected content type " + response.contentType,
            response.contentType != null && response.contentType.startsWith("image/png")
        );
        assertTrue(
            "expected a png, got " + describe(response),
            response.body.length > PNG_SIGNATURE.length
        );
        assertArrayEquals(
            "expected a png, got " + describe(response),
            PNG_SIGNATURE,
            Arrays.copyOf(response.body, PNG_SIGNATURE.length)
        );

        write(response, "src/test/resources/example.png");
    }

    private Response post(
        URI uri,
        String inputFilePath,
        ContentType contentType
    ) throws IOException {
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpPost post = new HttpPost(uri);
            post.setEntity(
                new InputStreamEntity(
                    new FileInputStream(Paths.get(inputFilePath).toFile()),
                    contentType
                )
            );

            try (final CloseableHttpResponse response = httpClient.execute(post)) {
                final HttpEntity entity = response.getEntity();
                final Header contentTypeHeader = response.getFirstHeader("Content-Type");

                return new Response(
                    response.getStatusLine().getStatusCode(),
                    contentTypeHeader == null ? null : contentTypeHeader.getValue(),
                    entity == null ? new byte[0] : EntityUtils.toByteArray(entity)
                );
            }
        }
    }

    private void write(Response response, String outputFilePath) throws IOException {
        final Path outputPath = Paths.get(outputFilePath);
        Files.deleteIfExists(outputPath);
        Files.write(outputPath, response.body);
    }

    /** Used in assertion messages only, so that a binary body stays readable enough to debug. */
    private static String describe(Response response) {
        return "HTTP " + response.status + ", body: " + new String(
            Arrays.copyOf(response.body, Math.min(response.body.length, 200)),
            StandardCharsets.UTF_8
        );
    }

    private static final class Response {
        private final int    status;
        private final String contentType;
        private final byte[] body;

        private Response(int status, String contentType, byte[] body) {
            this.status = status;
            this.contentType = contentType;
            this.body = body;
        }
    }
}
