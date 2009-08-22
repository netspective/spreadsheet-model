package org.netspective.io.spreadsheet.consumer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;

import java.util.List;

public interface WorksheetDataHandler
{
    public interface Result
    {
        public boolean handle();
        public boolean isLast();
        public TableRow createTableRow();
    }

    public Result process(final WorksheetTemplate worksheetTemplate, final Sheet sheet, final Row dataRow);
    public Table createTable(final WorksheetTemplate worksheetTemplate, final Sheet sheet, final List<TableRow> tableRows);
}
