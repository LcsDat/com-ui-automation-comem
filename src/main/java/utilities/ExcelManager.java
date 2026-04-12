package utilities;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class ExcelManager {

    String filePath;
    String fileName;
    String sheetName;

    public ExcelManager(String filePath, String fileName, String sheetName) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.sheetName = sheetName;
    }

    //Read Excel methods
    public Workbook getWorkbook() {
        Workbook workbook = null;
        try (var file = new FileInputStream(filePath + File.separator + fileName)) {
            workbook = new XSSFWorkbook(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }

    public Sheet getSheet() {
        return getWorkbook().getSheet(sheetName);
    }

    public Row getRow(int rowNum) {
        var sheet = getSheet();
        var row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
        }
        return row;
    }

    public Cell getCell(int rowNum, int colNum) {


        return getRow(rowNum).getCell(colNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
    }

}
