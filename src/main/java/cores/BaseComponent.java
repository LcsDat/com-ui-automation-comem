package cores;

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
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {}
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
        return  driver.getDomAttribute(locator, attributeValue);
    }
}
