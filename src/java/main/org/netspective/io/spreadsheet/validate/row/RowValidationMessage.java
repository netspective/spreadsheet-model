package org.netspective.io.spreadsheet.validate.row;

import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationMessage;
import org.netspective.io.spreadsheet.validate.cell.CellValidationMessage;

public interface RowValidationMessage extends ValidationMessage
{
    public TableRow getRow();
    public CellValidationMessage[] getCellValidationErrors();
}
