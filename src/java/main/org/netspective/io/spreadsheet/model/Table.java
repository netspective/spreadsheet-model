package org.netspective.io.spreadsheet.model;

import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;

import java.util.List;

public interface Table
{
    public WorksheetTemplate getTemplate();
    public Sheet getSheet();
    public List<TableRow> getRows();
}
