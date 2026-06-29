package utilities;

import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.math.Rational;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class ScreenRecorder {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String DEFAULT_DIR = "target" + File.separator + "recordings";
    private static final int DEFAULT_MAX = 20;

    private boolean enabled;
    private String recordingsDir;
    private int maxRecordings;
    private org.monte.screenrecorder.ScreenRecorder monteRecorder;
    private File currentFile;
    private String currentTestName;

    public ScreenRecorder() {
        this.enabled = Boolean.parseBoolean(System.getProperty("test.record", "false"));
        this.recordingsDir = System.getProperty("test.record.dir", DEFAULT_DIR);
        this.maxRecordings = Integer.parseInt(System.getProperty("test.record.max", String.valueOf(DEFAULT_MAX)));
    }

    public ScreenRecorder(boolean enabled) {
        this();
        this.enabled = enabled;
    }

    public ScreenRecorder(boolean enabled, String recordingsDir, int maxRecordings) {
        this.enabled = enabled;
        this.recordingsDir = recordingsDir;
        this.maxRecordings = maxRecordings;
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }
    public void setRecordingsDir(String dir) { this.recordingsDir = dir; }
    public String getRecordingsDir() { return recordingsDir; }
    public void setMaxRecordings(int max) { this.maxRecordings = max; }
    public int getMaxRecordings() { return maxRecordings; }

    public void start(String testName) {
        if (!enabled) return;

        this.currentTestName = testName;
        File recordDir = new File(recordingsDir);
        recordDir.mkdirs();

        try {
            GraphicsConfiguration gc = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();

            monteRecorder = new org.monte.screenrecorder.ScreenRecorder(gc,
                    gc.getBounds(),
                    new Format(MediaTypeKey, FormatKeys.MediaType.FILE, MimeTypeKey, MIME_AVI),
                    new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                            CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                            DepthKey, 24, FrameRateKey, Rational.valueOf(15),
                            QualityKey, 0.5f, KeyFrameIntervalKey, 15 * 60),
                    new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, EncodingKey, "black", FrameRateKey, Rational.valueOf(30)),
                    null,
                    recordDir);

            monteRecorder.start();
            System.out.println("[ScreenRecorder] Recording started: " + testName);
        } catch (Exception e) {
            System.out.println("[ScreenRecorder] Failed to start recording: " + e.getMessage());
            monteRecorder = null;
        }
    }

    public void stop() {
        if (!enabled || monteRecorder == null) return;

        try {
            monteRecorder.stop();
            List<File> files = monteRecorder.getCreatedMovieFiles();
            if (!files.isEmpty()) {
                File recorded = files.get(files.size() - 1);
                String timestamp = LocalDateTime.now().format(FORMATTER);
                String newName = currentTestName + "_" + timestamp + ".avi";
                currentFile = new File(recorded.getParent(), newName);
                recorded.renameTo(currentFile);
                System.out.println("[ScreenRecorder] Saved: " + currentFile.getPath());

                cleanOldRecordings();
            }
        } catch (Exception e) {
            System.out.println("[ScreenRecorder] Failed to stop recording: " + e.getMessage());
        } finally {
            monteRecorder = null;
        }
    }

    public void deleteLastRecording() {
        if (currentFile != null && currentFile.exists()) {
            String name = currentFile.getName();
            currentFile.delete();
            System.out.println("[ScreenRecorder] Deleted: " + name);
            currentFile = null;
        }
    }

    public void clearAll() {
        Path dir = Path.of(recordingsDir);
        if (!Files.exists(dir)) return;

        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".avi"))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {}
                    });
            System.out.println("[ScreenRecorder] All recordings cleared.");
        } catch (IOException e) {
            System.out.println("[ScreenRecorder] Failed to clear recordings: " + e.getMessage());
        }
    }

    public File getLastRecording() {
        return currentFile;
    }

    private void cleanOldRecordings() {
        Path dir = Path.of(recordingsDir);
        if (!Files.exists(dir)) return;

        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> recordings = stream
                    .filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".avi"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());

            while (recordings.size() > maxRecordings) {
                Path oldest = recordings.remove(0);
                try {
                    Files.delete(oldest);
                    System.out.println("[ScreenRecorder] Cleaned old recording: " + oldest.getFileName());
                } catch (IOException e) {
                    System.out.println("[ScreenRecorder] Failed to delete: " + oldest.getFileName());
                }
            }
        } catch (IOException e) {
            System.out.println("[ScreenRecorder] Failed to clean recordings: " + e.getMessage());
        }
    }
}
