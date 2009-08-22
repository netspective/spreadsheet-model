package org.netspective.io.spreadsheet.outline;

import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.outline.NodeValidationRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultTableOutlineNode implements TableOutlineNode
{
    private final TableRow tableRow;
    private final int firstDataRowIndex;
    private final int lastDataRowIndex;
    private final List<TableOutlineNode> children;
    private NodeValidationRule[] nodeRules;

    public DefaultTableOutlineNode(final TableRow tableRow, final int firstDataRowIndex, final int lastDataRowIndex,
                                   final List<TableOutlineNode> children,
                                   final NodeValidationRule[] nodeRules)
    {
        this.tableRow = tableRow;
        this.firstDataRowIndex = firstDataRowIndex;
        this.lastDataRowIndex = lastDataRowIndex;
        this.children = Collections.unmodifiableList(new ArrayList<TableOutlineNode>(children));
        this.nodeRules = nodeRules;
    }

    public DefaultTableOutlineNode(final TableRow tableRow, final int firstDataRowIndex, final int lastDataRowIndex,
                                   final List<TableOutlineNode> children)
    {
        this.tableRow = tableRow;
        this.firstDataRowIndex = firstDataRowIndex;
        this.lastDataRowIndex = lastDataRowIndex;
        this.children = Collections.unmodifiableList(new ArrayList<TableOutlineNode>(children));
        this.nodeRules = new NodeValidationRule[0];
    }

    public DefaultTableOutlineNode(final TableRow tableRow, final int firstDataRowIndex, final int lastDataRowIndex,
                                   final NodeValidationRule[] nodeRules)
    {
        this.tableRow = tableRow;
        this.firstDataRowIndex = firstDataRowIndex;
        this.lastDataRowIndex = lastDataRowIndex;
        this.children = Collections.unmodifiableList(new ArrayList<TableOutlineNode>());
        this.nodeRules = nodeRules;
    }

    public DefaultTableOutlineNode(final TableRow tableRow, final int firstDataRowIndex, final int lastDataRowIndex)
    {
        this.tableRow = tableRow;
        this.firstDataRowIndex = firstDataRowIndex;
        this.lastDataRowIndex = lastDataRowIndex;
        this.children = Collections.unmodifiableList(new ArrayList<TableOutlineNode>());
        this.nodeRules = new NodeValidationRule[0];
    }

    public TableRow getTableRow()
    {
        return tableRow;
    }

    public List<TableOutlineNode> getChildren()
    {
        return children;
    }

    public NodeValidationRule[] getValidationRules()
    {
        return nodeRules;
    }

    public int getFirstDataRowIndexInTable()
    {
        return firstDataRowIndex;
    }

    public int getLastDataRowIndexInTable()
    {
        return lastDataRowIndex;
    }

    public interface DebugNodeRenderer
    {
        public String nodeToString(final int level, final TableOutlineNode node);
    }

    public static String toDebugText(final int level, final List<TableOutlineNode> nodes, final DebugNodeRenderer nodeRenderer)
    {
        final StringBuffer result = new StringBuffer();
        for(final TableOutlineNode tsn : nodes)
        {
            result.append(nodeRenderer.nodeToString(level, tsn));
            result.append(toDebugText(level + 1, tsn.getChildren(), nodeRenderer));
        }
        return result.toString();
    }
}
