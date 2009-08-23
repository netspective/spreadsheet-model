package gov.omb.io.a11.exhibit.ex53;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.netspective.io.spreadsheet.message.Message;
import org.netspective.io.spreadsheet.model.TableCell;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.outline.TableOutlineNode;
import org.netspective.io.spreadsheet.util.Util;
import org.netspective.io.spreadsheet.validate.row.RowValidationMessage;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Exhibit53Validator
{
    public static void main(final String[] args) throws ParseException, IOException, InvalidFormatException
    {
        final Options options = new Options();
        options.addOption(new Option("?", "help", false, "See summary of usage."));
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

            new Exhibit53WorksheetConsumer(parameters, new ValidationStageHandler()).consume();
        }
        catch (ParseException e)
        {
            System.err.printf("** ERROR **: %s\n", e.getMessage());
            showHelp(options);
        }
    }

    public static void showHelp(final Options options)
    {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);
        formatter.printHelp( "Exhibit53Validator --workbook-name Exhibit53.xls --budget-year=2011 --agency-code=123", options);
    }

    public static class ValidationStageHandler implements Exhibit53WorksheetConsumer.ValidationStageHandler
    {
        public void startStage(final Exhibit53WorksheetConsumer.ValidationStage stage)
        {
            System.out.printf(" Start [%s] %s.\n", stage.name(), stage.getDescription());
        }

        public void completeStage(final Exhibit53WorksheetConsumer.ValidationStage stage, final Message[] warnings)
        {
            if(warnings.length > 0)
                System.out.printf("Finish [%s] %d warning(s) encountered.\n", stage.name(), warnings.length);
            showMessages(System.out, stage, "W", warnings);
        }

        public void completeStage(final Exhibit53WorksheetConsumer.ValidationStage stage, final Message[] errors, final Message[] warnings)
        {
            System.out.printf("Finish [%s] %d error(s) encountered, %d warning(s).\n", stage.name(), errors.length, warnings.length);
            showMessages(System.err, stage, " ERROR", errors);
            showMessages(System.out, stage, "  WARN", warnings);

            boolean first = true;
            for(Exhibit53WorksheetConsumer.ValidationStage unhandledStage : Exhibit53WorksheetConsumer.ValidationStage.values())
            {
                if(unhandledStage.ordinal() > stage.ordinal() && unhandledStage.ordinal() != Exhibit53WorksheetConsumer.ValidationStage.FINAL.ordinal())
                {
                    if(first)
                    {
                        System.out.printf("Validation incomplete. The following validations were not performed due to errors:\n");
                        first = false;
                    }

                    System.out.printf(" * [%s] %s.\n", unhandledStage.name(), unhandledStage.getDescription());
                }
            }
        }

        public void completeFinalStage(final Exhibit53Parameters parameters, final Exhibit53WorksheetTemplate.Exhibit53 exhibit53)
        {
            if(parameters.isDebug())
                System.out.println(Util.renderNodes(0, exhibit53.getRootNodes(), new DebuggingRenderer()));
        }

        public void showMessages(final PrintStream stream, final Exhibit53WorksheetConsumer.ValidationStage stage, final String type, final Message[] messages)
        {
            for(final Message m : messages)
            {
                stream.printf("%s [%s] %s\n", type, m.getCode(), m.getMessage());
                if(m instanceof RowValidationMessage)
                    for(final Message cm : ((RowValidationMessage) m).getCellValidationErrors())
                        stream.printf("       [%s] %s\n", cm.getCode(), cm.getMessage());
            }
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