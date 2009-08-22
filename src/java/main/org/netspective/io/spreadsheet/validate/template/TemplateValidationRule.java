package org.netspective.io.spreadsheet.validate.template;

import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;

import java.util.List;

public interface TemplateValidationRule
{
    public boolean isValid(final WorksheetTemplate worksheetTemplate, final Sheet sheet, final List<TemplateValidationMessage> messages);
    public boolean isFailureFatal();
}
