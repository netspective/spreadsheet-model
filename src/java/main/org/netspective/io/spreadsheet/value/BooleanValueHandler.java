package org.netspective.io.spreadsheet.value;

import org.apache.poi.ss.usermodel.Cell;

import java.util.HashMap;
import java.util.Map;

public class BooleanValueHandler implements ValueHandler
{
    private final Map<String, Boolean> textValues;

    public BooleanValueHandler()
    {
        textValues = new HashMap<String, Boolean>();
        textValues.put("True", Boolean.TRUE);
        textValues.put("Yes", Boolean.TRUE);
        textValues.put("1", Boolean.TRUE);
        textValues.put("False", Boolean.FALSE);
        textValues.put("No", Boolean.FALSE);
        textValues.put("0", Boolean.FALSE);
    }

    public BooleanValueHandler(final Map<String, Boolean> textValues)
    {
        this.textValues = textValues;
    }

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
                return true;

            case Cell.CELL_TYPE_STRING:
                final String value = cell.getStringCellValue();
                if(textValues.get(value) != null) return true;
                unassignableValueAsText.append(String.format("'%s' (boolean)", value));
                return false;

            case Cell.CELL_TYPE_ERROR:
                unassignableValueAsText.append(String.format("'%s' (Excel error)", cell.getErrorCellValue()));
                return false;

            case Cell.CELL_TYPE_FORMULA:
                unassignableValueAsText.append(String.format("'%s' (Excel formula)", cell.getCellFormula()));
                return false;
        }

        unassignableValueAsText.append(String.format("unknown cell type %d", cell.getCellType()));
        return false;
    }

    public Object getValue(final Cell cell, final Object defaultValue)
    {
        switch(cell.getCellType())
        {
            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_ERROR:
            case Cell.CELL_TYPE_FORMULA:
                return defaultValue;

            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();

            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue() != 0;

            case Cell.CELL_TYPE_STRING:
                final String value = cell.getStringCellValue();
                return textValues.get(value);
        }

        return defaultValue;
    }
}
