package org.netspective.io.spreadsheet.validate.row;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.template.Column;
import org.netspective.io.spreadsheet.util.Util;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.validate.cell.CellValidationMessage;
import org.netspective.io.spreadsheet.validate.cell.CellValidationRule;

import java.util.ArrayList;
import java.util.List;

public class ValidateColumnData implements RowValidationRule
{
    private final String messageCode;

    public ValidateColumnData(final String messageCode)
    {
        this.messageCode = messageCode;
    }

    public boolean isValid(final ValidationContext vc, final Table table, final TableRow row,
                           final List<RowValidationMessage> messages)
    {
        final List<CellValidationMessage> cellMessages = new ArrayList<CellValidationMessage>();

        for(final Column column : table.getTemplate().getColumns())
        {
            final CellValidationRule[] rules = column.getValidationRules();
            if(rules.length == 0)
                continue;

            final TableCell cell = row.findCellForColumn(column);
            for(final CellValidationRule cvr : rules)
            {
                // validate and store the messages in appropriate error/warning list
                try
                {
                    cvr.isValid(vc, table, row, cell, cellMessages);
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                    messages.add(new RowValidationMessage()
                    {
                        public TableRow getRow() { return row; }
                        public CellValidationMessage[] getCellValidationErrors() { return new CellValidationMessage[0]; }
                        public String getCode() { return messageCode; }
                        public String getMessage() { return String.format("Exception on row %d cell %d running rule %s.isValid(): %s (%s)", row.getRowNumberInSheet(), Util.getCellLocator(cell.getCell())); }
                        public String toString() { return getMessage(); }
                    });
                }
            }
        }

        if(cellMessages.size() > 0)
        {
            final CellValidationMessage[] validationMessages = cellMessages.toArray(new CellValidationMessage[cellMessages.size()]);
            messages.add(new RowValidationMessage()
            {
                public TableRow getRow() { return row; }
                public CellValidationMessage[] getCellValidationErrors() { return validationMessages; }
                public String getCode() { return messageCode; }
                public String getMessage() { return null; /* null means that we won't report this message, only the children through getCellValidationErrors */ }
                public String toString() { return getMessage(); }
            });
        }

        return cellMessages.size() == 0;
    }
}
