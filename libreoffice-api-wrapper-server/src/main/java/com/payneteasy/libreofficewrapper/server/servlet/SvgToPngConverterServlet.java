package com.payneteasy.libreofficewrapper.server.servlet;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class SvgToPngConverterServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(SvgToPngConverterServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("Received POST request to {}", req.getServletPath());
        File png = File.createTempFile("png", ".png");
        png.deleteOnExit();
        try {
            TranscoderInput transcoderInput = new TranscoderInput(req.getInputStream());

            OutputStream os = new FileOutputStream(png);
            TranscoderOutput transcoderOutput = new TranscoderOutput(os);
            PNGTranscoder pngTranscoder = new PNGTranscoder();

            pngTranscoder.transcode(transcoderInput, transcoderOutput);

            os.flush();
            os.close();

            resp.setContentType("image/png");
            resp.setContentLength((int) png.length());

            resp.setHeader("Content-Disposition", "attachment; filename=" + png.getName());
            try (BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(png))) {
                BufferedOutputStream outStream = new BufferedOutputStream(resp.getOutputStream());
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
                outStream.flush();
            }
        } catch (TranscoderException e) {
            logger.error("Cannot convert file to {} format", "png", e);
        } finally {
            if(png.exists()){
                png.delete();
            }
        }
    }
}
