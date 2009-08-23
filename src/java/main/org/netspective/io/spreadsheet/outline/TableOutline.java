package org.netspective.io.spreadsheet.outline;

import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.validate.outline.NodeValidationMessage;

import java.util.List;

public interface TableOutline
{
    public Table getTable();
    public List<TableOutlineNode> getRootNodes();
    public boolean isValid(final ValidationContext vc, final List<NodeValidationMessage> messages);
}
