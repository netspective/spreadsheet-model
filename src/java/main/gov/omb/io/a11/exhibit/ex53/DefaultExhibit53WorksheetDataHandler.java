package gov.omb.io.a11.exhibit.ex53;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.consumer.DefaultWorksheetDataHandler;
import org.netspective.io.spreadsheet.consumer.WorksheetDataHandler;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;

import java.util.List;

public class DefaultExhibit53WorksheetDataHandler implements WorksheetDataHandler
{
    private WorksheetDataHandler delegate = new DefaultWorksheetDataHandler(9, 2, 17, new int[] { 2, 3 });

    public Result process(final WorksheetTemplate worksheetTemplate, final Sheet sheet, final Row dataRow)
    {
        return delegate.process(worksheetTemplate, sheet, dataRow);
    }

    public Table createTable(final WorksheetTemplate worksheetTemplate, final Sheet sheet, final List<TableRow> tableRows)
    {
        return delegate.createTable(worksheetTemplate, sheet, tableRows);
    }
}
