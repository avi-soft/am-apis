package com.community.api.component;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/**
 * Utility for automatically downloading and setting up FFmpeg based on the operating system
 */
@Component
public class FFmpegManager {
    private static final Logger logger = LoggerFactory.getLogger(FFmpegManager.class);

    // Base directory for FFmpeg installation
    private final Path ffmpegBaseDir;

    // FFmpeg executable path
    private Path ffmpegPath;

    // Configurable FFmpeg URLs for different platforms
    @Value("${ffmpeg.url.windows}")
    private String windowsFfmpegUrl;

    @Value("${ffmpeg.url.mac}")
    private String macFfmpegUrl;

    @Value("${ffmpeg.url.linux}")
    private String linuxFfmpegUrl;

    // Flag to track initialization state
    private boolean initialized = false;

    /**
     * Initialize with application's temporary directory
     * Note: This constructor should not call initialize() directly due to @Value injection timing
     */
    public FFmpegManager() {
        this.ffmpegBaseDir = Paths.get(System.getProperty("java.io.tmpdir"), "ffmpeg-bin");
    }

    /**
     * Initialize with custom directory
     * Note: This constructor should not call initialize() directly due to @Value injection timing
     */
    public FFmpegManager(Path customDirectory) {
        this.ffmpegBaseDir = customDirectory;
    }

    /**
     * Post-construct method that initializes FFmpeg after Spring has injected property values
     */
    @PostConstruct
    public void postConstruct() {
        initialize();
    }

    /**
     * Initialize and download FFmpeg if needed
     */
    private void initialize() {
        if (initialized) {
            return;
        }

        try {
            // Create base directory if it doesn't exist
            if (!Files.exists(ffmpegBaseDir)) {
                Files.createDirectories(ffmpegBaseDir);
            }

            String os = detectOperatingSystem();
            logger.info("Detected operating system: {}", os);

            // Check if FFmpeg is already downloaded
            Path expectedPath = getExpectedFFmpegPath(os);
            if (Files.exists(expectedPath) && Files.isExecutable(expectedPath)) {
                logger.info("FFmpeg already exists at: {}", expectedPath);
                this.ffmpegPath = expectedPath;
                initialized = true;
                return;
            }

            // Download FFmpeg for the current OS
            logger.info("Downloading FFmpeg for {} to {}", os, ffmpegBaseDir);
            downloadFFmpeg(os);

            // Verify FFmpeg exists and is executable
            if (Files.exists(expectedPath) && Files.isExecutable(expectedPath)) {
                this.ffmpegPath = expectedPath;
                logger.info("FFmpeg successfully installed at: {}", ffmpegPath);

                // Test FFmpeg
                testFFmpeg();
                initialized = true;
            } else {
                logger.error("Failed to install FFmpeg at expected path: {}", expectedPath);
                throw new IOException("Failed to install FFmpeg");
            }
        } catch (Exception e) {
            logger.error("Error initializing FFmpeg: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize FFmpeg", e);
        }
    }

    /**
     * Get the FFmpeg executable path
     */
    public Path getFFmpegPath() {
        if (ffmpegPath == null) {
            // Lazy initialization if someone calls this before PostConstruct
            if (!initialized) {
                initialize();
            }

            if (ffmpegPath == null) {
                throw new IllegalStateException("FFmpeg is not initialized");
            }
        }
        return ffmpegPath;
    }

    /**
     * Get FFmpeg command for processing
     */
    public String getFFmpegExecutable() {
        return getFFmpegPath().toString();
    }

    /**
     * Detect the operating system
     */
    private String detectOperatingSystem() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "windows";
        } else if (os.contains("mac")) {
            return "mac";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return "linux";
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + os);
        }
    }

    /**
     * Get expected FFmpeg path based on OS
     */
    private Path getExpectedFFmpegPath(String os) {
        switch (os) {
            case "windows":
                return ffmpegBaseDir.resolve("ffmpeg.exe");
            case "mac":
            case "linux":
                return ffmpegBaseDir.resolve("ffmpeg");
            default:
                throw new UnsupportedOperationException("Unsupported OS: " + os);
        }
    }

    /**
     * Download FFmpeg for the current OS
     */
    private void downloadFFmpeg(String os) throws IOException {
        String url;
        switch (os) {
            case "windows":
                url = windowsFfmpegUrl;
                logger.debug("Using Windows FFmpeg URL: {}", url);
                downloadAndExtractZip(url, "windows");
                break;
            case "mac":
                url = macFfmpegUrl;
                logger.debug("Using Mac FFmpeg URL: {}", url);
                downloadAndExtractZip(url, "mac");
                break;
            case "linux":
                url = linuxFfmpegUrl;
                logger.debug("Using Linux FFmpeg URL: {}", url);
                downloadAndExtractZip(url, "linux");
                break;
            default:
                throw new UnsupportedOperationException("Unsupported OS: " + os);
        }
    }

    /**
     * Download and extract a ZIP file
     */
    private void downloadAndExtractZip(String url, String os) throws IOException {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("FFmpeg download URL cannot be null or empty for OS: " + os);
        }

        Path tempFile = Files.createTempFile("ffmpeg-download", ".zip");

        try {
            // Download ZIP file
            logger.info("Downloading FFmpeg from {}", url);
            downloadFile(url, tempFile);

            // Extract ZIP file
            logger.info("Extracting FFmpeg archive");
            try (ZipInputStream zipStream = new ZipInputStream(new BufferedInputStream(
                    Files.newInputStream(tempFile)))) {

                ZipEntry entry;
                boolean found = false;

                // For Mac, the structure might be different
                if (os.equals("mac")) {
                    while ((entry = zipStream.getNextEntry()) != null) {
                        String entryName = entry.getName();
                        logger.debug("Mac archive entry: {}", entryName);

                        // For Mac builds from evermeet.cx, the executable is usually just named "ffmpeg"
                        if (entryName.equals("ffmpeg")) {
                            Path ffmpegExecutable = getExpectedFFmpegPath(os);
                            logger.info("Found Mac FFmpeg executable: {}, extracting to {}", entryName, ffmpegExecutable);

                            // Copy the executable
                            Files.copy(zipStream, ffmpegExecutable, StandardCopyOption.REPLACE_EXISTING);

                            // Make it executable
                            ffmpegExecutable.toFile().setExecutable(true, false);
                            try {
                                Files.setPosixFilePermissions(ffmpegExecutable,
                                        PosixFilePermissions.fromString("rwxr-xr-x"));
                            } catch (UnsupportedOperationException e) {
                                // Not all filesystems support POSIX permissions, ignore
                            }

                            found = true;
                            break;
                        }
                    }
                } else {
                    while ((entry = zipStream.getNextEntry()) != null) {
                        String entryName = entry.getName();
                        logger.debug("Examining entry: {}", entryName);

                        // Look for the FFmpeg executable in the archive
                        if (entryName.endsWith("ffmpeg.exe") || entryName.endsWith("/ffmpeg") ||
                                entryName.contains("/bin/ffmpeg") || entryName.contains("\\bin\\ffmpeg.exe")) {
                            Path ffmpegExecutable = getExpectedFFmpegPath(os);
                            logger.info("Found FFmpeg executable: {}, extracting to {}", entryName, ffmpegExecutable);

                            // Copy the executable
                            Files.copy(zipStream, ffmpegExecutable, StandardCopyOption.REPLACE_EXISTING);

                            // Make it executable on Mac/Linux
                            if (!os.equals("windows")) {
                                ffmpegExecutable.toFile().setExecutable(true, false);
                                // On some Unix systems, also try with POSIX permissions
                                try {
                                    Files.setPosixFilePermissions(ffmpegExecutable,
                                            PosixFilePermissions.fromString("rwxr-xr-x"));
                                } catch (UnsupportedOperationException e) {
                                    // Not all filesystems support POSIX permissions, ignore
                                }
                            }

                            break;
                        }
                    }
                }
            }
        } finally {
            // Clean up
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Alternative approach: Use system's package manager to install FFmpeg on Linux
     * This is a fallback method in case the direct download fails
     */
    private boolean tryInstallFFmpegWithPackageManager() {
        if (!detectOperatingSystem().equals("linux")) {
            return false;
        }

        try {
            logger.info("Attempting to install FFmpeg using system package manager");

            // First, check if ffmpeg is already available
            Process checkProcess = Runtime.getRuntime().exec("which ffmpeg");
            int checkExitCode = checkProcess.waitFor();

            if (checkExitCode == 0) {
                // FFmpeg is already installed
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(checkProcess.getInputStream()))) {
                    String ffmpegPath = reader.readLine();
                    if (ffmpegPath != null && !ffmpegPath.isEmpty()) {
                        // Create a symlink to the system's ffmpeg
                        Path systemFFmpeg = Paths.get(ffmpegPath);
                        Path ourFFmpeg = getExpectedFFmpegPath("linux");
                        Files.createSymbolicLink(ourFFmpeg, systemFFmpeg);
                        logger.info("Created symlink to system FFmpeg at: {}", systemFFmpeg);
                        return true;
                    }
                }
            }

            // Try common package managers
            String[] packageManagers = {
                    "apt-get -y install ffmpeg",
                    "yum -y install ffmpeg",
                    "dnf -y install ffmpeg",
                    "pacman -S --noconfirm ffmpeg"
            };

            for (String installCmd : packageManagers) {
                Process process = Runtime.getRuntime().exec("which " + installCmd.split(" ")[0]);
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    // This package manager exists, try to use it
                    logger.info("Attempting to install FFmpeg with: {}", installCmd);
                    Process installProcess = Runtime.getRuntime().exec("sudo " + installCmd);
                    int installExitCode = installProcess.waitFor();

                    if (installExitCode == 0) {
                        // Installation successful, now find the path
                        Process whichProcess = Runtime.getRuntime().exec("which ffmpeg");
                        int whichExitCode = whichProcess.waitFor();

                        if (whichExitCode == 0) {
                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(whichProcess.getInputStream()))) {
                                String ffmpegPath = reader.readLine();
                                if (ffmpegPath != null && !ffmpegPath.isEmpty()) {
                                    // Create a symlink to the system's ffmpeg
                                    Path systemFFmpeg = Paths.get(ffmpegPath);
                                    Path ourFFmpeg = getExpectedFFmpegPath("linux");
                                    Files.createSymbolicLink(ourFFmpeg, systemFFmpeg);
                                    logger.info("Created symlink to system FFmpeg at: {}", systemFFmpeg);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to install FFmpeg with package manager: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Download a file from URL
     */
    private void downloadFile(String url, Path destination) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("Download URL cannot be null");
        }

        try (ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(destination.toFile())) {

            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    /**
     * Test if FFmpeg runs correctly
     */
    private void testFFmpeg() {
        try {
            ProcessBuilder pb = new ProcessBuilder(ffmpegPath.toString(), "-version");
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                logger.error("FFmpeg test failed with exit code: {}", exitCode);
                throw new RuntimeException("FFmpeg test failed");
            }

            // Read version info
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String versionLine = reader.readLine();
                logger.info("FFmpeg version: {}", versionLine);
            }
        } catch (Exception e) {
            logger.error("Error testing FFmpeg: {}", e.getMessage(), e);
            throw new RuntimeException("FFmpeg test failed", e);
        }
    }

    /**
     * Execute FFmpeg command
     */
    public boolean executeFFmpegCommand(String... args) throws IOException, InterruptedException {
        if (ffmpegPath == null) {
            // Lazy initialization
            if (!initialized) {
                initialize();
            }

            if (ffmpegPath == null) {
                throw new IllegalStateException("FFmpeg is not initialized");
            }
        }

        String[] command = new String[args.length + 1];
        command[0] = ffmpegPath.toString();
        System.arraycopy(args, 0, command, 1, args.length);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Read output for debugging
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("FFmpeg: {}", line);
            }
        }

        int exitCode = process.waitFor();
        return exitCode == 0;
    }
}