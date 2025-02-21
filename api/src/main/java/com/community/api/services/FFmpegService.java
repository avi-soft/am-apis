package com.community.api.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;

@Service
public class FFmpegService {

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    @Value("${ffplay.path}")
    private String ffplayPath;

    @Value("${ffprobe.path}")
    private String ffprobePath;

    private final ResourceLoader resourceLoader;

    public FFmpegService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private String extractExecutable(String resourcePath) throws Exception {
        Resource resource = resourceLoader.getResource(resourcePath);
        File tempFile = Files.createTempFile("ffmpeg_", ".exe").toFile();
        tempFile.deleteOnExit();

        try (InputStream in = resource.getInputStream(); FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        tempFile.setExecutable(true); // Make the file executable
        return tempFile.getAbsolutePath();
    }

    public String getFFmpegExecutable() throws Exception {
        return extractExecutable(ffmpegPath);
    }

    public String getFFplayExecutable() throws Exception {
        return extractExecutable(ffplayPath);
    }

    public String getFFprobeExecutable() throws Exception {
        return extractExecutable(ffprobePath);
    }
}
