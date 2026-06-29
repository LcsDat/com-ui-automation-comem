package components;

import cores.BaseComponent;
import cores.BrowserDriver;
import io.qameta.allure.Step;

public class DatePickerComponent extends BaseComponent<DatePickerComponent> {

    private static final String INPUT_FIELD  = "%s//input";
    private static final String CALENDAR_DAY = "%s//td[normalize-space()='%s' and not(contains(@class,'disabled'))]";
    private static final String NEXT_BUTTON  = "%s//*[contains(@class,'next') or contains(@aria-label,'Next')]";
    private static final String PREV_BUTTON  = "%s//*[contains(@class,'prev') or contains(@aria-label,'Previous')]";
    private static final String MONTH_LABEL  = "%s//*[contains(@class,'month') or contains(@class,'caption')]";

    public DatePickerComponent(BrowserDriver driver) {
        super(driver);
    }

    @Step("Type date '{date}'")
    public DatePickerComponent typeDate(String locator, String date) {
        click(String.format(INPUT_FIELD, locator));
        setText(String.format(INPUT_FIELD, locator), date);
        return self();
    }

    @Step("Open calendar")
    public DatePickerComponent openCalendar(String locator) {
        click(String.format(INPUT_FIELD, locator));
        return self();
    }

    @Step("Select day '{day}'")
    public DatePickerComponent selectDay(String locator, String day) {
        click(String.format(CALENDAR_DAY, locator, day));
        return self();
    }

    @Step("Go to next month")
    public DatePickerComponent nextMonth(String locator) {
        click(String.format(NEXT_BUTTON, locator));
        return self();
    }

    @Step("Go to previous month")
    public DatePickerComponent prevMonth(String locator) {
        click(String.format(PREV_BUTTON, locator));
        return self();
    }

    @Step("Get displayed month/year label")
    public String getMonthYearLabel(String locator) {
        return getText(String.format(MONTH_LABEL, locator));
    }

    @Step("Get current input value")
    public String getValue(String locator) {
        return getDomAttribute(String.format(INPUT_FIELD, locator), "value");
    }

    @Step("Clear date input")
    public DatePickerComponent clear(String locator) {
        driver.clear(String.format(INPUT_FIELD, locator));
        return self();
    }
}