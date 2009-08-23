package org.netspective.io.spreadsheet.validate.template;

import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.template.Column;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;

import java.util.List;

public class ValidateColumnHeadingsRule implements TemplateValidationRule
{
    private final String messageCode;

    public ValidateColumnHeadingsRule(final String messageCode)
    {
        this.messageCode = messageCode;
    }

    public boolean isFailureFatal()
    {
        return true;
    }

    public boolean isValid(final WorksheetTemplate worksheetTemplate, final Sheet sheet, final List<TemplateValidationMessage> messages)
    {
        int errors = 0;

        for(final Column column : worksheetTemplate.getColumns())
        {
            if(column.isNameValidationRequired())
            {
                try
                {
                    final Column.ExtractedName en = column.extractColumnName(sheet);
                    if(!column.isNameValid(en.getName()))
                    {
                        messages.add(new TemplateValidationMessage()
                        {
                            public String getCode() { return messageCode; }
                            public String getMessage()
                            {
                                return String.format("'%s' expected in column %s (# %d) on row %d. Found '%s' instead.",
                                                     column.getColumnName(),
                                                     column.getColumnLetters(),
                                                     column.getColumnIndex(),
                                                     en.extractedFromRowNumber(),
                                                     en.getName());
                            }
                            public String toString() { return getMessage(); }
                        });

                        errors++;
                    }
                }
                catch (final Exception e)
                {
                    messages.add(new TemplateValidationMessage()
                    {
                        public String getCode() { return messageCode; }
                        public boolean isException() { return true; }
                        public Exception getException() { return e; }
                        public String getMessage()
                        {
                            return String.format("'%s' expected in column %s (# %d) of row headings. Got an exception '%s' instead.",
                                                 column.getColumnName(),
                                                 column.getColumnLetters(),
                                                 column.getColumnIndex(),
                                                 e.getMessage());
                        }
                        public String toString() { return getMessage(); }
                    });
                    errors++;
                }
            }
        }

        return errors == 0;
    }
}
