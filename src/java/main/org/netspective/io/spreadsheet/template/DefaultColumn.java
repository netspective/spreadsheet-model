package org.netspective.io.spreadsheet.template;

import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.util.Util;
import org.netspective.io.spreadsheet.validate.cell.CellValidationRule;
import org.netspective.io.spreadsheet.value.ValueHandler;

public class DefaultColumn implements Column
{
    private final ValueHandler valueHandler;
    private final int groupNamesRowNumber;
    private final int columnHeadingsRowNumber;
    private final int columnIndex;
    private final String columnName;
    private final ColumnGroup group;
    private final CellValidationRule[] rules;

    public DefaultColumn(final ValueHandler valueHandler, final int groupNamesRowNumber, final int columnHeadingsRowNumber, final int columnIndex, final String columnName, final CellValidationRule[] rules, final ColumnGroup group)
    {
        this.valueHandler = valueHandler;
        this.groupNamesRowNumber = groupNamesRowNumber;
        this.columnHeadingsRowNumber = columnHeadingsRowNumber;
        this.columnName = columnName;
        this.columnIndex = columnIndex;
        this.rules = rules;
        this.group = group;
    }

    public DefaultColumn(final ValueHandler valueHandler, final int groupNamesRowNumber, final int columnHeadingsRowNumber, final int columnIndex, final String columnName, final CellValidationRule[] rules)
    {
        this(valueHandler, groupNamesRowNumber, columnHeadingsRowNumber, columnIndex, columnName, rules, null);
    }

    public DefaultColumn(final ValueHandler valueHandler, final int groupNamesRowNumber, final int columnHeadingsRowNumber, final int columnIndex, final String columnName)
    {
        this(valueHandler, groupNamesRowNumber, columnHeadingsRowNumber, columnIndex, columnName, new CellValidationRule[0], null);
    }

    public DefaultColumn(final ValueHandler valueHandler, final int groupNamesRowNumber, final int columnHeadingsRowNumber, final int columnIndex, final String columnName, final ColumnGroup group)
    {
        this(valueHandler, groupNamesRowNumber, columnHeadingsRowNumber, columnIndex, columnName, new CellValidationRule[0], group);
    }

    public String getColumnName()
    {
        return columnName;
    }

    public int getColumnIndex()
    {
        return columnIndex;
    }

    public String getColumnLetters()
    {
        return Util.getColumnIndexLetters(getColumnIndex());
    }

    public boolean isInGroup()
    {
        return group != null;
    }

    public String getQualifiedColumnName()
    {
        return group != null ? String.format("%s %s", getGroup().getGroupName(), getColumnName()) : getColumnName();
    }

    public ValueHandler getValueHandler()
    {
        return valueHandler;
    }

    public Column.ExtractedName extractColumnName(final Sheet sheet)
    {
        final String columnName;
        final int extractedFromRowNum;

        // see if there's a proper heading in the row with the rest of the headings (not group names)
        final String nameInHeadingRow = Util.getTrimmedStringWithoutNewlinesOrMultipleSpaces(sheet.getRow(columnHeadingsRowNumber - 1).getCell(getColumnIndex() - 1).getStringCellValue());
        if (nameInHeadingRow != null && nameInHeadingRow.trim().length() > 0)
        {
            columnName = nameInHeadingRow;
            extractedFromRowNum = columnHeadingsRowNumber;
        }
        else
        {
            // if we can't find the name in the heading row look in the group row
            columnName = Util.getTrimmedStringWithoutNewlinesOrMultipleSpaces(sheet.getRow(groupNamesRowNumber - 1).getCell(getColumnIndex() - 1).getStringCellValue());
            extractedFromRowNum = groupNamesRowNumber;
        }

        return new Column.ExtractedName()
        {
            public String getName()
            {
                return columnName;
            }

            public int extractedFromRowNumber()
            {
                return extractedFromRowNum;
            }
        };
    }

    public boolean isNameValidationRequired()
    {
        return true;
    }

    public boolean isNameValid(final String compareTo)
    {
        if (compareTo == null) return false;
        final String filteredCompare = Util.getTrimmedStringWithoutNewlinesOrMultipleSpaces(compareTo);
        return filteredCompare.compareToIgnoreCase(getColumnName()) == 0;
    }

    public CellValidationRule[] getValidationRules()
    {
        return rules;
    }

    public ColumnGroup getGroup()
    {
        return group;
    }
}