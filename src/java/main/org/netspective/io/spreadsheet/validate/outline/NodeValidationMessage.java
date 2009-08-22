package org.netspective.io.spreadsheet.validate.outline;

import org.netspective.io.spreadsheet.outline.TableOutline;
import org.netspective.io.spreadsheet.outline.TableOutlineNode;
import org.netspective.io.spreadsheet.validate.row.RowValidationMessage;

public interface NodeValidationMessage extends RowValidationMessage
{
    TableOutline getOutline();

    TableOutlineNode getNode();
}
