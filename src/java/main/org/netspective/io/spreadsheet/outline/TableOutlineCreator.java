package org.netspective.io.spreadsheet.outline;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.validate.outline.OutlineValidationMessage;

import java.util.List;

public interface TableOutlineCreator
{
    public TableOutline createOutline(final Table table, final List<OutlineValidationMessage> messages);
}
