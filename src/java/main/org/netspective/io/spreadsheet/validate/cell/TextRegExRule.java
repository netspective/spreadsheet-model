package org.netspective.io.spreadsheet.validate.cell;

import org.apache.poi.ss.usermodel.Cell;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationContext;

import java.util.List;

public class TextRegExRule implements CellValidationRule
{
    private final String messageCode;
    private final CellTypeRule stringTypeRule;
    private final String regExpr;
    private final String invalidMessageFormatSpec;

    public TextRegExRule(final String messageCode, final String regExpr, final String invalidMatchMessageFormat)
    {
        this.messageCode = messageCode;
        this.stringTypeRule = new CellTypeRule(messageCode, Cell.CELL_TYPE_STRING);
        this.regExpr = regExpr;
        this.invalidMessageFormatSpec = invalidMatchMessageFormat;
    }

    public boolean isValid(final ValidationContext vc, final Table table, final TableRow row, final TableCell cell, final List<CellValidationMessage> messages)
    {
        if(! stringTypeRule.isValid(vc, table, row, cell, messages))
            return false;

        final Cell poiCell = cell.getCell();
        final String value = poiCell.getStringCellValue();
        if(value.length() == 0)
            return true;

        if(! value.matches(regExpr))
        {
            messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidMessageFormatSpec, vc.getValidationMessageCellLocator(cell, true), value, regExpr));
            return false;
        }

        return true;
    }

}
