package org.netspective.io.spreadsheet.template;

import org.apache.poi.ss.usermodel.Sheet;

public interface ColumnGroup
{
    public interface ExtractedName
    {
        public String getGroupName();
        public int extractedFromRowNumber();
    }

    public String getGroupName();
    public int getGroupStartColumnIndex();
    public int getGroupEndColumnIndex();
    public ExtractedName extractGroupName(final Sheet sheet);

    public boolean isNameValidationRequired();
    public boolean isNameValid(final String compareTo);
}
