package org.netspective.io.spreadsheet.validate.cell;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationMessage;

public interface CellValidationMessage extends ValidationMessage
{
    public Table getTable();
    public TableRow getRow();
    public TableCell getCell();
}
