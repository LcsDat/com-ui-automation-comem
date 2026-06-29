package cores;

import utilities.LocaleManager;

public class BasePage<T extends BasePage<T>> {
    protected BrowserDriver driver;

    private final ComponentProvider components;

    public BasePage(BrowserDriver driver) {
        this.driver     = driver;
        this.components = new ComponentProvider(driver);
    }

    public ComponentProvider component() {
        return components;
    }

    protected String locale(String key) {
        return LocaleManager.get(key);
    }
}