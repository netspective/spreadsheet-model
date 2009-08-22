package org.netspective.io.spreadsheet.validate.outline;

import org.netspective.io.spreadsheet.model.Table;

public class DefaultOutlineValidationMessage implements OutlineValidationMessage
{
    private final Table table;
    private final String code;
    private final String message;

    public DefaultOutlineValidationMessage(final Table table, final String code, final String format, final Object ... args)
    {
        this.table = table;
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

    public String getCode()
    {
        return code;
    }

    public String getMessage()
    {
        return message;
    }
}
