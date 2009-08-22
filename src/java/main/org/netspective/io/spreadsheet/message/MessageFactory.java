package org.netspective.io.spreadsheet.message;

import java.util.ArrayList;
import java.util.List;

public class MessageFactory
{
    public static final MessageFactory instance = new MessageFactory();

    public static MessageFactory getInstance()
    {
        return instance;
    }

    public Message createMessage(final String code, final String format, final Object ... args)
    {
        return new DefaultMessage(code, format, args);
    }

    public Message createExceptionMessage(final Exception exception, final String code, final String format, final Object ... args)
    {
        return new DefaultExceptionMessage(new DefaultMessage(code, format, args), exception);
    }

    public List<Message> createMessages(final String category, final String format, final String[] messages)
    {
        final List<Message> result = new ArrayList<Message>();
        for(final String message : messages)
            result.add(new DefaultMessage(category, format, message));
        return result;
    }
}
