package cores;

import utilities.CommonUtils;
import utilities.LocaleManager;

import java.util.List;

public abstract class BaseComponent<T extends BaseComponent<T>> {
    protected BrowserDriver driver;

    public BaseComponent(BrowserDriver driver) {
        this.driver = driver;
    }

    @SuppressWarnings("unchecked")
    public T self() {
        return (T) this;
    }

    public T click(String locator) {
        driver.click(locator);
        return self();
    }

    public T pause(long seconds) {
        CommonUtils.pause(seconds);
        return self();
    }
    public T setText(String locator, String value) {
        driver.setText(locator, value);
        return self();
    }

    public T hover(String locator) {
        driver.moveToElement(locator);
        return self();
    }

    public String getDomAttribute(String locator, String attributeValue) {
        return driver.getDomAttribute(locator, attributeValue);
    }

    public String getText(String locator) {
        return driver.getText(locator);
    }

    public boolean isDisplayed(String locator) {
        return driver.isDisplayed(locator);
    }

    public List<UIElement> getElements(String locator) {
        return driver.findElements(locator);
    }

    protected String locale(String key) {
        return LocaleManager.get(key);
    }
}
