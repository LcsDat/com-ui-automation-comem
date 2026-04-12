package pages;

import cores.BasePage;
import cores.WebsiteDriver;
import org.openqa.selenium.Keys;

public class ProductDetailsPage extends BasePage {

    public ProductDetailsPage(WebsiteDriver driver) {
        super(driver);
    }

    private static final String INCREASE_QTY_BUTTON = "button[aria-label='Increase btn']";
    private static final String ADD_TO_CART_BUTTON = "//div[text()='Giỏ hàng']";
    private static final String DECREASE_QTY_BUTTON = "button[aria-label='Descrease btn']";
    private static final String CART_BUTTON = "//span[text()='Cart Icon']/ancestor::button";
    private static final String QUANTITY_INPUT = "input[name='qty']";

    private void decreaseProductQuantityToFour() {
        int count = 1;
        while (count <= 100) {
            driver.click(DECREASE_QTY_BUTTON);
            if (Integer.parseInt(driver.getDomAttribute(QUANTITY_INPUT, "value")) == 4) break;
            count++;
        }
    }

    private void setQuantityInput(int quantity) {

        String quantityStr = String.valueOf(quantity);

        // Validate input
        if (quantity >= 100 || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be between 1 and 99");
        }

        // Skip if default quantity
        if (quantity == 1) {
            return;
        }

        String os = System.getProperty("os.name").toLowerCase();

        // Click and position cursor at end
        driver.click(QUANTITY_INPUT);
        if (os.contains("mac")) {
            //Note that when using Mac, need to use different key to modify the quantity
            driver.sendKeys(QUANTITY_INPUT, Keys.END);
        } else {
            driver.sendKeys(QUANTITY_INPUT, Keys.CONTROL, Keys.RIGHT);
        }

        if (quantity < 10) {
            // Single digit (2-9): append digit, delete the '1'
            driver.setText(QUANTITY_INPUT, quantityStr);
            driver.sendKeys(QUANTITY_INPUT, Keys.LEFT, Keys.BACK_SPACE);

        } else if (quantity == 10) {
            // Special case: just append '0'
            driver.setText(QUANTITY_INPUT, "0");

        } else if (quantity % 10 == 0) {
            // Multiples of 10 (20, 30...90): replace '1' with tens digit, append '0'
            driver.setText(QUANTITY_INPUT, String.valueOf(quantityStr.charAt(0)));
            driver.sendKeys(QUANTITY_INPUT, Keys.LEFT, Keys.BACK_SPACE, Keys.RIGHT);
            driver.setText(QUANTITY_INPUT, "0");

        } else {
            // Two digits (11-99): append first digit, delete '1', append second digit
            driver.setText(QUANTITY_INPUT, String.valueOf(quantityStr.charAt(0)));
            driver.sendKeys(QUANTITY_INPUT, Keys.LEFT, Keys.BACK_SPACE);
            driver.sendKeys(QUANTITY_INPUT, Keys.CONTROL, Keys.END);
            driver.setText(QUANTITY_INPUT, String.valueOf(quantityStr.charAt(1)));
        }
    }

    /**
     * Hard method to increase qty one by one.
     */
    public void setProductQty() {
        driver.waitToBeClickable(INCREASE_QTY_BUTTON).click();
    }

    /**
     * Dynamic method to increase product quantity
     */
    public void setProductQty(int quantity) {
        /* Mặc định số lượng 1
         * Khi đổi số lượng vd 2 -> điền 2 sau số 1 -> xoá số 1
         * Với số lượng hàng chục, thêm số 0 ở phía sau
         * Khi muốn thay đổi lượng hàng chục, vd lúc đầu 10 và muốn đổi thành 20 -> điền 2 trước 1 -> xoá 1
         * Không đặt hàng số lượng quá 50 (mock req ngày 25/15/2026)
         */

        int currentQuantity = Integer.parseInt(driver.getDomAttribute(QUANTITY_INPUT, "value"));

        if (currentQuantity > 0 && currentQuantity < 10) {
            setQuantityInput(quantity);
        }

        if (currentQuantity >= 10) {
            decreaseProductQuantityToFour();
            setQuantityInput(quantity);
        }
    }

    public void addProductToCart() {
        driver.click(ADD_TO_CART_BUTTON);
    }

    /**
     * Hard method to decrease qty one by one.
     */
    public void decreaseProductQty() {
        driver.click(DECREASE_QTY_BUTTON);
    }

    public void clickToCart() {
        driver.click(CART_BUTTON);
    }
}

