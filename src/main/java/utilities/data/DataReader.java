package utilities.data;

public interface DataReader {
    Object[][] readData(String filePath, String dataKey);

    Object[][] readData(String filePath, String dataKey, String scenario);

    <T> Object[][] readData(String filePath, String dataKey, String scenario, Class<T> clazz);
}
