package infrastructure.reports;

import cores.BaseTest;
import cores.WebsiteDriver;
import io.qameta.allure.Allure;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom TestNG listener that integrates with Allure reporting.
 *
 * Responsibilities:
 *  1. onStart  — records the run start timestamp.
 *  2. onFinish — once the run completes:
 *                  a) generates a single-file static HTML report tagged with this run's timestamp,
 *                  b) archives the raw allure-results into a timestamped folder,
 *                  c) enforces a MAX_RECORDS cap on both archives and HTML reports.
 *  3. onTestFailure / onTestSkipped — attaches a screenshot to the Allure report.
 *
 * Folder structure produced (Maven):
 *   target/allure-results/                                    ← current live results (written by allure-testng)
 *   target/allure-results-archives/
 *       allure-results-2026-04-13_10-00-00/                   ← archived raw results per run
 *   target/allure-reports/
 *       report-2026-04-13_10-00-00.html                       ← single-file HTML report per run
 *
 * To open a specific report, just open the HTML file directly in any browser.
 */
public class AllureListener implements ITestListener, ISuiteListener {

    private static final int MAX_RECORDS = 20;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private String currentTimestamp;

    // ── Suite lifecycle ────────────────────────────────────────────────────────

    @Override
    public void onStart(ISuite suite) {
        // Capture the run start time — used to name the report and archive for THIS run
        currentTimestamp = LocalDateTime.now().format(FORMATTER);
    }

    @Override
    public void onFinish(ISuite suite) {
        // Step 1: Generate a static HTML report from the current run's results
        generateAndOpenReport();

        // Step 2: Archive the current run's raw results into a timestamped folder
        // Done after report generation so the timestamp belongs to this completed run
        archiveCurrentResults();

        // Step 3: Keep only the latest MAX_RECORDS archived results folders
        cleanOldFolders(getArchivesDir(), "allure-results-");

        // Step 4: Keep only the latest MAX_RECORDS HTML reports
        cleanOldFiles(getReportsDir(), "report-", ".html");
    }

    // ── Report generation & archiving ─────────────────────────────────────────

    private void generateAndOpenReport() {
        Path resultsPath = getResultsPath();
        Path reportsDir = getReportsDir();
        Path reportFile = reportsDir.resolve("report-" + currentTimestamp + ".html");
        Path tempDir = reportsDir.resolve(".tmp-" + currentTimestamp);

        try {
            Files.createDirectories(reportsDir);
            Files.createDirectories(tempDir);

            // Generate a self-contained single-file HTML report into a temp folder
            new ProcessBuilder("allure", "generate", "--single-file",
                    resultsPath.toString(), "-o", tempDir.toString(), "--clean")
                    .inheritIO()
                    .start()
                    .waitFor();

            // Move the generated index.html → report-{timestamp}.html
            Path generatedFile = tempDir.resolve("index.html");
            if (Files.exists(generatedFile)) {
                Files.move(generatedFile, reportFile);
                System.out.println("[AllureListener] Single-file report saved → " + reportFile);
            } else {
                System.err.println("[AllureListener] Single-file report not found at " + generatedFile);
            }

        } catch (Exception e) {
            System.err.println("[AllureListener] Could not generate report: " + e.getMessage());
        } finally {
            if (Files.exists(tempDir)) {
                deleteFolder(tempDir);
            }
        }
    }

    private void archiveCurrentResults() {
        Path resultsPath = getResultsPath();
        if (!Files.exists(resultsPath)) return;

        // Rename allure-results → allure-results-{timestamp} to preserve this run's raw data
        Path archive = getArchivesDir().resolve("allure-results-" + currentTimestamp);
        try {
            Files.createDirectories(getArchivesDir());
            resultsPath.toFile().renameTo(archive.toFile());
            System.out.println("[AllureListener] Results archived → " + archive);
        } catch (IOException e) {
            System.err.println("[AllureListener] Could not archive results: " + e.getMessage());
        }
    }

    // ── Cleanup helpers ───────────────────────────────────────────────────────

    private void cleanOldFolders(Path parentDir, String prefix) {
        if (!Files.exists(parentDir)) return;
        try (Stream<Path> stream = Files.list(parentDir)) {
            List<Path> folders = stream
                    .filter(p -> Files.isDirectory(p) && p.getFileName().toString().startsWith(prefix))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());

            while (folders.size() > MAX_RECORDS) {
                deleteFolder(folders.remove(0));
            }
        } catch (IOException e) {
            System.err.println("[AllureListener] Could not clean old folders: " + e.getMessage());
        }
    }

    private void cleanOldFiles(Path parentDir, String prefix, String suffix) {
        if (!Files.exists(parentDir)) return;
        try (Stream<Path> stream = Files.list(parentDir)) {
            List<Path> files = stream
                    .filter(p -> Files.isRegularFile(p)
                            && p.getFileName().toString().startsWith(prefix)
                            && p.getFileName().toString().endsWith(suffix))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());

            while (files.size() > MAX_RECORDS) {
                Path oldest = files.remove(0);
                try {
                    Files.delete(oldest);
                    System.out.println("[AllureListener] Deleted oldest report → " + oldest);
                } catch (IOException e) {
                    System.err.println("[AllureListener] Could not delete file: " + oldest);
                }
            }
        } catch (IOException e) {
            System.err.println("[AllureListener] Could not clean old files: " + e.getMessage());
        }
    }

    private void deleteFolder(Path folder) {
        try (Stream<Path> files = Files.walk(folder)) {
            files.sorted(Comparator.reverseOrder()).forEach(f -> f.toFile().delete());
            System.out.println("[AllureListener] Deleted oldest record → " + folder);
        } catch (IOException e) {
            System.err.println("[AllureListener] Could not delete folder: " + folder);
        }
    }

    // ── Path helpers ──────────────────────────────────────────────────────────

    // Reads allure.results.directory system property (set by Maven Surefire to target/allure-results).
    // Falls back to "allure-results" in the project root when running directly from IntelliJ.
    private Path getResultsPath() {
        return Paths.get(System.getProperty("allure.results.directory", "allure-results"));
    }

    // Returns the base directory for storing archives and reports.
    // When running via Maven:   target/
    // When running via IntelliJ: . (project root)
    private Path getBaseDir() {
        Path parent = getResultsPath().getParent();
        return parent != null ? parent : Paths.get(".");
    }

    // Directory where previous runs' raw results are archived: target/allure-results-archives/
    private Path getArchivesDir() {
        return getBaseDir().resolve("allure-results-archives");
    }

    // Directory where static HTML reports are stored: target/allure-reports/
    private Path getReportsDir() {
        return getBaseDir().resolve("allure-reports");
    }

    // ── Test lifecycle ────────────────────────────────────────────────────────

    @Override
    public void onTestFailure(ITestResult result) {
        // Capture and attach a screenshot to the Allure report on test failure
        attachScreenshot();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // Capture and attach a screenshot to the Allure report on test skip
        attachScreenshot();
    }

    private void attachScreenshot() {
        WebsiteDriver driver = BaseTest.webdriverThread.get();
        if (driver == null) return;

        try {
            byte[] screenshotBytes = Base64.getDecoder().decode(driver.takeScreenshotBASE64());
            Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(screenshotBytes), "png");
        } catch (Exception ignored) {
        }
    }
}