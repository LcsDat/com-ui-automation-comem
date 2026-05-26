package utilities.data;

import cores.Constants;
import org.apache.poi.ss.usermodel.*;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExcelDataReader implements DataReader {

    @Override
    public Object[][] readData(String filePath, String sheetName) {
        return readData(filePath, sheetName, null, null);
    }

    @Override
    public Object[][] readData(String filePath, String sheetName, String scenario) {
        return readData(filePath, sheetName, scenario, null);
    }

    @Override
    public <T> Object[][] readData(String filePath, String sheetName, String scenario, Class<T> clazz) {
        List<Object[]> rows = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new File(filePath))) {
            Sheet sheet = getSheet(workbook, sheetName);
            Row headerRow = sheet.getRow(0);

            List<String> headers = parseHeaders(headerRow);

            int scenarioColumnIndex = findScenarioIndex(headers);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                if (isSkippedRow(row, scenarioColumnIndex, scenario)) continue;

                Object rowData = clazz != null ? mapRowToPojo(row, headers, clazz) : mapRowToMap(row, headers);

                rows.add(new Object[]{rowData});
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel: " + filePath, e);
        }

        return rows.toArray(new Object[0][]);
    }

    //    ------------------------------------------------
//    PRIVATE - sheet helpers
//    ------------------------------------------------
    private Sheet getSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) throw new IllegalArgumentException("Sheet not found: " + sheetName);
        return sheet;
    }

    //    ------------------------------------------------
//    PRIVATE - header helpers
//    ------------------------------------------------
    private List<String> parseHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        int columnCount = headerRow.getLastCellNum();
        for (int i = 0; i < columnCount; i++) {
            Cell cell = headerRow.getCell(i);
            String raw = (cell != null) ? cell.getStringCellValue() : "";
            String normalized = normalizeHeader(raw);

            headers.add(normalizeHeader(raw));
        }


        return headers;
    }

    private String normalizeHeader(String raw) {
        String trimmed = raw.trim().toLowerCase();
        String[] words = trimmed.split("[\\s_]+");
        if (words.length == 0) return trimmed;
        var sb = new StringBuilder(words[0]);
        for (int i = 1; i < words.length; i++) {
            sb.append(Character.toUpperCase(words[i].charAt(0)));
            sb.append(words[i].substring(1));
        }

        return sb.toString();
    }

    private int findScenarioIndex(List<String> headers) {
        return headers.indexOf("scenario");
    }

    private boolean isSkipped(String header) {
        return header.startsWith("#") || header.isBlank();
    }

//    ------------------------------------------------
//    PRIVATE - row helpers
//    ------------------------------------------------

    private boolean isSkippedRow(Row row, int scenarioColumnIndex, String scenario) {
        if (scenario == null || scenarioColumnIndex == -1) return false;
        String rowScenario = getCellValue(row.getCell(scenarioColumnIndex));
        return !rowScenario.equals(scenario);
    }

    private Map<String, String> mapRowToMap(Row row, List<String> headers) {
        Map<String, String> rowData = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (isSkipped(header)) continue;
            rowData.put(header, getCellValue(row.getCell(i)));
        }
        return rowData;
    }

    private <T> T mapRowToPojo(Row row, List<String> headers, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                if (isSkipped(header)) continue;
                setField(instance, header, getCellValue(row.getCell(i)), clazz);
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map row to " + clazz.getSimpleName());
        }
    }

    private <T> void setField(T instance, String fieldName, String value, Class<T> clazz) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            if (field.isAnnotationPresent(ListValues.class)) {
                String delimeter = field.getAnnotation(ListValues.class).delimeter();

                if (!delimeter.isEmpty() && field.getType() == List.class){
                    List<String> list = Arrays.stream(value.split(Pattern.quote(delimeter)))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
                    field.set(instance, list);
                    return;
                }
            }
            field.set(instance, value);
        } catch (NoSuchFieldException e) {
            System.out.printf("Warning: field '%s' not found in %s, shipping%n", fieldName, clazz.getSimpleName());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field: " + fieldName, e);
        }
    }

//    ------------------------------------------------
//    PRIVATE - cell helpers
//    ------------------------------------------------

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING -> {
                return cell.getStringCellValue();
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell))
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                double value = cell.getNumericCellValue();
                return (value == Math.floor(value)) ? String.valueOf((long) value) : String.valueOf(value);
            }
            case BOOLEAN -> {
                return String.valueOf(cell.getBooleanCellValue());
            }

            case FORMULA -> {
                return cell.getCellFormula();
            }

            default -> {
                return "";
            }

        }
    }

    @DataProvider(name = "excel")
    public Object[][] fromExcel(Method method) {
        ExcelData annotation = method.getAnnotation(ExcelData.class);
        return readData(Constants.RESOURCES_PATH
                + File.separator + annotation.file(), annotation.sheet(), annotation.scenario(), annotation.dataClass());
    }
}
