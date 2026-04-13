package cores;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public enum Browser {
    CHROME, FIREFOX, EDGE, HEADLESS_CHROME, HEADLESS_FIREFOX, HEADLESS_EDGE;

    private ChromeOptions buildChromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (!headless) {
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            options.setExperimentalOption("prefs", prefs);
        } else {
            options.addArguments("--headless=new",
                    "--window-size=1920,1080",
                    "--disable-blink-features=AutomationControlled");
        }

        return options;
    }

    public WebDriver createChromeDriver() {
        return new ChromeDriver(buildChromeOptions(false));
    }

    public WebDriver createFirefoxDriver() {
        return new FirefoxDriver();
    }

    public WebDriver createEdgeDriver() {
        return new EdgeDriver();
    }

    public WebDriver createHeadlessChromeDriver() {
        return new ChromeDriver(buildChromeOptions(true));
    }

    public WebDriver createHeadlessFirefoxDriver() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless",
                "--width=1920",
                "--height=1080");
        return new FirefoxDriver(options);
    }

    public WebDriver createHeadlessEdgeDriver() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--headless", "--start-maximized", "--window-size=1920x1080");
        return new EdgeDriver(options);
    }
}
