package com.payneteasy.libreofficewrapper.server.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S1989")
public class SvgToPngConverterServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(SvgToPngConverterServlet.class);

    @Override
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        logger.info("Received POST request to {}", request.getServletPath());

        final File pngFile = File.createTempFile("png", ".pngFile");
        pngFile.deleteOnExit();

        try {
            final TranscoderInput transcoderInput = new TranscoderInput(request.getInputStream());

            final OutputStream os = new FileOutputStream(pngFile);
            final TranscoderOutput transcoderOutput = new TranscoderOutput(os);

            final PNGTranscoder pngTranscoder = new PNGTranscoder();
            pngTranscoder.transcode(transcoderInput, transcoderOutput);

            os.flush();
            os.close();

            response.setContentType("image/png");
            response.setContentLength((int) pngFile.length());
            response.setHeader("Content-Disposition", "attachment; filename=" + pngFile.getName());

            try (final BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(pngFile))) {
                final BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream());
                final byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
                outStream.flush();
            }
        } catch (TranscoderException e) {
            logger.error("Cannot convert file to {} format", "png", e);
        } finally {
            if (pngFile.exists()) {
                pngFile.delete();
            }
        }
    }
}
