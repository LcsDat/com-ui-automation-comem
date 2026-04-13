package cores;

import org.openqa.selenium.WebDriver;

import java.time.Duration;

public class DriverFactory {

    public static WebDriver createWebDriver(Browser browser) {
        WebDriver driver = null;

        switch (browser) {
            case FIREFOX -> {
                driver = browser.createFirefoxDriver();
                driver.manage().window().maximize();
            }

            case CHROME -> {
                driver = browser.createChromeDriver();
                driver.manage().window().maximize();
            }

            case EDGE -> {
                driver = browser.createEdgeDriver();
                driver.manage().window().maximize();
            }

            case HEADLESS_FIREFOX -> {
                driver = browser.createHeadlessFirefoxDriver();
            }

            case HEADLESS_CHROME -> driver = browser.createHeadlessChromeDriver();

            case HEADLESS_EDGE -> driver = browser.createHeadlessEdgeDriver();

            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Constants.LONG_TIMEOUT));
        return driver;
    }
}
