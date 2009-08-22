package org.netspective.io.spreadsheet.validate.cell;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.value.ValueHandler;

import java.util.List;
import java.util.Set;

public class IntegerEnumerationRule implements CellValidationRule
{
    public interface ValidValues
    {
        public boolean isValidValue(final int value);
        public String getInvalidValueErrorMessage(final String cellLocation, final int value);
        public String getInvalidValueErrorMessage(final String cellLocation, final String value);
    }

    public class StaticValidValues implements ValidValues
    {
        private final Set<Integer> validValues;

        public StaticValidValues(Set<Integer> validValues)
        {
            this.validValues = validValues;
        }

        public boolean isValidValue(final int value)
        {
            return validValues.contains(value);
        }

        public String getInvalidValueErrorMessage(final String cellLocation, final int value)
        {
            return String.format("Column value '%d' found in %s, expected one of %s.", value, cellLocation, validValues.toString());
        }

        public String getInvalidValueErrorMessage(final String cellLocation, final String value)
        {
            return String.format("Column value '%s' (text) found in %s, expected one of %s.", value, cellLocation, validValues.toString());
        }
    }

    private final String messageCode;
    private final ValidValues validValues;

    public IntegerEnumerationRule(final String messageCode, final ValidValues validValues)
    {
        this.messageCode = messageCode;
        this.validValues = validValues;
    }

    public IntegerEnumerationRule(final String messageCode, final Set<Integer> enumerations)
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

            if(object instanceof Integer[])
            {
                final Integer[] values = (Integer[]) object;
                int errors = 0;
                for(final Integer value : values)
                {
                    if(validValues.isValidValue(value))
                        continue;

                    messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, validValues.getInvalidValueErrorMessage(vc.getValidationMessageCellLocator(cell, true), value)));
                    errors++;
                }
                return errors == 0;
            }
            else if(object instanceof Integer)
            {
                final Integer value = (Integer) object;
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
