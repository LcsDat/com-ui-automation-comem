package utilities.data;

public class DataReaderFactory {

    public enum FileType {EXCEL}

    public static DataReader getReader(FileType fileType) {
        switch (fileType) {
            case EXCEL -> {
                return new ExcelDataReader();
            }

            default -> throw new IllegalArgumentException("Unknown file type: " + fileType);
        }
    }
}
