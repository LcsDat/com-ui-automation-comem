package cores;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public enum Browser {
    CHROME, FIREFOX, EDGE, HEADLESSCHROME, HEADLESSFIREFOX, HEADLESSEDGE;

    public WebDriver initChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        return new ChromeDriver(options);
    }

    public WebDriver initFirefoxDriver() {
        return new FirefoxDriver();
    }

    public WebDriver initEdgeDriver() {
        return new EdgeDriver();
    }

    public WebDriver initHeadlessChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new",
                "--window-size=1920,1080",
                "--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        return new ChromeDriver(options);
    }

    public WebDriver initHeadlessFirefoxDriver() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless",
                "--width=1920",
                "--height=1080");
        return new FirefoxDriver(options);
    }

    public WebDriver initHeadlessEdgeDriver() {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--headless", "--start-maximized", "--window-size=1920x1080");
        return new EdgeDriver(options);
    }
}
