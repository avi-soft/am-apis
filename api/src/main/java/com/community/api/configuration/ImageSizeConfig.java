package com.community.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "image.size")
@Getter
@Setter
public class ImageSizeConfig {

    private String min;
    private String max;

    public long getMinInBytes() {
        return convertToBytes(min);
    }

    public long getMaxInBytes() {
        return convertToBytes(max);
    }

    public static long convertToBytes(String size) {
        size = size.trim().toUpperCase();

        if (size.endsWith("EB")) {
            return (long) (Double.parseDouble(size.replace("EB", "").trim()) * Math.pow(1024, 6));
        } else if (size.endsWith("PB")) {
            return (long) (Double.parseDouble(size.replace("PB", "").trim()) * Math.pow(1024, 5));
        } else if (size.endsWith("TB")) {
            return (long) (Double.parseDouble(size.replace("TB", "").trim()) * Math.pow(1024, 4));
        } else if (size.endsWith("GB")) {
            return (long) (Double.parseDouble(size.replace("GB", "").trim()) * Math.pow(1024, 3));
        } else if (size.endsWith("MB")) {
            return (long) (Double.parseDouble(size.replace("MB", "").trim()) * Math.pow(1024, 2));
        } else if (size.endsWith("KB")) {
            return (long) (Double.parseDouble(size.replace("KB", "").trim()) * 1024);
        } else if (size.endsWith("B")) {
            return (long) (Double.parseDouble(size.replace("B", "").trim()));
        }

        // Fallback: assume plain bytes if no suffix
        return Long.parseLong(size.trim());
    }


    public static String convertBytesToReadableSize(long bytes) {
        double value = bytes;

        String[] units = {"B", "KB", "MB", "GB", "TB", "PB", "EB"};
        int unitIndex = 0;

        while (value >= 1024 && unitIndex < units.length - 1) {
            value /= 1024.0;
            unitIndex++;
        }

        return String.format("%.2f %s", value, units[unitIndex]);
    }

}

