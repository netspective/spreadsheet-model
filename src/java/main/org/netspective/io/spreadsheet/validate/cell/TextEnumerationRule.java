package org.netspective.io.spreadsheet.validate.cell;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.value.ValueHandler;

import java.util.List;
import java.util.Set;

public class TextEnumerationRule implements CellValidationRule
{
    public interface ValidValues
    {
        public boolean isValidValue(final String value);
        public String getInvalidValueErrorMessage(final String cellLocation, final String value);
    }

    public class StaticValidValues implements ValidValues
    {
        private final Set<String> validValues;

        public StaticValidValues(final Set<String> validValues)
        {
            this.validValues = validValues;
        }

        public boolean isValidValue(final String value)
        {
            return validValues.contains(value);
        }

        public String getInvalidValueErrorMessage(final String cellLocation, final String value)
        {
            if(validValues.size() < 10)
                return String.format("Column value '%s' found in %s, expected one of %s.", value, cellLocation, validValues.toString());
            else
                return String.format("Column value '%s' found in %s, expected one of %d proper values.", value, cellLocation, validValues.size());
        }
    }

    private final String messageCode;
    private final ValidValues validValues;

    public TextEnumerationRule(final String messageCode, final ValidValues validValues)
    {
        this.messageCode = messageCode;
        this.validValues = validValues;
    }

    public TextEnumerationRule(final String messageCode, final Set<String> enumerations)
    {
        this.messageCode = messageCode;
        this.validValues = new StaticValidValues(enumerations);
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
                    if(validValues.isValidValue(value))
                        continue;

                    messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, validValues.getInvalidValueErrorMessage(vc.getValidationMessageCellLocator(cell, true), value)));
                    errors++;
                }
                return errors == 0;
            }
            else if(object instanceof String)
            {
                final String value = (String) object;
                if(validValues.isValidValue(value))
                    return true;

                messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, validValues.getInvalidValueErrorMessage(vc.getValidationMessageCellLocator(cell, true), value)));
                return false;
            }
            else
            {
                messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, validValues.getInvalidValueErrorMessage(vc.getValidationMessageCellLocator(cell, true), object.toString() + " (" + object.getClass().getName() + ')')));
                return false;
            }
        }
        else
        {
            messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, validValues.getInvalidValueErrorMessage(vc.getValidationMessageCellLocator(cell, true), unassignableTextValue.toString())));
            return false;
        }
    }
}
