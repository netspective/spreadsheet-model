package org.netspective.io.spreadsheet.model;

import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultTable implements Table
{
    private final Sheet sheet;
    private final WorksheetTemplate worksheetTemplate;
    private final List<TableRow> rows;

    public DefaultTable(final WorksheetTemplate worksheetTemplate, final Sheet sheet, final List<TableRow> rows)
    {
        this.worksheetTemplate = worksheetTemplate;
        this.sheet = sheet;
        this.rows = Collections.unmodifiableList(new ArrayList<TableRow>(rows));
    }

    public List<TableRow> getRows()
    {
        return rows;
    }

    public WorksheetTemplate getTemplate()
    {
        return worksheetTemplate;
    }

    public Sheet getSheet()
    {
        return sheet;
    }
}
