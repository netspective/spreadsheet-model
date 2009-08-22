package gov.omb.io.a11.exhibit.ex53;

import gov.omb.data.format.micro.DataFormatFactory;
import gov.omb.data.format.micro.UPI;
import org.apache.poi.ss.usermodel.Cell;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.validate.cell.CellTypeRule;
import org.netspective.io.spreadsheet.validate.cell.CellValidationMessage;
import org.netspective.io.spreadsheet.validate.cell.CellValidationRule;
import org.netspective.io.spreadsheet.validate.cell.DefaultCellValidationMessage;

import java.util.List;

public class UpiRule implements CellValidationRule
{
    private final CellTypeRule stringTypeRule;
    private final int budgetYear;
    private final String agencyCode;
    private final List<String> bureauCodes;
    private final String messageCode;

    public UpiRule(final String messageCode, final int budgetYear, final String agencyCode, final List<String> bureauCodes)
    {
        this.stringTypeRule = new CellTypeRule(messageCode, Cell.CELL_TYPE_STRING);
        this.budgetYear = budgetYear;
        this.agencyCode = agencyCode;
        this.bureauCodes = bureauCodes;
        this.messageCode = messageCode;
    }

    public boolean isValid(final ValidationContext vc, final Table table, final TableRow row, final TableCell cell, final List<CellValidationMessage> messages)
    {
        if(! stringTypeRule.isValid(vc, table, row, cell, messages))
            return false;

        final Cell poiCell = cell.getCell();
        final String rawUPI = poiCell.getStringCellValue();
        if(rawUPI.length() == 0)
            return true;

        final DataFormatFactory dataFmtFactory = DataFormatFactory.getInstance();
        final UPI upi;
        if(agencyCode != null && (bureauCodes != null && bureauCodes.size() > 0))
            upi = dataFmtFactory.createUPI(budgetYear, rawUPI, agencyCode, bureauCodes);
        else if(agencyCode != null)
            upi = dataFmtFactory.createUPI(budgetYear, rawUPI, agencyCode);
        else
            upi = dataFmtFactory.createUPI(budgetYear, rawUPI);

        if(! upi.isValid())
        {
            for(final String s : upi.getIssues())
                messages.add(new DefaultCellValidationMessage(table, row, cell, messageCode, "In %s %s", vc.getValidationMessageCellLocator(cell, true), s));

            return false;
        }

        return true;
    }

}
