package org.netspective.io.spreadsheet.validate.cell;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.value.ValueHandler;

import java.util.List;

public class TextRegExRule implements CellValidationRule
{
    private final String messageCode;
    private final String regExpr;
    private final String invalidMessageFormatSpec;

    public TextRegExRule(final String messageCode, final String regExpr, final String invalidMatchMessageFormat)
    {
        this.messageCode = messageCode;
        this.regExpr = regExpr;
        this.invalidMessageFormatSpec = invalidMatchMessageFormat;
    }

    public boolean isValid(final ValidationContext vc, final Table table, final TableRow row, final TableCell cell, final List<CellValidationMessage> messages)
    {
        final StringBuilder unassignableTextValue = new StringBuilder();
        System.out.printf("Table %s, row %s, cell %s", table, row, cell);
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
                    if(value.matches(regExpr))
                        continue;

                    messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidMessageFormatSpec, vc.getValidationMessageCellLocator(cell, false), value, regExpr));
                    errors++;
                }
                return errors == 0;
            }
            else if(object instanceof String)
            {
                final String value = (String) object;
                if(value.matches(regExpr))
                    return true;

                messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidMessageFormatSpec, vc.getValidationMessageCellLocator(cell, false), value, regExpr));
                return false;
            }
            else
            {
                messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidMessageFormatSpec, vc.getValidationMessageCellLocator(cell, false), object.toString() + " (" + object.getClass().getName() + ')'));
                return false;
            }
        }
        else
        {
            messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidMessageFormatSpec, vc.getValidationMessageCellLocator(cell, false), unassignableTextValue));
            return false;
        }
    }

}
