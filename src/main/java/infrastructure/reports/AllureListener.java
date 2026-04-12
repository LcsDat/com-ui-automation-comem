package infrastructure.reports;

import cores.BaseTest;
import cores.WebsiteDriver;
import io.qameta.allure.Allure;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class AllureListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        attachScreenshot(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        attachScreenshot(result);
    }

    private void attachScreenshot(ITestResult result) {
        WebsiteDriver driver = BaseTest.webdriverThread.get();
        if (driver == null) return;

        try {
            byte[] screenshotBytes = Base64.getDecoder().decode(driver.takeScreenshotBASE64());
            Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(screenshotBytes), "png");
        } catch (Exception ignored) {
        }
    }
}
