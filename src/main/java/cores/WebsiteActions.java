package cores;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;

public class WebsiteActions {
    private Actions actions;
    private WebsiteDriver driver;

    public WebsiteActions(WebsiteDriver driver) {
        this.driver = driver;
        this.actions = new Actions(driver.getDriver(), Duration.ofSeconds(10));
    }

    public WebsiteActions(WebsiteDriver driver, Duration timeout) {
        this.driver = driver;
        this.actions = new Actions(driver.getDriver(), timeout);
    }

    private WebsiteElement getWebsiteElement(String locator){
        return new WebsiteElement(driver, locator);
    }

    private WebsiteElement getWebsiteElement(String locator, String... varargs){
        return new WebsiteElement(driver, locator, varargs);
    }

    public void moveToElement(String locator){
        actions.moveToElement(getWebsiteElement(locator).getElement()).perform();
    }

    public void moveToElement(String locator, String... varargs){
        actions.moveToElement(getWebsiteElement(locator, varargs).getElement()).perform();
    }

    public void click(String locator){
        actions.click(getWebsiteElement(locator).getElement()).perform();
    }

    public void click(String locator, String... varargs){
        actions.click(getWebsiteElement(locator,varargs).getElement()).perform();
    }

    public void doubleClick(String locator){
        actions.doubleClick(getWebsiteElement(locator).getElement()).perform();
    }

    public void doubleClick(String locator, String... varargs){
        actions.doubleClick(getWebsiteElement(locator, varargs).getElement()).perform();
    }
}
