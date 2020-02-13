package com.payneteasy.libreofficewrapper.server.servlet;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Attention!
 * You must start docker with libreoffice and start java! See more information in readme.md
 */
public class LibreOfficeConverterServletTest {


    @Test
    public void testVersionController() throws IOException {
        final URL url = new URL("http://localhost:8080/converter/management/version.txt");
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        final StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        System.out.println("Version is " + content.toString());
        assertFalse(content.toString().isEmpty());
    }

    @Test
    public void testLibreConverter() throws IOException {
        final URL url = new URL("http://localhost:8080/converter/convert");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        final InputStream file = LibreOfficeConverterServletTest.class.getClassLoader().getResourceAsStream("src/test/resources/example.docx");
        final HashMap<String, String> params = new HashMap<>();
        params.put("inputFormat", "docx");
        params.put("outputFormat", "pdf");
        params.put("inputStream", String.valueOf(file));
        connection.setDoOutput(true);
        final DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.writeBytes(getParamsString(params));
        out.flush();
        out.close();
        Files.deleteIfExists(Paths.get("src/test/resources/convert.pdf"));
        final InputStream inputStream = connection.getInputStream();
        final FileOutputStream outputStream = new FileOutputStream(new File("src/test/resources/convert.pdf"));
        int bytesRead = -1;
        byte[] buffer = new byte[4096];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
        connection.disconnect();
    }

    public static String getParamsString(final Map<String, String> params) {
        final StringBuilder result = new StringBuilder();

        params.forEach((name, value) -> {
            try {
                result.append(URLEncoder.encode(name, "UTF-8"));
                result.append('=');
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));
                result.append('&');
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

        final String resultString = result.toString();
        return !resultString.isEmpty()
            ? resultString.substring(0, resultString.length() - 1)
            : resultString;
    }


}