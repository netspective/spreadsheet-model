package org.netspective.io.spreadsheet.validate.cell;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;

public class DefaultCellValidationMessage implements CellValidationMessage
{
    private final Table table;
    private final TableRow row;
    private final TableCell cell;
    private final String code;
    private final String message;

    public DefaultCellValidationMessage(final Table table, final TableRow row, final TableCell cell, final String code, final String format, final Object ... args)
    {
        this.table = table;
        this.row = row;
        this.cell = cell;
        this.code = code;
        if(args == null || args.length == 0)
            this.message = format;
        else
        {
            try
            {
                this.message = String.format(format, args);
            }
            catch (Exception e)
            {
                throw new RuntimeException(String.format("Unable to format error code '%s' with message format \"%s\"", code, format), e);
            }            
        }
    }

    public Table getTable()
    {
        return table;
    }

    public TableRow getRow()
    {
        return row;
    }

    public TableCell getCell()
    {
        return cell;
    }

    public String getCode()
    {
        return code;
    }

    public String getMessage()
    {
        return message;
    }
}
