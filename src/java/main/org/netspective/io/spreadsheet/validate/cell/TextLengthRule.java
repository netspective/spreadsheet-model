package org.netspective.io.spreadsheet.validate.cell;

import org.apache.poi.ss.usermodel.Cell;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationContext;

import java.util.List;

public class TextLengthRule implements CellValidationRule
{
    private final String messageCode;
    private final CellTypeRule stringTypeRule;
    private final int minLength;
    private final int maxLength;
    private final String invalidMessageFormatSpec;

    public TextLengthRule(final String messageCode, final int maxLength, final String invalidMessageFormatSpec)
    {
        this.messageCode = messageCode;
        this.stringTypeRule = new CellTypeRule(messageCode, Cell.CELL_TYPE_STRING);
        this.minLength = 0;
        this.maxLength = maxLength;
        this.invalidMessageFormatSpec = invalidMessageFormatSpec;
    }

    public TextLengthRule(final String messageCode, final int minLength, final int maxLength, final String invalidMessageFormatSpec)
    {
        this.messageCode = messageCode;
        this.stringTypeRule = new CellTypeRule(messageCode, Cell.CELL_TYPE_STRING);
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.invalidMessageFormatSpec = invalidMessageFormatSpec;
    }

    public boolean isValid(final ValidationContext vc, final Table table, final TableRow row, final TableCell cell, final List<CellValidationMessage> messages)
    {
        if(! stringTypeRule.isValid(vc, table, row, cell, messages))
            return false;

        final Cell poiCell = cell.getCell();
        final String value = poiCell.getStringCellValue();
        if(value != null && value.length() >= minLength && value.length() <= maxLength)
            return true;

        messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, invalidMessageFormatSpec, vc.getValidationMessageCellLocator(cell, true), value, value != null ? value.length() : 0, minLength, maxLength));

        return false;
    }
}
