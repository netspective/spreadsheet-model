package org.netspective.io.spreadsheet.template;

import org.netspective.io.spreadsheet.message.Message;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.validate.row.RowValidationRule;
import org.netspective.io.spreadsheet.validate.template.TemplateValidationRule;

import java.util.List;
import java.util.Map;

public interface WorksheetTemplate
{
    public List<Column> getColumns();

    public List<ColumnGroup> getColumnGroups();
    public boolean hasColumnGroups();

    public Map<Integer, Column> getColumnsMapByIndex();
    public List<TemplateValidationRule> getTemplateValidationRules();
    public List<RowValidationRule> getRowValidationRules();
    public ValidationContext createValidationContext();

    public boolean isWarning(final Message message);
}
