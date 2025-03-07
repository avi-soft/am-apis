package com.community.api.configuration;

import com.community.api.component.FFmpegManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
public class ApplicationConfig {

    @Value("${ffmpeg.custom.directory:#{null}}")
    private String customDirectory;
    
    /**
     * Create and configure FFmpegManager
     */
    @Bean
    public FFmpegManager ffmpegManager() {
        if (customDirectory != null && !customDirectory.isEmpty()) {
            // Use custom directory if specified
            return new FFmpegManager(Paths.get(customDirectory));
        } else {
            // Use default temp directory
            return new FFmpegManager();
        }
    }
}