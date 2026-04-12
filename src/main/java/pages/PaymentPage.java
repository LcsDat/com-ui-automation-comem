package pages;

import cores.BasePage;
import cores.WebsiteDriver;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;

public class PaymentPage extends BasePage {
    public PaymentPage(WebsiteDriver driver) {
        super(driver);
    }

    private static final String COMMON_BUTTON = "//h2[text()='%s']/following-sibling::div//button[text()='%s']";
    private static final String COMMON_ADD_NEW_ADDRESS_INPUT = "//input[@placeholder='%s']";
    private static final String COMMON_VALIDATION_MESSAGE_INPUT = COMMON_ADD_NEW_ADDRESS_INPUT + "/parent::div/following-sibling::p";
    private static final String COMMON_ADD_NEW_ADDRESS_DROPDOWN = "//button[text()='%s']";
    private static final String COMMON_VALIDATION_MESSAGE_DROPDOWN = COMMON_ADD_NEW_ADDRESS_DROPDOWN + "/following-sibling::p";
    private static final String CHANGE_ADDRESS_BUTTON = "//span[text()='Thêm địa chỉ mới']";
    private static final String CONTINUE_BUTTON = "//h2[text()='%s']/following-sibling::form//button[text()='Tiếp tục']";
    private static final String CONTINUE_BUTTON_2nd = "//h2[text()='Thêm địa chỉ mới']/parent::div/following-sibling::form//descendant::button[text()='Tiếp tục']";
    private static final String PAYMENT_METHOD_RADIO_OPTION = "//h2[text()='Hình thức thanh toán']/following-sibling::form//p[text()='%s']";
    private static final String DROPDOWN_SEARCH_INPUT = "//input[contains(@placeholder, 'Tìm kiếm')]";
    private static final String DROPDOWN_SEARCH_INPUT_OPTION = "//div[contains(text(), '%s')]";
    private static final String STREET_NUMBER_INPUT = "button[name='address']";
    private static final String STREET_NUMBER_CONTINUE_BUTTON = "//span[text()='Sửa vị trí trên bản đồ']/parent::div//following-sibling::div//button[text()='Tiếp tục']";
    private static final String PAYMENT_OPTION_BY_NUMBER = "payment-option-%s";
    private static final String PAYMENT_OPTION_BY_NAME = "//h2[text()='Hình thức thanh toán']/following-sibling::form//p[text()='%s']";
    private static final String ADDRESS_OPTION = "//h2[text()='Địa chỉ nhận hàng']/following-sibling::form//p[normalize-space()='%s']";
    private static final String DELETE_ADDRESS_BUTTON = "(" + ADDRESS_OPTION + "/following-sibling::div/child::button)[1]";
    private static final String DELETE_ADDRESS_CONFIRM_BUTTON = "//button[text()='Xác nhận']";
    private static final String SUCCESS_MESSAGE = "//div[text()='%s']";
    private static final String CLOSE_POPUP_BUTTON = "//span[text()='Close']/parent::button";
    private static final String CHANGE_PRODUCT_LINK = "//a[text()='Thay đổi']";

    public void changeProduct(){
        driver.click(CHANGE_PRODUCT_LINK);
    }

    public Boolean waitForMessageInvisible(String message){
        return waitToBeInvisible(SUCCESS_MESSAGE, message);
    }
    /**
     * Delete a delivery address
     * @param addressInfo Include name + phone number
     */
    public void deleteAddress(String addressInfo){
        driver.click(ADDRESS_OPTION, addressInfo);
        driver.click(DELETE_ADDRESS_BUTTON, addressInfo);
        driver.click(DELETE_ADDRESS_CONFIRM_BUTTON);
    }
    public void chooseEdit(String section, String buttonName) {
        driver.click(COMMON_BUTTON, section, buttonName);
    }

    public void clickAddNewAddress() {
        driver.click(CHANGE_ADDRESS_BUTTON);
    }


    public void setTextToNewAddressFields(String fieldName, String value) {
        driver.setText(COMMON_ADD_NEW_ADDRESS_INPUT, value, fieldName);
    }

    public void clickContinue(String popupName) {
//        try {
//            driver.waitToBeClickable(CONTINUE_BUTTON, popupName).click();
//        } catch (NoSuchElementException e){
//            System.out.printf("\n First locator fail: " + CONTINUE_BUTTON + " Try second locator\n", popupName);
//            driver.waitToBeClickable(CONTINUE_BUTTON_2nd, popupName).click();
//        }

        tryClickLocators(new String[]{CONTINUE_BUTTON, CONTINUE_BUTTON_2nd}, popupName);
    }

    public String getCommonValidationMessageInput(String inputName) {
        return driver.getText(COMMON_VALIDATION_MESSAGE_INPUT, inputName);
    }

    public String getCommonValidationMessageDropdown(String dropdownName) {
        return driver.getText(COMMON_VALIDATION_MESSAGE_DROPDOWN, dropdownName);
    }

    private void clickDropdown(String dropdownName) {
        driver.click(COMMON_ADD_NEW_ADDRESS_DROPDOWN, dropdownName);
    }

    private void setTextDropdownSearchField(String value) {
        driver.setText(DROPDOWN_SEARCH_INPUT, value);
    }

    public void chooseCity(String cityName) {
        clickDropdown("Chọn Tỉnh/ TP, Quận/ Huyện");
        setTextDropdownSearchField(cityName);
        driver.click(DROPDOWN_SEARCH_INPUT_OPTION, cityName);
//        driver.click(DROPDOWN_SEARCH_INPUT_OPTION, cityName);
    }

    public void chooseWard(String wardName) {
        clickDropdown("Chọn Phường/ Xã");
        setTextDropdownSearchField(wardName);
        driver.click(DROPDOWN_SEARCH_INPUT_OPTION, wardName);
    }

    public void clickStreetField() {
        driver.waitToBeClickable(STREET_NUMBER_INPUT).click();
    }

    public void setTextStreetField(String value) {
        driver.clear(COMMON_ADD_NEW_ADDRESS_INPUT, "Nhập vị trí của bạn");
        driver.setText(COMMON_ADD_NEW_ADDRESS_INPUT, value, "Nhập vị trí của bạn");
        driver.sendKeys(COMMON_ADD_NEW_ADDRESS_INPUT, Keys.ENTER, "Nhập vị trí của bạn");
    }

    public void clickContinueStreetNumberButton() {
        driver.click(STREET_NUMBER_CONTINUE_BUTTON);
    }

    public String getStreetNumberInputValue(String value){
        return driver.getDomAttribute(COMMON_ADD_NEW_ADDRESS_INPUT, value, "Nhập vị trí của bạn");
    }

    /**
     * Choose option by number
     * @param options From 0 to 4
     */
    public void choosePaymentMethod(int options){
        try {
            driver.click(PAYMENT_OPTION_BY_NUMBER, String.valueOf(options));
        } catch (NoSuchElementException e) {
            System.out.println("Out range of options");
        }

    }

    /**
     * Choose option by name
     * @param paymentName Name of the payment
     */
    public void choosePaymentMethod(String paymentName){
        driver.click(PAYMENT_OPTION_BY_NAME, paymentName);
    }

    public void closePopup(){
        driver.click(CLOSE_POPUP_BUTTON);
    }
}
