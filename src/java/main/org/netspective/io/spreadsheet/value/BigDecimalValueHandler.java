package org.netspective.io.spreadsheet.value;

import org.apache.poi.ss.usermodel.Cell;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalValueHandler implements ValueHandler
{
    private final Integer scale;
    private final RoundingMode rounding;

    public BigDecimalValueHandler()
    {
        this.scale = 6;
        this.rounding = RoundingMode.HALF_UP;
    }

    public BigDecimalValueHandler(final Integer scale)
    {
        this.scale = scale;
        this.rounding = RoundingMode.HALF_UP;
    }

    public BigDecimalValueHandler(final Integer scale, final RoundingMode rounding)
    {
        this.scale = scale;
        this.rounding = rounding;
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
            case Cell.CELL_TYPE_FORMULA:
                return true;

            case Cell.CELL_TYPE_STRING:
                final String textValue = cell.getStringCellValue();
                try
                {
                    new BigDecimal(Double.parseDouble(textValue));
                    return true;
                }
                catch (NumberFormatException e)
                {
                    unassignableValueAsText.append(String.format("'%s' (BigDecimal)", textValue));
                    return false;
                }

            case Cell.CELL_TYPE_ERROR:
                unassignableValueAsText.append(String.format("'%s' (Excel error)", cell.getErrorCellValue()));
                return false;
        }

        unassignableValueAsText.append(String.format("unknown cell type %d", cell.getCellType()));
        return false;
    }

    public BigDecimal setScale(final BigDecimal original)
    {
        return scale == null ? original : original.setScale(scale, rounding);
    }

    public Object getValue(final Cell cell, final Object defaultValue)
    {
        switch(cell.getCellType())
        {
            case Cell.CELL_TYPE_BLANK:
                return defaultValue;

            case Cell.CELL_TYPE_BOOLEAN:
                return setScale(new BigDecimal(cell.getBooleanCellValue() ? "1.0" : "0.0"));

            case Cell.CELL_TYPE_ERROR:
                return defaultValue;

            case Cell.CELL_TYPE_FORMULA:
            case Cell.CELL_TYPE_NUMERIC:
                // have to round to "scale" digits here, since excel formulas are computed using doubles
                return new BigDecimal(cell.getNumericCellValue()).divide(new BigDecimal(1), scale, rounding);


            case Cell.CELL_TYPE_STRING:
                return setScale(new BigDecimal(cell.getStringCellValue()));
        }

        return String.format("ERROR: unknown cell type %d", cell.getCellType());
    }
}

