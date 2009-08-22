package org.netspective.io.spreadsheet.validate.cell;

import org.apache.poi.ss.usermodel.Cell;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationContext;

import java.util.List;

public class CellTypeRule implements CellValidationRule
{
    private final String messageCode;
    private final int expectedType;
    private final boolean allowBlankAlso;

    public CellTypeRule(final String messageCode, final int expectedType)
    {
        this.messageCode = messageCode;
        this.expectedType = expectedType;
        this.allowBlankAlso = true;
    }

    public CellTypeRule(final String messageCode, final int expectedType, boolean allowBlankAlso)
    {
        this.messageCode = messageCode;
        this.expectedType = expectedType;
        this.allowBlankAlso = allowBlankAlso;
    }

    public static String getCellTypeName(int type)
    {
        switch(type)
        {
            case Cell.CELL_TYPE_NUMERIC:
                return "Numeric";

            case Cell.CELL_TYPE_STRING:
                return "Text";

            case Cell.CELL_TYPE_FORMULA:
                return "Formula";

            case Cell.CELL_TYPE_BLANK:
                return "Blank";

            case Cell.CELL_TYPE_BOOLEAN:
                return "Boolean";

            case Cell.CELL_TYPE_ERROR:
                return "Error";

            default:
                return "Unknown (" + Integer.toString(type) + ")";
        }        
    }

    public boolean isValid(final ValidationContext vc, final Table table, final TableRow row, final TableCell tableCell, final List<CellValidationMessage> messages)
    {
        final Cell poiCell = tableCell.getCell();
        final int cellType = poiCell.getCellType();
        if(cellType == expectedType || (allowBlankAlso && cellType == Cell.CELL_TYPE_BLANK))
            return true;

        final String expectedTypeName = getCellTypeName(expectedType);
        final String message;

        switch(cellType)
        {
            case Cell.CELL_TYPE_NUMERIC:
                message = String.format("Expected %s data type in %s, instead '%s' is of type %s.", expectedTypeName, vc.getValidationMessageCellLocator(tableCell, false), poiCell.getStringCellValue(), getCellTypeName(cellType));
                break;

            case Cell.CELL_TYPE_STRING:
                message = String.format("Expected %s data type in %s, instead '%s' is of type %s.", expectedTypeName, vc.getValidationMessageCellLocator(tableCell, false), poiCell.getStringCellValue(), getCellTypeName(cellType));
                break;

            case Cell.CELL_TYPE_FORMULA:
                message = String.format("Expected %s data type in %s, instead '%s' is of type %s.", expectedTypeName, vc.getValidationMessageCellLocator(tableCell, false), poiCell.getStringCellValue(), getCellTypeName(cellType));
                break;

            case Cell.CELL_TYPE_BLANK:
                message = String.format("Expected %s data type in %s, found a blank cell instead.", expectedTypeName, vc.getValidationMessageCellLocator(tableCell, false));
                break;

            case Cell.CELL_TYPE_BOOLEAN:
                message = String.format("Expected %s data type in %s, instead '%s' is of type %s.", expectedTypeName, vc.getValidationMessageCellLocator(tableCell, false), poiCell.getStringCellValue(), getCellTypeName(cellType));
                break;

            case Cell.CELL_TYPE_ERROR:
                message = String.format("Expected %s data type in %s, found a cell with an error in it instead.", expectedTypeName, vc.getValidationMessageCellLocator(tableCell, false));
                break;

            default:
                message = String.format("Expected %s data type in %s, but found a cell of type %d which is not a valid type.", expectedTypeName, vc.getValidationMessageCellLocator(tableCell, false), cellType);
        }

        messages.add(new DefaultCellValidationMessage(table, row, tableCell, messageCode, message));

        return false;
    }
}
