package org.netspective.io.spreadsheet.message;

public class DefaultMessage implements Message
{
    private final String code;
    private final String message;

    public DefaultMessage(final String code, final String format, final Object ... args)
    {
        this.code = code;
        this.message = String.format(format, args);
    }

    public String getCode()
    {
        return code;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public String toString()
    {
        return getMessage();
    }
}
