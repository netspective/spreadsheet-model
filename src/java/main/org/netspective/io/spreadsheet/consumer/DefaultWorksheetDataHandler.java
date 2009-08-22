package org.netspective.io.spreadsheet.consumer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.model.DefaultTable;
import org.netspective.io.spreadsheet.model.DefaultTableCell;
import org.netspective.io.spreadsheet.model.DefaultTableRow;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultWorksheetDataHandler implements WorksheetDataHandler
{
    private final int firstDataRowNumber;
    private final int lastDataRowNumber;
    private final int firstDataColumnNumber;
    private final int lastDataColumnNumber;
    private final Set<Integer> lastRowIfTheseColumnsAreBlank = new HashSet<Integer>();

    public DefaultWorksheetDataHandler(final int firstDataRowNumber, final int lastDataRowNumber, final int firstDataColumnNumber, final int lastDataColumnNumber)
    {
        this.firstDataRowNumber = firstDataRowNumber;
        this.lastDataRowNumber = lastDataRowNumber;
        this.firstDataColumnNumber = firstDataColumnNumber;
        this.lastDataColumnNumber = lastDataColumnNumber;
    }

    public DefaultWorksheetDataHandler(final int firstDataRowNumber, final int firstDataColumnNumber, final int lastDataColumnNumber, final int[] lastRowIfTheseColumnsAreBlank)
    {
        this.firstDataRowNumber = firstDataRowNumber;
        this.lastDataRowNumber = -1;
        this.firstDataColumnNumber = firstDataColumnNumber;
        this.lastDataColumnNumber = lastDataColumnNumber;

        for(final int rowNum : lastRowIfTheseColumnsAreBlank)
            this.lastRowIfTheseColumnsAreBlank.add(rowNum);
    }

    public Table createTable(final WorksheetTemplate worksheetTemplate, final Sheet sheet, final List<TableRow> tableRows)
    {
        return new DefaultTable(worksheetTemplate, sheet, tableRows);
    }

    public Result process(final WorksheetTemplate worksheetTemplate, final Sheet sheet, final Row dataRow)
    {
        final int rowNum = dataRow.getRowNum();
        if(rowNum < firstDataRowNumber-1)
        {
            return new Result()
            {
                public boolean handle() { return false; }
                public boolean isLast() { return rowNum > Integer.MAX_VALUE-1; }
                public TableRow createTableRow() { return null; }
            };
        }
        else
        {
            int blankCount = 0;
            for(int checkColumn : lastRowIfTheseColumnsAreBlank)
            {
                if(dataRow.getCell(checkColumn-1).getCellType() == Cell.CELL_TYPE_BLANK)
                    blankCount++;
            }

            final boolean isRowConsideredBlank = lastRowIfTheseColumnsAreBlank.size() > 0 && blankCount == lastRowIfTheseColumnsAreBlank.size();

            return new Result()
            {
                public boolean handle() { return ! isRowConsideredBlank; }
                public boolean isLast() { return (lastDataRowNumber > 0 && rowNum == lastDataRowNumber) || isRowConsideredBlank || rowNum > Integer.MAX_VALUE-1; }
                public TableRow createTableRow()
                {
                    final List<TableCell> cellsInRow = new ArrayList<TableCell>();

                    for(final Cell dataCell : dataRow)
                    {
                        final int columnIndex = dataCell.getColumnIndex();
                        if(columnIndex < firstDataColumnNumber-1)
                            continue;

                        final TableCell tc = new DefaultTableCell(worksheetTemplate.getColumnsMapByIndex().get(columnIndex + 1), dataCell);
                        cellsInRow.add(tc);

                        if(columnIndex == lastDataColumnNumber-1)
                            break;
                    }

                    return new DefaultTableRow(dataRow, cellsInRow);
                }
            };
        }
    }
}
