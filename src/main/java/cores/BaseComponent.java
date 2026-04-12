package cores;

import org.openqa.selenium.By;

public abstract class BaseComponent<T extends BaseComponent<T>> {
    protected WebsiteDriver driver;

    public BaseComponent(WebsiteDriver driver) {
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

    public T sleep(int seconds) {
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {}
        return self();
    }
    public T input(String locator, String value) {
        driver.setText(locator, value);
        return self();
    }
}
