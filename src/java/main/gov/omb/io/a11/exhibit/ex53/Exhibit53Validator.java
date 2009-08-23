package gov.omb.io.a11.exhibit.ex53;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.netspective.io.spreadsheet.consumer.DefaultWorksheetDataHandler;
import org.netspective.io.spreadsheet.consumer.WorksheetConsumer;
import org.netspective.io.spreadsheet.consumer.WorksheetDataHandler;
import org.netspective.io.spreadsheet.message.Message;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.outline.TableOutlineNode;
import org.netspective.io.spreadsheet.util.Util;
import org.netspective.io.spreadsheet.validate.row.RowValidationMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Exhibit53Validator
{
    public static void main(final String[] args) throws ParseException, IOException, InvalidFormatException
    {
        final Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("help")
                                       .withDescription("Print options to stdout")
                                       .create('?'));

        options.addOption(OptionBuilder.withLongOpt("workbook")
                                       .withDescription("The Exhibit 53 Excel workbook file to load.")
                                       .hasArg().withArgName("file")
                                       .isRequired()
                                       .create('w'));

        options.addOption(OptionBuilder.withLongOpt("worksheet")
                                       .withDescription("The Exhibit 53 Excel workbook's worksheet name to parse. Defaults to the first worksheet.")
                                       .hasArg().withArgName("name")
                                       .create('s'));

        options.addOption(OptionBuilder.withLongOpt("agency-code")
                                       .withDescription("The OMB Agency Code to use to validate UPIs.")
                                       .hasArg().withArgName("code")
                                       .isRequired()
                                       .create('a'));

        options.addOption(OptionBuilder.withLongOpt("bureau-codes")
                                       .withDescription("The OMB Bureau Code(s) to use to validate UPIs. For multiple codes, separate using commas.")
                                       .hasArg().withArgName("code")
                                       .create('b'));

        options.addOption(OptionBuilder.withLongOpt("budget-year")
                                       .withDescription("The budget year to use.")
                                       .hasArg().withArgName("year")
                                       .isRequired()
                                       .create('y'));

        options.addOption(OptionBuilder.withLongOpt("no-validate-subtotals")
                                       .withDescription("Set this flag to skill all subtotals calculation validations.")
                                       .create('d'));

        options.addOption(OptionBuilder.withLongOpt("debug")
                                       .withDescription("Show debugging messages.")
                                       .create('d'));

        final CommandLineParser parser = new PosixParser();
        CommandLine commandLine;
        try
        {
            commandLine = parser.parse(options, args);

            if(commandLine.hasOption('w') && commandLine.hasOption('a') && commandLine.hasOption('y'))
            {
                final String workbookFileName = commandLine.getOptionValue("workbook");
                final File workBookFile = new File(workbookFileName);
                if(! workBookFile.exists() && workBookFile.isFile())
                {
                    System.err.printf("Workbook file '%s' does not exist.", workbookFileName);
                    return;
                }

                if(! workBookFile.getName().endsWith(".xls"))
                {
                    System.err.printf("Only Excel 2003 *.xls workbooks are supported.\nUnable to validate '%s'.", workbookFileName);
                    return;
                }

                final String agencyCode = commandLine.getOptionValue("agency-code");
                final String bureauCodes = commandLine.getOptionValue("bureau-codes");
                final String budgetYear = commandLine.getOptionValue("budget-year");
                final boolean debug = commandLine.hasOption("debug");

                final Workbook workbook;
                try
                {
                    workbook = WorkbookFactory.create(new FileInputStream(workBookFile));
                }
                catch (Exception e)
                {
                    System.err.printf("Unable to open to '%s'. [%s: %s]", workbookFileName, e.getClass().getName(), e.getMessage());
                    return;
                }

                final Sheet sheet;
                if(commandLine.hasOption("worksheet"))
                {
                    final String worksheetName = commandLine.getOptionValue("worksheet");
                    sheet = workbook.getSheet(worksheetName);
                    if(sheet == null)
                    {
                        System.err.printf("Unable to locate worksheet '%s' in workbook '%s'.", worksheetName, workbookFileName);
                        return;
                    }
                }
                else
                    sheet = workbook.getSheetAt(0);

                final String[] splitBureauCodes = bureauCodes == null ? null : bureauCodes.split(",");
                final List<String> bureauCodesList;
                if(splitBureauCodes != null)
                {
                    bureauCodesList = new ArrayList<String>();
                    bureauCodesList.addAll(Arrays.asList(splitBureauCodes));
                }
                else
                    bureauCodesList = null;

                final DefaultExhibit53Parameters parameters = new DefaultExhibit53Parameters(Integer.parseInt(budgetYear), agencyCode, bureauCodesList);
                if(commandLine.hasOption("no-validate-subtotals"))
                    parameters.setValidateAnySubtotals(false);

                final Exhibit53WorksheetTemplate template = new Exhibit53WorksheetTemplate(parameters);
                final WorksheetDataHandler exhibit53DataHandler = new DefaultWorksheetDataHandler(9, 2, 17, new int[] { 2, 3 });

                System.out.printf("Validating File: \"%s\".\n", workBookFile.getAbsoluteFile());
                System.out.printf("Validating Sheet: \"%s\".\n", sheet.getSheetName());

                final WorksheetConsumer consumer = new WorksheetConsumer(template, exhibit53DataHandler, template, template, sheet);
                final WorksheetConsumer.TemplateValidationResult tvr = consumer.validateTemplate();
                System.out.printf("[** Phase 1 **] Template validation completed. Total: %d error(s), %d warning(s).\n", tvr.getErrors().length, tvr.getWarnings().length);
                if(! tvr.isValid() || tvr.hasWarnings())
                {
                    showMessages(System.err, "E", tvr.getErrors());
                    showMessages(System.err, "W", tvr.getWarnings());
                    if(! tvr.isValid())
                    {
                        System.out.printf("[** Aborting **] Phases 2, 3, and 4 validation will not be performed due to template validation errors.");
                        return;
                    }
                }

                final WorksheetConsumer.DataValidationResult dvr = consumer.validateData();
                System.out.printf("[** Phase 2 **] Worksheet data validation completed. Rows processed: %d. Issues: %d row(s) with errors, %d with warnings.\n", dvr.getTable().getRows().size(), dvr.getErrors().length, dvr.getWarnings().length);
                if(! dvr.isValid() || tvr.hasWarnings())
                {
                    showMessages(System.err, "E", dvr.getErrors());
                    showMessages(System.err, "W", dvr.getWarnings());
                    if(! dvr.isValid())
                    {
                        System.out.printf("[** Aborting **] Phases 3 and 4 validation will not be performed due to worksheet data validation errors.");
                        return;
                    }
                }

                final WorksheetConsumer.OutlineStructureValidationResult osvr = consumer.validateOutlineStructure(dvr.getTable());
                System.out.printf("[** Phase 3 **] Exhibit 53 Part/Mission Area/Investment structure validation completed. Nodes: %d, Issues: %d node(s) with errors, %d with warnings.\n", osvr.getTableOutline().getRootNodes().size(), osvr.getErrors().length, osvr.getWarnings().length);
                if(! osvr.isValid() || osvr.hasWarnings())
                {
                    showMessages(System.err, "E", osvr.getErrors());
                    showMessages(System.err, "W", osvr.getWarnings());
                    if(! osvr.isValid())
                    {
                        System.out.printf("[** Aborting **] Phase 4 validation will not be performed due to Exhibit 53 Part/Mission Area/Investment structure validation errors.");
                        return;
                    }
                }

                final WorksheetConsumer.OutlineDataValidationResult odvr = consumer.validateOutlineData(osvr.getTableOutline());
                System.out.printf("[** Phase 4 **] Exhibit 53 Part/Mission Area/Investment data validation completed. Issues: %d rows(s) with errors, %d with warnings.\n", odvr.getErrors().length, odvr.getWarnings().length);
                if(! odvr.isValid() || odvr.hasWarnings())
                {
                    showMessages(System.err, "E", odvr.getErrors());
                    showMessages(System.err, "W", odvr.getWarnings());
                }

                System.out.printf("[** Completed **] All four phases of validation are complete.\n");
                if(debug)
                    System.out.println(Util.renderNodes(0, odvr.getTableOutline().getRootNodes(), new DebuggingRenderer()));
            }
        }
        catch (MissingOptionException e)
        {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "Exhibit53Validator", options);
        }
    }

    public static void showMessages(final PrintStream stream, final String type, final Message[] messages)
    {
        for(final Message m : messages)
        {
            stream.printf("{%s} [%s] %s\n", type, m.getCode(), m.getMessage());
            if(m instanceof RowValidationMessage)
                for(final Message cm : ((RowValidationMessage) m).getCellValidationErrors())
                    stream.printf("     [%s] %s\n", cm.getCode(), cm.getMessage());
        }
    }

    public static class DebuggingRenderer implements Util.DebugNodeRenderer
    {
        public String nodeToString(final int level, final TableOutlineNode node)
        {
            final StringBuffer indent = new StringBuffer();
            for(int i = 0; i < level; i++)
                indent.append("    ");

            final TableRow row = node.getTableRow();
            final List<TableCell> cells = row.getCells();
            return String.format("%03d [%s] %s %s (%s)\n", row.getRowNumberInSheet(), cells.get(1).getValue("NO_UPI"), indent,
                    cells.get(2).getValue("No Investment Title"),
                    cells.get(3).getValue("No Description"));
        }
    }
}