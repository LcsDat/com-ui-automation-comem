package pages;

import cores.BasePage;
import cores.WebsiteDriver;

public class CartPage extends BasePage {

    public CartPage(WebsiteDriver driver) {
        super(driver);
    }


    //            "nav[aria-label='Main'] button.p-0";
    private static final String CART_QUANTITY = "span.counter_number.counter.qty";
    //            "nav[aria-label='Main'] button.p-0 a span:nth-child(3)";
    private static final String SHIP_2H_EXPRESS_BUTTON = "//div[text()='Mua ngay NowFree 2H ']";
    private static final String PROCEED_TO_CART_BUTTON = "//div[text()='Hóa đơn của bạn']/following-sibling::div/button";

    public void shipExpress2h() {
        driver.click(SHIP_2H_EXPRESS_BUTTON);
    }

    public void clickProceedToCart() {
        driver.click(PROCEED_TO_CART_BUTTON);
    }


    /**
     * - This method is checking the cart quantity <b style='color:yellow'>BEFORE</b> log out, for test tear down
     * <p>
     * - The <b style='color:yellow'>CURRENT</b> user position is not in Homepage
     * <p>
     * - If the quantity is bigger than 0, remove all products in the cart
     */
//    public void checkCartQuantity(){
//        if (!(driver.getText(CART_QUANTITY).equals("0"))) {
//            driver.click(CART_BUTTON);
//
//            while (true) {
//                Integer size = driver.findElements("//tbody/tr").size();
//                if (size != 0) {
//                    driver.waitToBeClickable("//tbody/tr//button[text()='Xóa']").click();
//                    driver.waitToBeInvisibleBy("div.animate-spin");
//                } else break;
//            }
//        }
//    }
}
