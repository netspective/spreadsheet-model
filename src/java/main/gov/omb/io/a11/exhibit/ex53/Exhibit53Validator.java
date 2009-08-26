package gov.omb.io.a11.exhibit.ex53;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.netspective.io.spreadsheet.PackageAttributes;
import org.netspective.io.spreadsheet.consumer.MultipleValidationStageHandlers;
import org.netspective.io.spreadsheet.consumer.WorksheetConsumer;
import org.netspective.io.spreadsheet.consumer.WorksheetConsumerStageHandler;
import org.netspective.io.spreadsheet.consumer.WorksheetDataHandler;
import org.netspective.io.spreadsheet.message.Message;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.outline.TableOutline;
import org.netspective.io.spreadsheet.outline.TableOutlineNode;
import org.netspective.io.spreadsheet.util.Util;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Exhibit53Validator
{
    public static void main(final String[] args) throws ParseException, IOException, InvalidFormatException
    {
        final FEACodesManager feaCodesManager = FEACodesManager.getInstance();
        if(! feaCodesManager.isValid())
        {
            for(final Message m : feaCodesManager.getErrors())
                System.err.printf("[%s] %s\n", m.getCode(), m.getMessage());
            System.err.printf("Unable to run Exhibit 53 validator without FEA codes.\n");
            return;
        }
        else
            System.err.printf("Cached %d LOB/Svc Type codes and %d Sub-function/Components codes.\n",
                    feaCodesManager.getLineOfBusinessOrServiceTypesCache().size(),
                    feaCodesManager.getSubFunctionOrSvcComponentsCache().size());            

        final Options options = new Options();
        options.addOption(new Option("?", "help", false, "See summary of usage."));
        options.addOption(new Option("l", "show-fea-lob-codes", false, "Show the cached FEA LOB codes."));
        options.addOption(new Option("s", "show-fea-subf-codes", false, "Show the cached FEA Subfunction codes."));

        for(final DefaultExhibit53Parameters.Parameter param : DefaultExhibit53Parameters.PARAMETERS)
        {
            final Option option = new Option(null, param.getName(), ! param.getArgument().isFlag(), "");
            option.setArgName(param.getArgument().getName());
            if(param.isRequired())
                option.setRequired(true);
            options.addOption(option);
        }
        
        final CommandLineParser parser = new PosixParser();
        CommandLine commandLine;
        try
        {
            commandLine = parser.parse(options, args);
            if(commandLine.hasOption("help"))
            {
                showHelp(options);
                return;                
            }

            if(commandLine.hasOption("show-fea-lob-codes") || commandLine.hasOption("show-fea-subf-codes"))
            {
                showFEACodes(commandLine);
                return;
            }

            final Map<String, String> parameterValues = new HashMap<String, String>();
            for(final Option option : commandLine.getOptions())
                parameterValues.put(option.getLongOpt(), option.getValue());

            final DefaultExhibit53Parameters parameters = new DefaultExhibit53Parameters(parameterValues);
            if(! parameters.isValid())
            {
                for(final String error : parameters.getErrors())
                    System.err.println(error);
                return;
            }

            final ExcelWorkbookValidationReporter workbookValidationReporter = new ExcelWorkbookValidationReporter(parameters);
            final MultipleValidationStageHandlers handlers = new MultipleValidationStageHandlers(new SimpleValidationReporter(parameters), workbookValidationReporter);

            final Exhibit53WorksheetTemplate template = new Exhibit53WorksheetTemplate(parameters);
            final WorksheetDataHandler exhibit53DataHandler = new DefaultExhibit53WorksheetDataHandler();
            final WorksheetConsumer consumer = new WorksheetConsumer(parameters.getSheet(), template, exhibit53DataHandler, template, template);

            consumer.consume(handlers);

            final String[] workbookAndSheetNames = workbookValidationReporter.write();
            System.out.printf("\nCreated error report file in %s.\n", workbookAndSheetNames[0]);
            System.out.printf(" * Cells with errors are marked with red border in '%s' sheet.\n", parameters.getSheet().getSheetName());
            System.out.printf(" * Errors are reported in the '%s' sheet.\n", workbookAndSheetNames[1]);
            System.out.printf(" * Warnings are reported in the '%s' sheet.\n", workbookAndSheetNames[2]);
        }
        catch (ParseException e)
        {
            System.err.printf("** ERROR **: %s\n", e.getMessage());
            showHelp(options);
        }
    }

    public static void showFEACodes(final CommandLine commandLine)
    {
        if(commandLine.hasOption("show-fea-lob-codes"))
        {
            System.out.println("FEA Line of Business codes");
            for(final Map.Entry entry : FEACodesManager.getInstance().getLineOfBusinessOrServiceTypesCache().entrySet())
                System.out.printf("[%s] %s\n", entry.getKey(), entry.getValue());
        }

        if(commandLine.hasOption("show-fea-subf-codes"))
        {
            System.out.println("FEA Subfunction codes");
            for(final Map.Entry entry : FEACodesManager.getInstance().getSubFunctionOrSvcComponentsCache().entrySet())
                System.out.printf("[%s] %s\n", entry.getKey(), entry.getValue());
        }
    }

    public static void showHelp(final Options options)
    {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);
        final PackageAttributes packageAttrs = PackageAttributes.PACKAGE;
        formatter.printHelp(String.format("Exhibit53Validator %s --workbook-name Exhibit53.xls --budget-year=2011 --agency-code=123", packageAttrs.getVersion()), options);
        System.out.printf("The library was built on %s.\n", packageAttrs.getBuildDate());
    }

    public static class SimpleValidationReporter implements WorksheetConsumerStageHandler
    {
        private Exhibit53Parameters parameters;

        public SimpleValidationReporter(final Exhibit53Parameters parameters)
        {
            this.parameters = parameters;
        }

        public void startConsumption()
        {
        }

        public void startStage(final Stage stage)
        {
            System.out.printf("   Work [%s] %s.\n", stage.name(), stage.description());
        }

        public void completeStage(final Stage stage, final Message[] warnings)
        {
            if(warnings.length > 0)
                System.out.printf("Message [%s] %d warning(s) encountered.\n", stage.name(), warnings.length);
            showMessages(System.out, stage, "   WARN", warnings);
        }

        public void completeStage(final Stage stage, final Message[] errors, final Message[] warnings)
        {
            System.out.printf("Problem [%s] %d error(s) encountered, %d warning(s).\n", stage.name(), errors.length, warnings.length);
            showMessages(System.err, stage, "  ERROR", errors);
            showMessages(System.out, stage, "   WARN", warnings);

            boolean first = true;
            for(final Stage unhandledStage : stage.remaining(true))
            {
                if(first)
                {
                    System.out.printf("Validation incomplete. The following validations were not performed due to errors:\n");
                    first = false;
                }

                System.out.printf(" * [%s] %s.\n", unhandledStage.name(), unhandledStage.description());
            }
        }

        public void endConsumption(final boolean successful, final Table table, final TableOutline outline)
        {
            if(successful && outline != null && parameters.isDebug())
                System.out.println(Util.renderNodes(0, outline.getRootNodes(), new DebuggingRenderer()));
        }

        public void showMessages(final PrintStream stream, final Stage stage, final String type, final Message[] messages)
        {
            if(! parameters.isDebug())
                return;

            for(final Message m : messages)
                stream.printf("%s [%s] %s\n", type, m.getCode(), m.getMessage());
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