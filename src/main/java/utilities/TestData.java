package utilities;

import cores.Constants;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.lang.reflect.Method;

public class TestData {

    private static String resourcePath = Constants.PROJECTPATH
            + File.separator +"src"
            + File.separator + "main"
            + File.separator +"resources";

    //Default file
    private String fileName = "TestCaseReference.xlsx";

    //Test data Login
    private String loginDataTC01 = "Login-tc01";
    private String loginDataTC02 = "Login-tc02";

    //Test data Order
    private String orderDataTC01 = "OrderProductInChrome-tc01";

    private ExcelManager getTestCaseFile(String fileName, String sheetName) {
        return new ExcelManager(resourcePath, fileName, sheetName);
    }

    private Object[][] readData(ExcelManager excelManager) {
        int rows = excelManager.getSheet().getLastRowNum();
        int columns = excelManager.getSheet().getRow(0).getLastCellNum();

        Object[][] data = new Object[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                data[row][column] = excelManager.getCell(row + 1,column).toString();
            }
        }

        return data;
    }

    @DataProvider(name = "excel")
    public Object[][] fromExcel(Method method) {
        ExcelData annotation = method.getAnnotation(ExcelData.class);
        return readData(getTestCaseFile(annotation.file(), annotation.sheet()));
    }

    @DataProvider(name = "Login-tc01")
    public Object[][] loginTC01(Method method) {
//        String filePath = "D:\\Work\\Automation\\IntelliJ\\Automation-Testing\\src\\main\\resources";
//        String fileName = "TestCaseReference.xlsx";
//        String sheetName = "Login-tc01";
        var excelManager = getTestCaseFile(fileName, loginDataTC01);

//        System.out.println("sheet name " + excelManager.getSheet().toString());
//
//
//        int rows = excelManager.getSheet().getLastRowNum();
//        int columns = excelManager.getSheet().getRow(0).getLastCellNum();
//
//        Object[][] data = new Object[rows][columns];
//
//        for (int row = 0; row < rows; row++) {
//            for (int column = 0; column < columns; column++) {
//                data[row][column] = excelManager.getCell(row + 1,column).toString();
//            }
//        }

        return readData(excelManager);
    }

    @DataProvider(name = "Login-tc02")
    public Object[][] loginTC02(Method method) {
//        String filePath = "D:\\Work\\Automation\\IntelliJ\\Automation-Testing\\src\\main\\resources";
//        String fileName = "TestCaseReference.xlsx";
//        String sheetName = "Login-tc02";
        var excelManager = getTestCaseFile(fileName, loginDataTC02);

//        int rows = excelManager.getSheet().getLastRowNum();
//        int columns = excelManager.getSheet().getRow(0).getLastCellNum();
//
//        Object[][] data = new Object[rows][columns];
//
//        for (int row = 0; row < rows; row++) {
//            for (int column = 0; column < columns; column++) {
//                data[row][column] = excelManager.getCell(row + 1,column).toString();
//            }
//        }

        return readData(excelManager);
    }

    @DataProvider(name = "OrderProductInChrome-tc01")
    public Object[][] orderProductInChromeTC01(Method method) {
//        String filePath = "D:\\Work\\Automation\\IntelliJ\\Automation-Testing\\src\\main\\resources";
//        String fileName = "TestCaseReference.xlsx";
//        String sheetName = "OrderProductInChrome-tc01";
        var excelManager = getTestCaseFile( fileName, orderDataTC01);

//        int rows = excelManager.getSheet().getLastRowNum();
//        int columns = excelManager.getSheet().getRow(0).getLastCellNum();
//
//        Object[][] data = new Object[rows][columns];
//
//        for (int row = 0; row < rows; row++) {
//            for (int column = 0; column < columns; column++) {
//                data[row][column] = excelManager.getCell(row + 1,column).toString();
//            }
//        }

        return readData(excelManager);
    }
}
