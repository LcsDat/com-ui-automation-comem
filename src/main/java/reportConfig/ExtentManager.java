package reportConfig;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import cores.GlobalVariables;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtentManager {
    private static ExtentManager extentManager;
    private static ExtentSparkReporter extentSparkReporter;
    private static ExtentReports extentReports;
    protected static final ConcurrentHashMap<String, ExtentTest> extentTestSuiteMap = new ConcurrentHashMap<>();
    protected static final ConcurrentHashMap<String, ExtentTest> extentTestCaseMap = new ConcurrentHashMap<>();

    private ExtentManager() {
        extentSparkReporter = new ExtentSparkReporter(GlobalVariables.PROJECTPATH + "/extentV5/Hasaki.html");
        extentReports = new ExtentReports();

        extentSparkReporter.config().setReportName("Hasaki Test Report");
        extentSparkReporter.config().setDocumentTitle("Hasaki Test Report");
        extentSparkReporter.config().setTimelineEnabled(true);
        extentSparkReporter.config().setEncoding("utf-8");
        extentSparkReporter.config().setTheme(Theme.DARK);
        extentSparkReporter.config().setTimeStampFormat("MMM dd, HH:mm:ss a");

        extentReports.attachReporter(extentSparkReporter);
        extentReports.setSystemInfo("Company", "Hideyashy");
        extentReports.setSystemInfo("Project", "Hasaki");
        extentReports.setSystemInfo("Team", "Hideyashy Team");
        extentReports.setSystemInfo("Contact", "datle.testing01@gmail.com");
        extentReports.setSystemInfo("JDK version", GlobalVariables.JAVA_VERSION);
    }

    private ExtentManager(String fileName, String reportName, String documentTitle, boolean enableTimeline,
                          String encoidng, Theme theme, String timeFormat, String companyName, String projectName,
                          String teamName, String contactInfo, String programmingVersion) {
        extentSparkReporter = new ExtentSparkReporter(GlobalVariables.PROJECTPATH + "/extentV5/" + fileName);
        extentReports = new ExtentReports();

        extentSparkReporter.config().setReportName(reportName);
        extentSparkReporter.config().setDocumentTitle(documentTitle);
        extentSparkReporter.config().setTimelineEnabled(enableTimeline);
        extentSparkReporter.config().setEncoding(encoidng);
        extentSparkReporter.config().setTheme(theme);
        extentSparkReporter.config().setTimeStampFormat(timeFormat);

        extentReports.attachReporter(extentSparkReporter);
        extentReports.setSystemInfo("Company", companyName);
        extentReports.setSystemInfo("Project", projectName);
        extentReports.setSystemInfo("Team", teamName);
        extentReports.setSystemInfo("Contact", contactInfo);
        extentReports.setSystemInfo("JDK version", programmingVersion);
    }

    /**
     * Gets the singleton instance of ExtentReports
     * Creates a new instance if one doesn't exist
     * This instance include pre-configured Extent report
     *
     * @return ExtentManager instance
     */
    public static ExtentManager getInstance() {

        if (extentReports == null && extentManager == null) {
            extentManager = new ExtentManager();
        }
        return extentManager;
    }

    /**
     * Gets the singleton instance of ExtentReports
     * Creates a new instance if one doesn't exist
     * This instance allow customizing Extent report
     *
     * @return ExtentManager instance
     */
    public static ExtentManager getInstance(String fileName, String reportName, String documentTitle, boolean enableTimeline,
                                            String encoidng, Theme theme, String timeFormat, String companyName, String projectName,
                                            String teamName, String contactInfo, String programmingVersion) {

        if (extentReports == null && extentManager == null) {
            extentManager = new ExtentManager(fileName, reportName, documentTitle, enableTimeline,
                    encoidng, theme, timeFormat, companyName, projectName, teamName, contactInfo, programmingVersion);
        }
        return extentManager;
    }


//    public void initReport() {
//        // Initialize the reporter
//        ExtentSparkReporter reporter = new ExtentSparkReporter(GlobalVariables.PROJECTPATH + "/extentV5/Hasaki.html");
//
//        // Configure the reporter appearance
//        reporter.config().setReportName("Hasaki Test Report");
//        reporter.config().setDocumentTitle("Hasaki Test Report");
//        reporter.config().setTimelineEnabled(true);
//        reporter.config().setEncoding("utf-8");
//        reporter.config().setTheme(Theme.DARK);
//        reporter.config().setTimeStampFormat("MMM dd, HH:mm:ss a");
//
//        // Initialize ExtentReports and attach the reporter
//        extentReports = new ExtentReports();
//        extentReports.attachReporter(reporter);
//        extentReports.setSystemInfo("Company", "Hideyashy");
//        extentReports.setSystemInfo("Project", "Hasaki");
//        extentReports.setSystemInfo("Team", "Hideyashy Team");
//        extentReports.setSystemInfo("Contact", "datle.testing01@gmail.com");
//        extentReports.setSystemInfo("JDK version", GlobalVariables.JAVA_VERSION);
//
//    }

    public static ExtentReports getExtentReports() {
        return extentReports;
    }

    /**
     * Create Extent report - Suite level
     *
     * @param suiteName
     */
    public ExtentTest createExtentTestSuite(String suiteName) {
        var extentTest = extentReports.createTest(suiteName);
        extentTestSuiteMap.put(suiteName, extentTest);
        return getExtentTestSuiteMap().get(suiteName);
    }

    public ExtentTest createExtentTestCase(String suiteName, String testCaseName) {
        var extentTest = getExtentTestSuiteMap().get(suiteName).createNode(testCaseName);
        extentTestCaseMap.put(testCaseName, extentTest);
        return getExtentTestCaseMap().get(testCaseName);
    }

    public void createExtentTestSuite(String suiteName, String description) {
        var extentTest = extentReports.createTest(suiteName, description);
        extentTestSuiteMap.put(suiteName, extentTest);
    }

    public Map<String, ExtentTest> getExtentTestSuiteMap() {
        return extentTestSuiteMap;
    }

    public Map<String, ExtentTest> getExtentTestCaseMap() {
        return extentTestCaseMap;
    }


}
