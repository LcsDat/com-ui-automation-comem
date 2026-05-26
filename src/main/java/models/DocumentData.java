package models;

import utilities.data.ListValues;

import java.util.List;

public class DocumentData {
    @ListValues
    private List<String> list;
    @ListValues
    private List<String> documentName;
    private String dateFrom;
    private String dateTo;
    private String category;

    public List<String> getList() {
        return list;
    }

    public List<String> getDocumentName() {
        return documentName;
    }

    public String getCategory() {
        return category;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }
}
