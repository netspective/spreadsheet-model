package org.netspective.io.spreadsheet.template;

import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.util.Util;

public class DefaultColumnGroup implements ColumnGroup
{
    private final int groupNamesRowNumber;
    private final String groupName;
    private final int groupStartColumnIndex;
    private final int groupEndColumnIndex;

    public DefaultColumnGroup(final int groupNamesRowNumber, final String groupName, final int groupStartColumnIndex, final int groupEndColumnIndex)
    {
        this.groupNamesRowNumber = groupNamesRowNumber;
        this.groupName = groupName;
        this.groupStartColumnIndex = groupStartColumnIndex;
        this.groupEndColumnIndex = groupEndColumnIndex;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public int getGroupStartColumnIndex()
    {
        return groupStartColumnIndex;
    }

    public int getGroupEndColumnIndex()
    {
        return groupEndColumnIndex;
    }

    public boolean isNameValidationRequired()
    {
        return true;
    }

    public ExtractedName extractGroupName(final Sheet sheet)
    {
        return new ExtractedName()
        {
            public String getGroupName()
            {
                return Util.getTrimmedStringWithoutNewlinesOrMultipleSpaces(sheet.getRow(groupNamesRowNumber - 1).getCell(getGroupStartColumnIndex() - 1).getStringCellValue());
            }

            public int extractedFromRowNumber()
            {
                return groupNamesRowNumber;
            }
        };
    }

    public boolean isNameValid(final String compareTo)
    {
        if (compareTo == null) return false;
        final String filteredCompare = Util.getTrimmedStringWithoutNewlinesOrMultipleSpaces(compareTo);
        return filteredCompare.compareToIgnoreCase(getGroupName()) == 0;
    }
}