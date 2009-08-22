package org.netspective.io.spreadsheet;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.netspective.io.spreadsheet.consumer.DefaultWorksheetDataHandler;
import org.netspective.io.spreadsheet.consumer.WorksheetConsumer;
import org.netspective.io.spreadsheet.consumer.WorksheetDataHandler;
import org.netspective.io.spreadsheet.message.Message;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;
import org.netspective.io.spreadsheet.validate.row.RowValidationMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;


public class WorksheetConsumerTest
{
    public Sheet getWorksheetFromTestFile(final String sheetName) throws IOException, InvalidFormatException
    {
        final String resource = '/' + WorksheetConsumerTest.class.getName().replace('.', '/') + ".xls";
        final InputStream is = getClass().getResourceAsStream(resource);

        assertNotNull(String.format("This test requires a workbook called '%s' to be available in the classpath.", resource), is);

        final Workbook workbook = WorkbookFactory.create(is);
        final Sheet sheet = workbook.getSheet(sheetName);
        assertNotNull(String.format("This test requires a sheet called '%s'", sheetName), sheet);

        return sheet;
    }

    @Test
    public void testInvalidEmpty() throws IOException, InvalidFormatException
    {
        final Sheet sheet = getWorksheetFromTestFile("Invalid - Empty");

        final int headingRowNumber = 3;
        final WorksheetTemplate template = new FlatSimpleWorksheetTemplate(FlatSimpleWorksheetTemplate.NO_GROUP, headingRowNumber);
        final WorksheetDataHandler dataHandler = new DefaultWorksheetDataHandler(5, sheet.getLastRowNum(), 2, 11);

        final WorksheetConsumer consumer = new WorksheetConsumer(template, dataHandler, null, null, sheet);
        final WorksheetConsumer.TemplateValidationResult tvr = consumer.validateTemplate();

        assertFalse("This is an empty spreadsheet so the template validation should be invalid.", tvr.isValid());
        assertEquals("All the columns are invalid so we should have errors", 10, tvr.getErrors().length);
    }

    @Test
    public void testValidSimpleUngrouped() throws IOException, InvalidFormatException
    {
        final Sheet sheet = getWorksheetFromTestFile("Valid - Simple Ungrouped");
        assertEquals("Make sure we have data rows", 17, sheet.getLastRowNum());

        final int headingRowNumber = 3;
        final WorksheetTemplate template = new FlatSimpleWorksheetTemplate(FlatSimpleWorksheetTemplate.NO_GROUP, headingRowNumber);
        final WorksheetDataHandler dataHandler = new DefaultWorksheetDataHandler(4, sheet.getLastRowNum(), 2, 11);

        final WorksheetConsumer consumer = new WorksheetConsumer(template, dataHandler, null, null, sheet);
        final WorksheetConsumer.TemplateValidationResult tvr = consumer.validateTemplate();
        assertTrue("This should be a valid sheet.", tvr.isValid());

        final WorksheetConsumer.DataValidationResult dvr = consumer.validateData();
        assertTrue("This should have valid data.", dvr.isValid());
        assertEquals("This should have data rows.", 15, dvr.getTable().getRows().size());
    }

    @Test
    public void testValidSimpleGrouped() throws IOException, InvalidFormatException
    {
        final Sheet sheet = getWorksheetFromTestFile("Valid - Simple Grouped");
        assertEquals("Make sure we have data rows", 17, sheet.getLastRowNum());

        final int groupRowNumber = 2;
        final int headingRowNumber = 3;
        final WorksheetTemplate template = new FlatSimpleWorksheetTemplate(groupRowNumber, headingRowNumber);
        final WorksheetDataHandler dataHandler = new DefaultWorksheetDataHandler(4, sheet.getLastRowNum(), 2, 11);

        final WorksheetConsumer consumer = new WorksheetConsumer(template, dataHandler, null, null, sheet);
        final WorksheetConsumer.TemplateValidationResult tvr = consumer.validateTemplate();
        assertTrue("This should be a valid sheet.", tvr.isValid());
                
        final WorksheetConsumer.DataValidationResult dvr = consumer.validateData();
        assertTrue("This should have valid data.", dvr.isValid());
        assertEquals("This should have data rows.", 15, dvr.getTable().getRows().size());
    }

    public static void showMessages(final PrintStream stream, final String type, final Message[] messages)
    {
        for(final Message m : messages)
        {
            stream.printf("{%s} %s\n", type, m.getMessage());
            if(m instanceof RowValidationMessage)
                for(final Message cm : ((RowValidationMessage) m).getCellValidationErrors())
                    stream.printf("{%s}     %s\n", type, cm.getMessage());
        }
    }
}
