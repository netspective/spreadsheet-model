package org.netspective.io.spreadsheet.validate.cell;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.value.ValueHandler;

import java.util.List;

public class NumericRangeRule implements CellValidationRule
{
    private final String messageCode;
    private final double min;
    private final double max;
    private final String invalidValueFormatSpec;
    private final String invalidDataTypeFormatSpec;

    public NumericRangeRule(final String messageCode, double min, double max, final String invalidValueFormatSpec, final String invalidDataTypeFormatSpec)
    {
        this.messageCode = messageCode;
        this.min = min;
        this.max = max;
        this.invalidValueFormatSpec = invalidValueFormatSpec;
        this.invalidDataTypeFormatSpec = invalidDataTypeFormatSpec;
    }

    public boolean isValid(final ValidationContext vc, final Table table, final TableRow row, final TableCell cell, final List<CellValidationMessage> messages)
    {
        final StringBuilder unassignableTextValue = new StringBuilder();
        final ValueHandler vh = cell.getColumn().getValueHandler();

        if(vh.isBlank(cell.getCell()))
            return true;

        if(vh.isValid(cell.getCell(), unassignableTextValue))
        {
            final Object object = vh.getValue(cell.getCell(), null);
            if(object == null)
                return true;

            if(object instanceof Number)
            {
                final double value = ((Number) object).doubleValue();
                if(value < min || value > max)
                {
                    messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidValueFormatSpec, vc.getValidationMessageCellLocator(cell, false), value, min, max));
                    return false;
                }
                else
                    return true;
            }
            else
            {
                messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidDataTypeFormatSpec, vc.getValidationMessageCellLocator(cell, false), object.toString(), min, max));
                return false;
            }

        }
        else
        {
            messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidDataTypeFormatSpec, vc.getValidationMessageCellLocator(cell, false), unassignableTextValue, min, max));
            return false;
        }
    }
}
