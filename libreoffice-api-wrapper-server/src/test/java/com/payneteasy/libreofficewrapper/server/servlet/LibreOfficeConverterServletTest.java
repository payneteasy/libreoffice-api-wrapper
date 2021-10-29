package com.payneteasy.libreofficewrapper.server.servlet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Attention!
 * You must start docker with libreoffice and start java! See more information in readme.md
 */
@SuppressWarnings("squid:S2699")
public class LibreOfficeConverterServletTest {

    @Test
    public void testVersionController() throws IOException {
        final URL url = new URL("http://localhost:8080/converter/management/version.txt");

        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        final BufferedReader in = new BufferedReader(
            new InputStreamReader(connection.getInputStream())
        );

        String inputLine;
        final StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        System.out.println("Version is " + content);

        assertFalse(content.toString().isEmpty());
    }

    @Test
    public void testLibreofficeConverter() throws Exception {
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final URI uri = new URIBuilder("http://localhost:8080/converter/convert")
                .addParameter("inputFormat", "docx")
                .addParameter("outputFormat", "pdf")
                .build();

            final HttpPost post = new HttpPost(uri);
            post.setEntity(
                new InputStreamEntity(
                    new FileInputStream(
                        Paths.get("src/test/resources/example.docx").toFile()
                    ),
                    ContentType.create(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    )
                )
            );

            doPost(httpClient, post, "src/test/resources/example.pdf");
        }
    }

    @Test
    public void testSvgToPngConverter() throws Exception {
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpPost post = new HttpPost("http://localhost:8080/converter/convert/svg-to-png");
            post.setEntity(
                new InputStreamEntity(
                    new FileInputStream(
                        Paths.get("src/test/resources/example.svg").toFile()
                    ),
                    ContentType.create("text/plain")
                )
            );

            doPost(httpClient, post, "src/test/resources/example.png");
        }
    }

    private void doPost(
        CloseableHttpClient httpClient,
        HttpPost httpPost,
        String outputFilePath
    ) throws IOException {
        try (final CloseableHttpResponse response = httpClient.execute(httpPost);
             final InputStream connectionInputStream = response.getEntity().getContent()
        ) {
            final Path outputPath = Paths.get(outputFilePath);
            Files.deleteIfExists(outputPath);

            final FileOutputStream fileOutputStream = new FileOutputStream(outputPath.toFile());
            int bytesRead;
            byte[] buffer = new byte[4096];
            while ((bytesRead = connectionInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.close();
        }
    }
}
