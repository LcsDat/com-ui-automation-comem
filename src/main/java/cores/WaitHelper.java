package cores;

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

    private UIElement resolveElement(String locator){
        return new UIElement(driver, locator);
    }

    private UIElement resolveElement(String locator, String... varargs){
        return new UIElement(driver, locator, varargs);
    }

    public Boolean waitUntilInvisible(String locator) {
        return wait.until(ExpectedConditions.invisibilityOf(resolveElement(locator).getElement()));
    }

    public Boolean waitUntilInvisible(String locator, String... varargs) {
        return wait.until(ExpectedConditions.invisibilityOf(resolveElement(locator, varargs).getElement()));
    }

    public UIElement waitUntilClickable(String locator) {
        UIElement element = resolveElement(locator);
        wait.until(ExpectedConditions.elementToBeClickable(element.getElement()));
        return element;
    }

    public UIElement waitUntilClickable(String locator, String... varargs) {
        UIElement element = resolveElement(locator, varargs);
        wait.until(ExpectedConditions.elementToBeClickable(element.getElement()));
        return element;
    }

    public UIElement waitUntilVisible(String locator) {
        UIElement element = resolveElement(locator);
        wait.until(ExpectedConditions.visibilityOf(element.getElement()));
        return element;
    }

    public UIElement waitUntilVisible(String locator, String... varargs) {
        UIElement element = resolveElement(locator, varargs);
        wait.until(ExpectedConditions.visibilityOf(element.getElement()));
        return element;
    }
}
