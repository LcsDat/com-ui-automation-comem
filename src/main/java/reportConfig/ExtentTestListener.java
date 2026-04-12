package reportConfig;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import cores.BaseTest;
import logConfig.Log4j2Manager;
import org.testng.IExecutionListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;


public class ExtentTestListener extends BaseTest implements ITestListener, IExecutionListener {

    private ExtentTest extentLog(ITestResult iTestResult) {
        return extentManager.getExtentTestCaseMap().get(iTestResult.getMethod().getMethodName().replace("_", " "));
    }

    @Override
    public void onFinish(ITestContext iTestContext) {
        ExtentManager.getExtentReports().flush();
    }

    @Override
    public void onTestStart(ITestResult iTestResult) {
        extentLog(iTestResult).info(MarkupHelper.createLabel("TEST CASE START: " + iTestResult.getTestClass() + "." + iTestResult.getName(), ExtentColor.BLUE));
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        var logger = Log4j2Manager.getLogger(iTestResult.getTestClass()).getAssertionPassLogger();
        extentLog(iTestResult).pass(MarkupHelper.createLabel(iTestResult.getTestClass() + "." + iTestResult.getName() + " ====> PASSED", ExtentColor.GREEN));
        logger.info("{}.{} ====> PASSED", iTestResult.getTestClass().getName(), iTestResult.getName());
    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        var logger = Log4j2Manager.getLogger(iTestResult.getTestClass()).getAssertionFailLogger();
        var mediaList = extentLog(iTestResult).getModel().getMedia();

        extentLog(iTestResult).addScreenCaptureFromBase64String(webdriverThread.get().takeScreenshotBASE64());
        extentLog(iTestResult).fail(iTestResult.getTestClass() + "." + iTestResult.getName() + " FAILED \n ====> " + iTestResult.getThrowable().getMessage(), mediaList.get(mediaList.size()-1));

        logger.error("{}.{} ====> FAILED", iTestResult.getTestClass().getName(), iTestResult.getName());
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {
        var logger = Log4j2Manager.getLogger(iTestResult.getTestClass()).getAssertionFailLogger();
        extentLog(iTestResult).skip(MarkupHelper.createLabel(iTestResult.getTestClass() + "." + iTestResult.getName() + " ====> SKIPPED", ExtentColor.ORANGE));
        extentLog(iTestResult).skip(iTestResult.getThrowable(), attachScreenshot());
        logger.error("{}.{} ====> SKIPPED", iTestResult.getTestClass().getName(), iTestResult.getName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        var logger = Log4j2Manager.getLogger(iTestResult.getTestClass()).getAssertionFailLogger();
        extentLog(iTestResult).skip(MarkupHelper.createLabel(iTestResult.getTestClass() + "." + iTestResult.getName() + " ====> SKIPPED WITH PERCENT", ExtentColor.ORANGE));
        extentLog(iTestResult).skip(iTestResult.getThrowable(), attachScreenshot());
        logger.error("{}.{} ====> SKIPPED WITH PERCENT", iTestResult.getTestClass().getName(), iTestResult.getName());
    }
}
