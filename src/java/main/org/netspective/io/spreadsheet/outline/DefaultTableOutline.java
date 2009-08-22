package org.netspective.io.spreadsheet.outline;

import org.netspective.io.spreadsheet.model.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultTableOutline implements TableOutline
{
    private final Table table;
    private final List<TableOutlineNode> rootNodes;

    public DefaultTableOutline(final Table table, final List<TableOutlineNode> rootNodes)
    {
        this.table = table;
        this.rootNodes = Collections.unmodifiableList(new ArrayList<TableOutlineNode>(rootNodes));
    }

    public Table getTable()
    {
        return table;
    }

    public List<TableOutlineNode> getRootNodes()
    {
        return rootNodes;
    }

}
