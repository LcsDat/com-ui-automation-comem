package components;

import cores.BaseComponent;
import cores.BrowserDriver;
import io.qameta.allure.Step;

public class TableComponent extends BaseComponent<TableComponent> {

    private static final String ROWS          = "%s//tbody/tr";
    private static final String HEADER_CELLS  = "%s//thead//th";
    private static final String CELL          = "(%s//tbody/tr)[%d]/td[%d]";
    private static final String ROW_BY_VALUE  = "%s//tbody/tr[td[normalize-space()='%s']]";
    private static final String CELL_IN_ROW   = "%s//tbody/tr[td[normalize-space()='%s']]/td[%d]";
    private static final String ROW_INDEX     = "(%s//tbody/tr)[%d]";

    public TableComponent(BrowserDriver driver) {
        super(driver);
    }

    @Step("Get total row count")
    public int getRowCount(String locator) {
        return getElements(String.format(ROWS, locator)).size();
    }

    @Step("Get column count")
    public int getColumnCount(String locator) {
        return getElements(String.format(HEADER_CELLS, locator)).size();
    }

    @Step("Get cell value at row {row}, column {col}")
    public String getCellValue(String locator, int row, int col) {
        return getText(String.format(CELL, locator, row, col));
    }

    @Step("Click row at index {row}")
    public TableComponent clickRow(String locator, int row) {
        click(String.format(ROW_INDEX, locator, row));
        return self();
    }

    @Step("Check if row with value '{value}' is present")
    public boolean isRowPresent(String locator, String value) {
        return isDisplayed(String.format(ROW_BY_VALUE, locator, value));
    }

    @Step("Get cell value in column {col} of row containing '{rowKey}'")
    public String getCellValueByRowKey(String locator, String rowKey, int col) {
        return getText(String.format(CELL_IN_ROW, locator, rowKey, col));
    }
}