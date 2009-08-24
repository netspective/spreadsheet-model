package gov.omb.io.a11.exhibit.ex53;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.netspective.io.spreadsheet.message.Message;
import org.netspective.io.spreadsheet.util.Util;
import org.netspective.io.spreadsheet.validate.cell.CellValidationMessage;
import org.netspective.io.spreadsheet.validate.row.RowValidationMessage;

import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelWorkbookValidationReporter implements Exhibit53WorksheetConsumer.ValidationStageHandler
{
    private Exhibit53Parameters parameters;
    private Sheet errorsSheet;
    private Sheet warningsSheet;
    private CellStyle cellErrorStyle;
    private CellStyle cellHyperlinkStyle;
    private CreationHelper createHelper;

    public ExcelWorkbookValidationReporter(final Exhibit53Parameters parameters)
    {
        this.parameters = parameters;

        final Workbook workbook = parameters.getWorkbook();
        createHelper = workbook.getCreationHelper();

        this.errorsSheet = workbook.createSheet("Validation Errors");
        this.warningsSheet= workbook.createSheet("Validation Warnings");

        setupSheet(errorsSheet, "Errors");
        setupSheet(warningsSheet, "Warnings");

        // Style the cell with borders all around.
        cellErrorStyle = workbook.createCellStyle();
        cellErrorStyle.setBorderBottom(CellStyle.BORDER_THICK);
        cellErrorStyle.setBottomBorderColor(IndexedColors.DARK_RED.getIndex());
        cellErrorStyle.setBorderLeft(CellStyle.BORDER_THICK);
        cellErrorStyle.setLeftBorderColor(IndexedColors.DARK_RED.getIndex());
        cellErrorStyle.setBorderRight(CellStyle.BORDER_THICK);
        cellErrorStyle.setRightBorderColor(IndexedColors.DARK_RED.getIndex());
        cellErrorStyle.setBorderTop(CellStyle.BORDER_THICK);
        cellErrorStyle.setTopBorderColor(IndexedColors.DARK_RED.getIndex());

        cellHyperlinkStyle = workbook.createCellStyle();
        Font hlinkFont = workbook.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        cellHyperlinkStyle.setFont(hlinkFont);
    }

    public String[] write() throws IOException
    {
        for(int i = 0; i < 5; i++)
        {
            errorsSheet.autoSizeColumn(i);
            warningsSheet.autoSizeColumn(i);
        }

        final String reportFileName = parameters.getReportWorkbookName();
        final FileOutputStream os = new FileOutputStream(reportFileName);
        parameters.getWorkbook().write(os);
        os.close();

        return new String[] { reportFileName, errorsSheet.getSheetName(), warningsSheet.getSheetName() };
    }

    public void setupSheet(final Sheet sheet, final String title)
    {
        sheet.createRow(0).createCell(0).setCellValue(String.format("%s in sheet '%s'", title, parameters.getSheet().getSheetName()));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        createRow(sheet, null, "Stage", "Code", "Row", "Cell", "Message");
    }

    public Row createRow(final Sheet sheet, final Hyperlink hyperlink, Object ...cells)
    {
        final Row row = sheet.createRow(sheet.getLastRowNum()+1);
        int cellNum = 0;
        for(final Object s : cells)
        {
            final Cell cell = row.createCell(cellNum);
            cell.setCellValue(s.toString());
            if(hyperlink != null && cellNum == 3)
            {
                cell.setHyperlink(hyperlink);
                cell.setCellStyle(cellHyperlinkStyle);
            }
            cellNum++;
        }
        return row;
    }

    public void recordMessage(final Exhibit53WorksheetConsumer.ValidationStage stage, final Sheet sheet, final Message message)
    {
        if(message instanceof RowValidationMessage)
        {
            final RowValidationMessage rvm = (RowValidationMessage) message;
            createRow(sheet, null, stage.name(), message.getCode(), rvm.getRow().getRowNumberInSheet(), "", message.getMessage());
            for(final CellValidationMessage cm : rvm.getCellValidationErrors())
            {
                final Cell cell = cm.getCell().getCell();
                final String locator = Util.getCellLocator(cell);

                final Hyperlink hl = createHelper.createHyperlink(Hyperlink.LINK_DOCUMENT);
                hl.setAddress(String.format("'%s'!%s", parameters.getSheet(), locator));
                createRow(sheet, hl, stage.name(), cm.getCode(), rvm.getRow().getRowNumberInSheet(), locator, cm.getMessage());

                cell.setCellStyle(cellErrorStyle);
            }
        }
        else
            createRow(sheet, null, stage.name(), message.getCode(), "", "", message.getMessage());
    }

    public void recordMessages(final Exhibit53WorksheetConsumer.ValidationStage stage, final Sheet sheet, final Message[] messages)
    {
        for(final Message m : messages)
            recordMessage(stage, sheet, m);
    }

    public void startStage(final Exhibit53WorksheetConsumer.ValidationStage stage)
    {

    }

    public void completeStage(final Exhibit53WorksheetConsumer.ValidationStage stage, final Message[] warnings)
    {
        recordMessages(stage, warningsSheet, warnings);
    }

    public void completeStage(final Exhibit53WorksheetConsumer.ValidationStage stage, final Message[] errors, final Message[] warnings)
    {
        recordMessages(stage, errorsSheet, errors);
        recordMessages(stage, warningsSheet, warnings);

        boolean first = true;
        for(Exhibit53WorksheetConsumer.ValidationStage unhandledStage : Exhibit53WorksheetConsumer.ValidationStage.values())
        {
            if(unhandledStage.ordinal() > stage.ordinal() && unhandledStage.ordinal() != Exhibit53WorksheetConsumer.ValidationStage.FINAL.ordinal())
            {
                if(first)
                {
                    errorsSheet.createRow(errorsSheet.getLastRowNum()+1); // blank row
                    final Row row = createRow(errorsSheet, null, "Validation incomplete. The following validations were not performed due to errors:\n");
                    errorsSheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 4));
                    first = false;
                }

                final Row row = createRow(errorsSheet, null, unhandledStage.name(), unhandledStage.description());
                errorsSheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 4));
            }
        }
    }

    public void completeFinalStage(final Exhibit53Parameters parameters, final Exhibit53WorksheetTemplate.Exhibit53 exhibit53)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
