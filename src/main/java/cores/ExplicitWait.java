package cores;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ExplicitWait {

    private WebDriverWait wait;
    private WebsiteDriver driver;

    public WebDriverWait getWait() {
        return wait;
    }

    public ExplicitWait(WebsiteDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver.getDriver(), Duration.ofSeconds(GlobalVariables.LONG_TIMEOUT));
    }

    public ExplicitWait(WebsiteDriver driver, Duration timeout) {
        this.driver = driver;
        wait = new WebDriverWait(driver.getDriver(), timeout);
    }

    private WebsiteElement getWebsiteElement(String locator){
        return new WebsiteElement(driver, locator);
    }

    private WebsiteElement getWebsiteElement(String locator, String... varargs){
        return new WebsiteElement(driver, locator, varargs);
    }

    public Boolean waitToBeInvisibleBy(String locator) {
        return wait.until(ExpectedConditions.invisibilityOf(getWebsiteElement(locator).getElement()));
    }

    public Boolean waitToBeInvisibleBy(String locator, String... varargs) {
        return wait.until(ExpectedConditions.invisibilityOf(getWebsiteElement(locator, varargs).getElement()));
    }

    public WebsiteElement waitToBeClickable(String locator) {
        WebsiteElement element = getWebsiteElement(locator);
        wait.until(ExpectedConditions.elementToBeClickable(element.getElement()));
        return element;
    }

    public WebsiteElement waitToBeClickable(String locator, String... varargs) {
        WebsiteElement element = getWebsiteElement(locator, varargs);
        wait.until(ExpectedConditions.elementToBeClickable(element.getElement()));
        return element;
    }

    public WebsiteElement waitToBeVisible(String locator) {
        WebsiteElement element = getWebsiteElement(locator);
        wait.until(ExpectedConditions.visibilityOf(element.getElement()));
        return element;
    }

    public WebsiteElement waitToBeVisible(String locator, String... varargs) {
        WebsiteElement element = getWebsiteElement(locator, varargs);
        wait.until(ExpectedConditions.visibilityOf(element.getElement()));
        return element;
    }
}
