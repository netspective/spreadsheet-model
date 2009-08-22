package org.netspective.io.spreadsheet.validate.outline;

import org.netspective.io.spreadsheet.outline.TableOutline;
import org.netspective.io.spreadsheet.outline.TableOutlineNode;
import org.netspective.io.spreadsheet.validate.ValidationContext;

import java.util.List;

public interface NodeValidationRule 
{
    public boolean isValid(final ValidationContext vc, final TableOutline outline, final TableOutlineNode node,
                           final List<NodeValidationMessage> messages);
}
