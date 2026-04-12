package pages;

import cores.WebsiteDriver;
import cores.WebsiteElement;
import org.openqa.selenium.NoSuchElementException;

import java.util.List;

public class HomePage extends HomeProductCommons {

    public HomePage(WebsiteDriver driver) {
        super(driver);
    }


    private static final String LOGIN_LINK = "//a[text()='Đăng nhập' and @id='hskLoginButton']";

    private static final String POPUP_CANCEL_BUTTON = "onesignal-slidedown-cancel-button";
    private static final String COOKIES_CANCEL_BUTTON = "rejectCookies";

    private static final String IN_CART_QUANTITY = "span.counter_number";
    private static final String CART_BUTTON = "span.counter_number";
    private static final String HOMEPAGE_LINK = "//a[@aria-label='Homepage']";
    private static final String SEARCH_BAR = "input_search";
    private static final String SEARCH_DROPDOWN_ITEMS = "//div[@id='suggestion_products']//h2";
    private static final String SIGNIN_LABEL = "#btn-login";
    private static final String FAQ_LINK = "//div[@class='item_header']";
    private static final String STORES_LOCATION_LINK = "div.item_header.item_header_hethong";
    private static final String ACCOUNT_LINK = "//a[normalize-space()='Tài khoản']";
    private static final String YOUR_ACCOUNT_ITEM = "//a[normalize-space()='Tài khoản của bạn']";


    public void navigateFAQPage() {
        driver.click(FAQ_LINK);
    }

    public void navigateToStoresLocationPage() {
        driver.click(STORES_LOCATION_LINK);
    }

    public void setTextToSearch(String value) {
        driver.setText(SEARCH_BAR, value);
    }

    public void clickProductFromSearchDropdown(String productName) {
        List<WebsiteElement> list = driver.findElements(SEARCH_DROPDOWN_ITEMS);
        if (list.isEmpty()) throw new NoSuchElementException("Unable to find the element");
        else {
            if (productName.length() != list.get(0).getText().length()) list.get(0).click();
            else list
                    .stream()
                    .filter(e -> e.getText().equals(productName))
                    .findFirst()
                    .get().click();
        }
    }

    public void chooseProductFromSearchDropdown(String productName) {
        setTextToSearch(productName);
        clickProductFromSearchDropdown(productName);
    }

    /**
     * - This method is checking the cart quantity <b style='color:yellow'>AFTER</b> log in to the page, for refresh test setup
     * <p>
     * - The <b style='color:yellow'>CURRENT</b> user position is in HomePage
     * <p>
     * - If the quantity is bigger than 0, remove all products in the cart
     */
    public void removeProductFromCart() {
        refreshPage();
        if (!getCartQuantity().equals("0")) {
            clickToCart();
            sleepInSecond(2);

            int size = driver.findElements("//tbody/tr").size();
            int i = 1;
            if (size != 0) {
                while (i < size) {
                    sleepInSecond(2);
                    driver.waitToBeClickable("(//tbody/tr//button[text()='Xóa'])[1]").click();
                    i++;
                }
            }
            navigateToHomepage();
        }
    }


    public void navigateToHomepage() {
        driver.waitToBeClickable(HOMEPAGE_LINK).click();
    }

    public String getCartQuantity() {
        return driver.getText(IN_CART_QUANTITY);
    }

    public void clickToCart() {
        driver.click(CART_BUTTON);
    }

    public void clickToLoginLink() {
        driver.click(LOGIN_LINK);
    }

    public void cancelPopup() {
        driver.waitToBeVisible(POPUP_CANCEL_BUTTON).click();
    }

    public void cancelCookie() {
        driver.click(COOKIES_CANCEL_BUTTON);
    }
}
