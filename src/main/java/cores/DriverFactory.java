package cores;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

import java.time.Duration;

public class DriverFactory {

    public static WebDriver initWebdriver(Browser browser) {
        WebDriver driver = null;

        switch (browser) {
            case FIREFOX -> {
                driver = browser.initFirefoxDriver();
                driver.manage().window().maximize();
            }

            case CHROME -> {
                driver = browser.initChromeDriver();
                driver.manage().window().maximize();
            }

            case EDGE -> {
                driver = browser.initEdgeDriver();
                driver.manage().window().maximize();
            }

            case HEADLESSFIREFOX -> {
                driver = browser.initHeadlessFirefoxDriver();
            }

            case HEADLESSCHROME -> driver = browser.initHeadlessChromeDriver();

            case HEADLESSEDGE -> driver = browser.initHeadlessEdgeDriver();
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(GlobalVariables.LONG_TIMEOUT));
        return driver;
    }

    public static WebsiteDriver initWebsiteDriver(Browser browser) {
        return new WebsiteDriver(browser);
    }
}
