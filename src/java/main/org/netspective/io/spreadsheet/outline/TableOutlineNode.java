package org.netspective.io.spreadsheet.outline;

import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.outline.NodeValidationRule;

import java.util.List;

public interface TableOutlineNode
{
    public TableRow getTableRow();
    public List<TableOutlineNode> getChildren();
    public int getFirstDataRowIndexInTable();
    public int getLastDataRowIndexInTable();
    public NodeValidationRule[] getValidationRules();
}
