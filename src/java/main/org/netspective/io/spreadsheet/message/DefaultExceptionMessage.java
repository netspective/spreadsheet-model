package org.netspective.io.spreadsheet.message;

public class DefaultExceptionMessage implements ExceptionMessage
{
    private final Message message;
    private final Exception exception;

    public DefaultExceptionMessage(final Message message, final Exception exception)
    {
        this.message = message;
        this.exception = exception;
    }

    public String getCode() { return message.getCode(); }
    public String getMessage() { return message.getMessage(); }

    public Exception getException()
    {
        return exception;
    }
}
