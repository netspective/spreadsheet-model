package org.netspective.io.spreadsheet.value;

import org.apache.poi.ss.usermodel.Cell;
import org.netspective.io.spreadsheet.util.Util;

public class IntegerValueHandler implements ValueHandler
{
    public boolean isBlank(final Cell cell)
    {
        return cell.getCellType() == Cell.CELL_TYPE_BLANK;
    }

    public boolean isValid(final Cell cell, final StringBuilder unassignableValueAsText)
    {
        switch(cell.getCellType())
        {
            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_BOOLEAN:
            case Cell.CELL_TYPE_NUMERIC:
            case Cell.CELL_TYPE_FORMULA:
                return true;

            case Cell.CELL_TYPE_STRING:
                final String textValue = cell.getStringCellValue();
                try
                {
                    Integer.valueOf(textValue);
                    return true;
                }
                catch (NumberFormatException e)
                {
                    unassignableValueAsText.append(String.format("'%s' (int)", textValue));
                    return false;
                }

            case Cell.CELL_TYPE_ERROR:
                unassignableValueAsText.append(String.format("'%s' (Excel error)", cell.getErrorCellValue()));
                return false;
        }

        unassignableValueAsText.append(String.format("ERROR_%s: unknown cell type %d", Util.getCellLocator(cell), cell.getCellType()));
        return false;
    }

    public Object getValue(final Cell cell, final Object defaultValue)
    {
        switch(cell.getCellType())
        {
            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_ERROR:
                return defaultValue;

            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue() ? 1 : 0;

            case Cell.CELL_TYPE_FORMULA:
            case Cell.CELL_TYPE_NUMERIC:
                return new Double(cell.getNumericCellValue()).intValue();

            case Cell.CELL_TYPE_STRING:
                return Integer.parseInt(cell.getStringCellValue());
        }

        return defaultValue;
    }
}

