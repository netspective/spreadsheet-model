package org.netspective.io.spreadsheet.outline;

import org.netspective.io.spreadsheet.model.Table;

import java.util.List;

public interface TableOutline
{
    public Table getTable();
    public List<TableOutlineNode> getRootNodes();
}
