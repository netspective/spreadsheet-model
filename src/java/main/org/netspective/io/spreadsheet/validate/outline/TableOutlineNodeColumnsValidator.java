package org.netspective.io.spreadsheet.validate.outline;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.outline.TableOutline;
import org.netspective.io.spreadsheet.outline.TableOutlineNode;
import org.netspective.io.spreadsheet.template.Column;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.validate.cell.CellValidationMessage;
import org.netspective.io.spreadsheet.validate.cell.CellValidationRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableOutlineNodeColumnsValidator
{
    private final String messageCode;
    private final Map<Column, CellValidationRule[]> cellValidationRules;

    public TableOutlineNodeColumnsValidator(final String messageCode, final Map<Column, CellValidationRule[]> cellValidationRules)
    {
        this.messageCode = messageCode;
        this.cellValidationRules = cellValidationRules;
    }

    public boolean isValid(final ValidationContext vc, final TableOutline outline, final TableOutlineNode node,
                           final List<NodeValidationMessage> messages)
    {
        final List<CellValidationMessage> cellMessages = new ArrayList<CellValidationMessage>();

        final Table table = outline.getTable();
        final TableRow row = node.getTableRow();
        for(final Map.Entry<Column, CellValidationRule[]> entry : cellValidationRules.entrySet())
        {
            final Column column = entry.getKey();
            final CellValidationRule[] rules = entry.getValue();

            final TableCell cell = row.findCellForColumn(column);
            if(cell != null)
            {
                for(final CellValidationRule cvr : rules)
                {
                    // validate and store the messages in appropriate error/warning list
                    cvr.isValid(vc, table, row, cell, cellMessages);
                }
            }
            else
            {
                throw new RuntimeException(String.format("Unable to find cell for Column %s at row %s in outline node.", column, node));
            }
        }

        if(cellMessages.size() > 0)
        {
            final CellValidationMessage[] validationMessages = cellMessages.toArray(new CellValidationMessage[cellMessages.size()]);
            messages.add(new NodeValidationMessage()
            {
                public TableRow getRow()
                {
                    return row;
                }

                public TableOutline getOutline()
                {
                    return outline;
                }

                public TableOutlineNode getNode()
                {
                    return node;
                }

                public CellValidationMessage[] getCellValidationErrors()
                {
                    return validationMessages;
                }

                public String getCode()
                {
                    return messageCode;
                }

                public String getMessage()
                {
                    // null means that we won't report this message, only the children through getCellValidationErrors
                    return null;
                }

                @Override
                public String toString()
                {
                    return getMessage();
                }
            });
        }

        return cellMessages.size() == 0;
    }
}