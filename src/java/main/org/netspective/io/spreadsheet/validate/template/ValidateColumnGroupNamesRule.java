package org.netspective.io.spreadsheet.validate.template;

import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.template.ColumnGroup;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;

import java.util.List;

public class ValidateColumnGroupNamesRule implements TemplateValidationRule
{
    public boolean isFailureFatal()
    {
        return true;
    }

    public boolean isValid(final WorksheetTemplate worksheetTemplate, final Sheet sheet, final List<TemplateValidationMessage> messages)
    {
        if(! worksheetTemplate.hasColumnGroups())
            return true;

        int errors = 0;

        for(final ColumnGroup group : worksheetTemplate.getColumnGroups())
        {
            if(group.isNameValidationRequired())
            {
                final ColumnGroup.ExtractedName en = group.extractGroupName(sheet);
                try
                {
                    if(!group.isNameValid(en.getGroupName()))
                    {
                        messages.add(new TemplateValidationMessage()
                        {
                            public String getCode() { return ValidateColumnGroupNamesRule.class.getName(); }
                            public boolean isException() { return false; }
                            public Exception getException() { return null; }
                            public String getMessage()
                            {
                                return String.format("Column group '%s' expected in columns %d-%d on row %d. Found '%s' instead.",
                                                     group.getGroupName(),
                                                     group.getGroupStartColumnIndex(), group.getGroupEndColumnIndex(),
                                                     en.extractedFromRowNumber(),
                                                     en.getGroupName());
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
                        public String getCode() { return ValidateColumnGroupNamesRule.class.getName(); }
                        public boolean isException() { return true; }
                        public Exception getException() { return e; }
                        public String getMessage()
                        {
                            return String.format("Column group '%s' expected in columns %d-%d on row %d. Got an exception '%s' instead.",
                                                 group.getGroupName(),
                                                 group.getGroupStartColumnIndex(), group.getGroupEndColumnIndex(),
                                                 en.extractedFromRowNumber(),
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
