package pages;

import cores.BasePage;
import cores.BrowserDriver;

public class AboutPage extends BasePage<AboutPage> {

    private static final String PAGE_HEADING = "//h1 | //h2[contains(@class,'title')]";
    private static final String BREADCRUMB = "//nav[contains(@class,'breadcrumb')] | //div[contains(@class,'breadcrumb')]";

    public AboutPage(BrowserDriver driver) {
        super(driver);
    }

    public boolean isPageHeadingDisplayed() {
        return driver.isDisplayed(PAGE_HEADING);
    }

    public String getCurrentUrl() {
        return driver.getDriver().getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getPageTitle();
    }
}
