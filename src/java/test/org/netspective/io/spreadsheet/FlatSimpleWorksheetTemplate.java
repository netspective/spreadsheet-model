package org.netspective.io.spreadsheet;

import org.netspective.io.spreadsheet.message.Message;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.template.Column;
import org.netspective.io.spreadsheet.template.ColumnGroup;
import org.netspective.io.spreadsheet.template.DefaultColumn;
import org.netspective.io.spreadsheet.template.DefaultColumnGroup;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;
import org.netspective.io.spreadsheet.util.Util;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.validate.cell.CellValidationMessage;
import org.netspective.io.spreadsheet.validate.row.RowValidationRule;
import org.netspective.io.spreadsheet.validate.row.ValidateColumnData;
import org.netspective.io.spreadsheet.validate.template.TemplateValidationRule;
import org.netspective.io.spreadsheet.validate.template.ValidateColumnGroupNamesRule;
import org.netspective.io.spreadsheet.validate.template.ValidateColumnHeadingsRule;
import org.netspective.io.spreadsheet.value.StringValueHandler;
import org.netspective.io.spreadsheet.value.ValueHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlatSimpleWorksheetTemplate implements WorksheetTemplate, ValidationContext
{
    public static final int NO_GROUP = -1;

    private final List<Column> columns = new ArrayList<Column>();
    private final Map<Integer, Column> columnsMapByIndex = new HashMap<Integer, Column>();
    private final List<TemplateValidationRule> templateValidationRules = new ArrayList<TemplateValidationRule>();
    private final List<RowValidationRule> rowValidationRules = new ArrayList<RowValidationRule>();

    public FlatSimpleWorksheetTemplate(final int groupNamesRowNumber, final int headingRowNumber)
    {
        final boolean groupingTest = groupNamesRowNumber != NO_GROUP;
        final ValueHandler stringValueHandler = new StringValueHandler();

        columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 2, "Column 1"));
        columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 3, "Column 2"));

        if(groupingTest)
        {
            final ColumnGroup group1 = new DefaultColumnGroup(groupNamesRowNumber, "Group 1", 4, 5);
            columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 4, "Column 3", group1));
            columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 5, "Column 4", group1));            
        }
        else
        {
            columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 4, "Column 3"));
            columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 5, "Column 4"));
        }

        columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 6, "Column 5"));

        if(groupingTest)
        {
            final ColumnGroup group2 = new DefaultColumnGroup(groupNamesRowNumber, "Group 2", 7, 9);
            columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 7, "Column 6", group2));
            columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 8, "Column 7", group2));
            columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 9, "Column 8", group2));
        }
        else
        {
            columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 7, "Column 6"));
            columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 8, "Column 7"));
            columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 9, "Column 8"));
        }
        columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 10, "Column 9"));
        columns.add(new DefaultColumn(stringValueHandler, NO_GROUP, headingRowNumber, 11, "Column 10"));

        for(final Column column : columns)
            columnsMapByIndex.put(column.getColumnIndex(), column);

        if(groupingTest)
            templateValidationRules.add(new ValidateColumnGroupNamesRule());
        templateValidationRules.add(new ValidateColumnHeadingsRule());

        rowValidationRules.add(new ValidateColumnData());
    }

    public List<Column> getColumns()
    {
        return columns;
    }

    public List<ColumnGroup> getColumnGroups()
    {
        return null;
    }

    public boolean hasColumnGroups()
    {
        return false;
    }

    public Map<Integer, Column> getColumnsMapByIndex()
    {
        return Collections.unmodifiableMap(columnsMapByIndex);
    }

    public List<TemplateValidationRule> getTemplateValidationRules()
    {
        return Collections.unmodifiableList(templateValidationRules);
    }

    public List<RowValidationRule> getRowValidationRules()
    {
        return Collections.unmodifiableList(rowValidationRules);
    }

    public ValidationContext createValidationContext()
    {
        return this;
    }

    public boolean isWarning(final Message message)
    {
        return false;
    }

    public String getValidationMessageRowSummary(final TableRow row, final CellValidationMessage[] cellMessages)
    {
        return String.format("Row %d: %d error(s).", row.getRowNumberInSheet(), cellMessages.length);
    }

    public String getValidationMessageCellLocator(final TableCell cell, final boolean useInFormatSpec)
    {
        final String cellLocation = Util.getCellLocator(cell.getCell());
        final String columnName = cell.getColumn().getQualifiedColumnName();
        final String filteredName = useInFormatSpec ? columnName.replace("%", "%%") : columnName;
        return String.format("%s (\"%s\")", cellLocation, filteredName);
    }
}
