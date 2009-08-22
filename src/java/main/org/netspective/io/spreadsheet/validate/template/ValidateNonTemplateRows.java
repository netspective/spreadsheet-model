package org.netspective.io.spreadsheet.validate.template;

import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;

import java.util.List;

public class ValidateNonTemplateRows implements TemplateValidationRule
{
    public boolean isValid(final WorksheetTemplate worksheetTemplate, final Sheet sheet, final List<TemplateValidationMessage> messages)
    {
        return true;
    }

    public boolean isFailureFatal()
    {
        return false;
    }
}
