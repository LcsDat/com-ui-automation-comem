package utilities.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CsvDataReader {

    public List<String> readHeaders(String csvPath) {
        try {
            String content = readContent(Path.of(csvPath));
            List<List<String>> records = parseCsv(content);
            if (records.isEmpty()) return Collections.emptyList();
            return records.get(0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV headers: " + csvPath, e);
        }
    }

    public List<Map<String, String>> readAll(String csvPath) {
        try {
            String content = readContent(Path.of(csvPath));
            return parseToMaps(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV: " + csvPath, e);
        }
    }

    private String readContent(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("CSV file not found: " + path);
        }
        String content = Files.readString(path, StandardCharsets.UTF_8);
        if (!content.isEmpty() && content.charAt(0) == '﻿') {
            content = content.substring(1);
        }
        return content;
    }

    private List<Map<String, String>> parseToMaps(String content) {
        List<List<String>> records = parseCsv(content);
        if (records.isEmpty()) return Collections.emptyList();

        List<String> headers = records.get(0);
        List<Map<String, String>> result = new ArrayList<>();

        for (int i = 1; i < records.size(); i++) {
            List<String> rec = records.get(i);
            if (rec.size() == 1 && rec.get(0).isEmpty()) continue;

            Map<String, String> row = new LinkedHashMap<>();
            for (int c = 0; c < headers.size(); c++) {
                String value = c < rec.size() ? rec.get(c) : "";
                row.put(headers.get(c), value);
            }
            result.add(row);
        }
        return result;
    }

    private List<List<String>> parseCsv(String content) {
        List<List<String>> records = new ArrayList<>();
        List<String> current = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        int len = content.length();

        while (i < len) {
            char c = content.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < len && content.charAt(i + 1) == '"') {
                        field.append('"');
                        i += 2;
                    } else {
                        inQuotes = false;
                        i++;
                    }
                } else {
                    field.append(c);
                    i++;
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                    i++;
                } else if (c == ',') {
                    current.add(field.toString());
                    field.setLength(0);
                    i++;
                } else if (c == '\r') {
                    i++;
                } else if (c == '\n') {
                    current.add(field.toString());
                    field.setLength(0);
                    records.add(current);
                    current = new ArrayList<>();
                    i++;
                } else {
                    field.append(c);
                    i++;
                }
            }
        }

        if (!field.isEmpty() || !current.isEmpty()) {
            current.add(field.toString());
            records.add(current);
        }
        return records;
    }
}
