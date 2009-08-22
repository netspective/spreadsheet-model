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
        if(vh.isValid(cell.getCell(), unassignableTextValue))
        {
            final Object object = vh.getValue(cell.getCell(), null);
            if(object == null)
                return true;

            if(object instanceof Number[])
            {
                final Integer[] values = (Integer[]) object;
                int errors = 0;
                for(final Number value : values)
                {
                    if(! isValid(vc, table, row, cell, messages,  value))
                        errors++;
                }
                return errors == 0;
            }
            else if(object instanceof Number)
                return isValid(vc, table, row, cell, messages,  object);
            else
            {
                messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidDataTypeFormatSpec, vc.getValidationMessageCellLocator(cell, false), object.toString() + " (" + object.getClass().getName() + ')', min, max));
                return false;
            }
        }
        else
        {
            messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidDataTypeFormatSpec, vc.getValidationMessageCellLocator(cell, false), unassignableTextValue.toString(), min, max));
            return false;
        }
    }

    protected boolean isValid(final ValidationContext vc, final Table table, final TableRow row, final TableCell cell, final List<CellValidationMessage> messages, final Object object)
    {
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
}
