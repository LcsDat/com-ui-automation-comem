import com.sun.tools.javac.Main;
import cores.Browser;
import cores.DriverFactory;
import cores.WebsiteDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class Demo2 extends DemoParent {
    private WebDriver driver;
    private static final String HUB_URL = "http://localhost:4444";

    @BeforeMethod
    @Parameters({"browser"})
    public void setUp(String browser) throws MalformedURLException {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        WebsiteDriver driver1 = DriverFactory.initWebsiteDriver(Browser.CHROME);



//         Set browser capabilities
        switch (browser.toLowerCase()) {
            case "chrome":
                capabilities.setBrowserName("chrome");
                break;
            case "firefox":
                capabilities.setBrowserName("firefox");
                break;
            case "edge":
                capabilities.setBrowserName("MicrosoftEdge");
                break;
            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }

        capabilities.setBrowserName("chrome");

        // Create RemoteWebDriver instance
        driver = new RemoteWebDriver(new URL(HUB_URL), capabilities);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();

        System.out.println("Test started on: " + browser + " browser");
        System.out.println("Thread ID: " + Thread.currentThread().getId());
    }

    @Test
    public void testGoogleSearch() {
        // Navigate to Google
        driver.get("https://www.google.com");

        // Find search box and perform search
        WebElement searchBox = driver.findElement(By.name("q"));
        searchBox.sendKeys("Selenium Grid Tutorial");
        searchBox.submit();

        // Verify results page title contains search term
        String title = driver.getTitle();
        Assert.assertTrue(title.contains("Selenium Grid Tutorial"),
                "Title doesn't contain search term: " + title);

        System.out.println("Test completed successfully on thread: " +
                Thread.currentThread().getId());
    }

    @Test
    public void testSeleniumWebsite() {
        // Navigate to Selenium website
        driver.get("https://www.selenium.dev/");

        // Verify page title
        String title = driver.getTitle();
        Assert.assertTrue(title.contains("Selenium"),
                "Page title doesn't contain 'Selenium': " + title);

        // Find and verify a specific element
        WebElement header = driver.findElement(By.xpath("//h1"));
        Assert.assertTrue(header.isDisplayed(), "Header element is not displayed");

        System.out.println("Selenium website test completed on thread: " +
                Thread.currentThread().getId());
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println("Browser closed for thread: " + Thread.currentThread().getId());
        }
    }
}
