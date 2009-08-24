package org.netspective.io.spreadsheet.validate.cell;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.value.ValueHandler;

import java.util.List;

public class DataRequiredRule implements CellValidationRule
{
    private final String messageCode;
    private final String invalidMessageFormatSpec;

    public DataRequiredRule(final String messageCode) 
    {
        this.messageCode = messageCode;
        this.invalidMessageFormatSpec = "%s is blank, a value is required in this cell.";
    }

    public DataRequiredRule(final String messageCode, final String invalidMessageFormatSpec)
    {
        this.messageCode = messageCode;
        this.invalidMessageFormatSpec = invalidMessageFormatSpec;
    }

    public boolean isValid(final ValidationContext vc, final Table table, final TableRow row, final TableCell cell, final List<CellValidationMessage> messages)
    {
        final StringBuilder unassignableTextValue = new StringBuilder();
        final ValueHandler vh = cell.getColumn().getValueHandler();
        
        if(vh.isBlank(cell.getCell()))
        {
            messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidMessageFormatSpec, vc.getValidationMessageCellLocator(cell, false)));
            return false;
        }

        if(! vh.isValid(cell.getCell(), unassignableTextValue))
        {
            messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidMessageFormatSpec, vc.getValidationMessageCellLocator(cell, false), unassignableTextValue));
            return false;
        }

        return true;
    }
}
