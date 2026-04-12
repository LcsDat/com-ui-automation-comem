package cores;

import org.openqa.selenium.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class WebsiteElement {

    private WebElement element;
    private WebsiteDriver customDriver;

    public WebsiteElement(WebsiteDriver websiteDriver, String locator) {
        customDriver = websiteDriver;
        element = customDriver.findDefaultWebElement(locator);
    }

    public WebsiteElement(WebsiteDriver websiteDriver, String locator, String... varargs) {
        customDriver = websiteDriver;
        element = customDriver.findDefaultWebElement(locator, varargs);
    }

    public WebElement getElement() {
        return element;
    }

    public void click() {
        element.click();
    }

    public void setText(String value) {
        element.sendKeys(value);
    }

    public String getText() {
        return element.getText();
    }

    public String getDomAttribute(String attributeValue) {
        return element.getDomAttribute(attributeValue);
    }

    public boolean isDisplayed() {
        return element.isDisplayed();
    }


    public String getCssValue(String value) {
        return element.getCssValue(value);
    }

    public boolean isEnabled(){
        return element.isEnabled();
    }

    public void clear(){
        element.clear();
    }
}
