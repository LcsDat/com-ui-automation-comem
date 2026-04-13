package cores;

import org.openqa.selenium.*;

public class UIElement {

    private WebElement element;
    private BrowserDriver customDriver;

    public UIElement(BrowserDriver browserDriver, String locator) {
        customDriver = browserDriver;
        element = customDriver.findNativeElement(locator);
    }

    public UIElement(BrowserDriver browserDriver, String locator, String... varargs) {
        customDriver = browserDriver;
        element = customDriver.findNativeElement(locator, varargs);
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
