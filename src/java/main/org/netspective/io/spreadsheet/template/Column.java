package org.netspective.io.spreadsheet.template;

import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.validate.cell.CellValidationRule;
import org.netspective.io.spreadsheet.value.ValueHandler;

public interface Column
{
    public interface ExtractedName
    {
        public String getName();
        public int extractedFromRowNumber();
    }

    public String getColumnName();
    public int getColumnIndex();
    public String getColumnLetters();
    public ExtractedName extractColumnName(final Sheet sheet);

    public boolean isNameValidationRequired();
    public boolean isNameValid(final String compareTo);

    public CellValidationRule[] getValidationRules();

    public ColumnGroup getGroup();
    public boolean isInGroup();
    public String getQualifiedColumnName();

    public ValueHandler getValueHandler();
}
