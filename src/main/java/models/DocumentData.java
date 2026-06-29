package models;

import lombok.Getter;
import utilities.data.ListValues;

import java.util.List;

@Getter
public class DocumentData {
    @ListValues
    private List<String> list;
    @ListValues
    private List<String> documentName;
    private String dateFrom;
    private String dateTo;
    private String category;
}
