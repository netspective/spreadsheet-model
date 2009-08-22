package org.netspective.io.spreadsheet.validate.cell;

import org.apache.poi.ss.usermodel.Cell;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.value.ValueHandler;

import java.util.List;

public class TextLengthRule implements CellValidationRule
{
    private final String messageCode;
    private final CellTypeRule stringTypeRule;
    private final int minLength;
    private final int maxLength;
    private final String invalidMessageFormatSpec;

    public TextLengthRule(final String messageCode, final int maxLength, final String invalidMessageFormatSpec)
    {
        this.messageCode = messageCode;
        this.stringTypeRule = new CellTypeRule(messageCode, Cell.CELL_TYPE_STRING);
        this.minLength = 0;
        this.maxLength = maxLength;
        this.invalidMessageFormatSpec = invalidMessageFormatSpec;
    }

    public TextLengthRule(final String messageCode, final int minLength, final int maxLength, final String invalidMessageFormatSpec)
    {
        this.messageCode = messageCode;
        this.stringTypeRule = new CellTypeRule(messageCode, Cell.CELL_TYPE_STRING);
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.invalidMessageFormatSpec = invalidMessageFormatSpec;
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

            if(object instanceof String[])
            {
                final String[] values = (String[]) object;
                int errors = 0;
                for(final String value : values)
                {
                    if(value.length() >= minLength && value.length() <= maxLength)
                        continue;

                    messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidMessageFormatSpec, vc.getValidationMessageCellLocator(cell, false), value, minLength, maxLength));
                    errors++;
                }
                return errors == 0;
            }
            else if(object instanceof String)
            {
                final String value = (String) object;
                if(value.length() >= minLength && value.length() <= maxLength)
                    return true;

                messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidMessageFormatSpec, vc.getValidationMessageCellLocator(cell, false), value, minLength, maxLength));
                return false;
            }
            else
            {
                messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidMessageFormatSpec, vc.getValidationMessageCellLocator(cell, false), object.toString() + " (" + object.getClass().getName() + ')', minLength, maxLength));
                return false;
            }
        }
        else
        {
            messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidMessageFormatSpec, vc.getValidationMessageCellLocator(cell, false), unassignableTextValue, minLength, maxLength));
            return false;
        }
    }
}
