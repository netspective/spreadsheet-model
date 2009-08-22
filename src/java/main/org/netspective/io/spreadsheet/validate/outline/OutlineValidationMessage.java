package org.netspective.io.spreadsheet.validate.outline;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.validate.ValidationMessage;

public interface OutlineValidationMessage extends ValidationMessage
{
    public Table getTable();
}
