package pages;

import cores.BasePage;
import cores.WebsiteDriver;

public class FAQPage extends BasePage {
    public FAQPage(WebsiteDriver driver) {
        super(driver);
    }

    private static final String SLOGAN_HEADER = "h1.slogan";
    private static final String STORES_LOCATIONS_LINK = "//div[@id='block_thongtin_hotro']//a[text()='Hệ thống cửa hàng Hasaki trên toàn quốc']";
    private static final String STORE_ITEMS = "//div[@class='title_item_thethong' and contains(text(),'CN %s:')]";
    private static final String EXPANDED_STORE_ITEMS = "//div[@class='title_item_thethong arrow_up' and contains(text(),'CN %s:')]/following-sibling::div";
    private static final String EXPANDED_STORE_ITEMS_ADDRESS = "(" + EXPANDED_STORE_ITEMS + "//div[@class='space_bottom_10'])[1]";
    private static final String EXPANDED_STORE_ITEMS_CLOCK_ICONS = EXPANDED_STORE_ITEMS + "//img[@src='/images/graphics/icon_alarm.svg']";

    public String getSloganHeader(){
        return driver.getText(SLOGAN_HEADER);
    }

    public void navigateStoresLocationPage(){
        driver.click(STORES_LOCATIONS_LINK);
    }

    public void clickStore(String branchNo){
        driver.click(STORE_ITEMS, branchNo);
    }

    public String getFullAddress(String branchNo){
        return driver.getText(EXPANDED_STORE_ITEMS_ADDRESS, branchNo);
    }

    public int getClockQuantity(String branchNo){
        return driver.findElements(EXPANDED_STORE_ITEMS_CLOCK_ICONS, branchNo).size();
    }
}
