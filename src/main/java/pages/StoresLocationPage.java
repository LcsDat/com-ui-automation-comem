package pages;

import cores.BasePage;
import cores.WebsiteDriver;

public class StoresLocationPage extends BasePage {

    public StoresLocationPage(WebsiteDriver driver) {
        super(driver);
    }

    private static final String BREADCRUMB = "//a[contains(text(),'Hệ Thống  Cửa Hàng Hasaki')]";
    private static final String TOTAL_STORE_WIDGET = "span#total_store";
    private static final String STORES_WIDGET = "div.hethong_col_left";

    public String getBreadcrumbText(){
        return driver.getText(BREADCRUMB);
    }

    public String getTotalStores(){
        return driver.getText(TOTAL_STORE_WIDGET);
    }

    public boolean isStoresDisplayed(){
        return driver.findElement(STORES_WIDGET).isDisplayed();
    }
}
