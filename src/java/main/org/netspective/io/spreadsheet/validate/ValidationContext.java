package org.netspective.io.spreadsheet.validate;

import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.cell.CellValidationMessage;

public interface ValidationContext
{
    public String getValidationMessageRowSummary(final TableRow row, final CellValidationMessage[] messages);
    public String getValidationMessageCellLocator(final TableCell cell, final boolean useInFormatSpec);
}
