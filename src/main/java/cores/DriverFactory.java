package cores;

import org.openqa.selenium.WebDriver;

import java.time.Duration;

public class DriverFactory {

    public static WebDriver createWebDriver(Browser browser) {
        WebDriver driver = switch (browser) {
            case FIREFOX          -> browser.createFirefoxDriver();
            case CHROME           -> browser.createChromeDriver();
            case EDGE             -> browser.createEdgeDriver();
            case HEADLESS_FIREFOX -> browser.createHeadlessFirefoxDriver();
            case HEADLESS_CHROME  -> browser.createHeadlessChromeDriver();
            case HEADLESS_EDGE    -> browser.createHeadlessEdgeDriver();
        };

        if (!browser.name().startsWith("HEADLESS")) {
            driver.manage().window().maximize();
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(Constants.LONG_TIMEOUT));
        return driver;
    }
}
