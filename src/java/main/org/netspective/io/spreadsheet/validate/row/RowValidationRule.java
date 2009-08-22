package org.netspective.io.spreadsheet.validate.row;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationContext;

import java.util.List;

public interface RowValidationRule
{
    public boolean isValid(final ValidationContext vc, final Table table, final TableRow row,
                           final List<RowValidationMessage> messages);
}
