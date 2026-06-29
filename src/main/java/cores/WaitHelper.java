package cores;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitHelper {

    private WebDriverWait wait;
    private BrowserDriver driver;

    public WebDriverWait getWait() {
        return wait;
    }

    public WaitHelper(BrowserDriver driver, Duration timeout) {
        this.driver = driver;
        wait = new WebDriverWait(driver.getDriver(), timeout);
    }

    private By resolveBy(String locator) {
        if (locator.startsWith("/") || locator.startsWith("(")) return By.xpath(locator);
        if (locator.startsWith("#") || locator.startsWith(".")) return By.cssSelector(locator);
        return By.cssSelector(locator);
    }

    private By resolveBy(String locator, String... varargs) {
        return By.xpath(String.format(locator, (Object[]) varargs));
    }

    public Boolean waitUntilInvisible(String locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(resolveBy(locator)));
    }

    public Boolean waitUntilInvisible(String locator, String... varargs) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(resolveBy(locator, varargs)));
    }

    public UIElement waitUntilClickable(String locator) {
        wait.until(ExpectedConditions.elementToBeClickable(resolveBy(locator)));
        return new UIElement(driver, locator);
    }

    public UIElement waitUntilClickable(String locator, String... varargs) {
        wait.until(ExpectedConditions.elementToBeClickable(resolveBy(locator, varargs)));
        return new UIElement(driver, locator, varargs);
    }

    public UIElement waitUntilVisible(String locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(resolveBy(locator)));
        return new UIElement(driver, locator);
    }

    public UIElement waitUntilVisible(String locator, String... varargs) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(resolveBy(locator, varargs)));
        return new UIElement(driver, locator, varargs);
    }
}
