package pages;

import cores.BasePage;
import cores.BrowserDriver;

public class StoreLocatorPage extends BasePage<StoreLocatorPage> {

    private static final String STORE_LIST = "//div[contains(@class,'store')] | //div[contains(@class,'address')]";
    private static final String HOTLINE = "//a[contains(@href,'tel:')]";

    public StoreLocatorPage(BrowserDriver driver) {
        super(driver);
    }

    public String getCurrentUrl() {
        return driver.getDriver().getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getPageTitle();
    }

    public boolean isStoreListDisplayed() {
        return driver.isDisplayed(STORE_LIST);
    }

    public boolean isHotlineDisplayed() {
        return driver.isDisplayed(HOTLINE);
    }
}
