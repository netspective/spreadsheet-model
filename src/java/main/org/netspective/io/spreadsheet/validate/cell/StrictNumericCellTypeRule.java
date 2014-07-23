package org.netspective.io.spreadsheet.validate.cell;

import org.apache.poi.ss.usermodel.Cell;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationContext;

import java.util.List;

public class StrictNumericCellTypeRule extends CellTypeRule {

    public StrictNumericCellTypeRule(final String messageCode, final int expectedType, boolean allowBlankAlso) {
        super(messageCode, expectedType, allowBlankAlso);
    }

    public boolean isValid(final ValidationContext vc, final Table table, final TableRow row, final TableCell tableCell,
            final List<CellValidationMessage> messages) {
        final Cell poiCell = tableCell.getCell();
        final int cellType = poiCell.getCellType();
        if (cellType == Cell.CELL_TYPE_NUMERIC || cellType == Cell.CELL_TYPE_FORMULA || (cellType == Cell.CELL_TYPE_BLANK && allowBlankAlso)) {
            return true;
        }

        messages.add(new DefaultCellValidationMessage(table, row, tableCell, messageCode, String.format("Expected Numeric or Formula cell type instead of type %s in cell %s%s.", getCellTypeName(cellType), tableCell.getColumn().getColumnLetters(), tableCell.getCell().getRowIndex() + 1)));

        return false;
    }
}
