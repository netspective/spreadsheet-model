package org.netspective.io.spreadsheet.value;

import org.apache.poi.ss.usermodel.Cell;

public class StringValueHandler implements ValueHandler
{
    private final boolean trim;
    private final String[] booleanValues;
    private final String numericFormat;

    public StringValueHandler()
    {
        this.trim = true;
        this.booleanValues = new String[] { "No", "Yes" };
        this.numericFormat = "%f";
    }

    public StringValueHandler(boolean trim)
    {
        this.trim = trim;
        this.booleanValues = new String[] { "No", "Yes" };
        this.numericFormat = "%f";
    }

    public StringValueHandler(final String numericFormat)
    {
        this.trim = true;
        this.numericFormat = numericFormat;
        this.booleanValues = new String[] { "No", "Yes" };
    }

    public StringValueHandler(final String[] booleanValues)
    {
        assert(booleanValues != null && booleanValues.length == 2);
        this.trim = true;
        this.booleanValues = booleanValues;
        this.numericFormat = "%f";
    }

    public StringValueHandler(final boolean trim, final String numericFormat, final String[] booleanValues)
    {
        assert(booleanValues != null && booleanValues.length == 2);
        if(numericFormat != null)
            assert(numericFormat.length() > 0 && numericFormat.contains("%"));
        this.trim = trim;        
        this.numericFormat = numericFormat;
        this.booleanValues = booleanValues;
    }

    public boolean isBlank(final Cell cell)
    {
        if(cell.getCellType() == Cell.CELL_TYPE_BLANK)
            return true;

        if(cell.getCellType() == Cell.CELL_TYPE_STRING)
        {
            final String value = trim ? cell.getStringCellValue().trim() : cell.getStringCellValue();
            return value == null || value.length() == 0;
        }

        return false;
    }

    public boolean isValid(final Cell cell, final StringBuilder unassignableValueAsText)
    {
        switch(cell.getCellType())
        {
            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_BOOLEAN:
            case Cell.CELL_TYPE_NUMERIC:
            case Cell.CELL_TYPE_STRING:
                return true;

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
                return cell.getBooleanCellValue() ? booleanValues[1] : booleanValues[0];

            case Cell.CELL_TYPE_NUMERIC:
                return numericFormat == null ? defaultValue : String.format(numericFormat, cell.getNumericCellValue());

            case Cell.CELL_TYPE_STRING:
                return trim ? cell.getStringCellValue().trim() : cell.getStringCellValue();
        }

        return defaultValue;
    }
}
