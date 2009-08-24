package org.netspective.io.spreadsheet.consumer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.netspective.io.spreadsheet.cache.CacheManager;
import org.netspective.io.spreadsheet.message.Message;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.model.TableRow;
import org.netspective.io.spreadsheet.outline.TableOutline;
import org.netspective.io.spreadsheet.outline.TableOutlineCreator;
import org.netspective.io.spreadsheet.template.WorksheetTemplate;
import org.netspective.io.spreadsheet.validate.ValidationContext;
import org.netspective.io.spreadsheet.validate.outline.NodeValidationMessage;
import org.netspective.io.spreadsheet.validate.outline.OutlineValidationMessage;
import org.netspective.io.spreadsheet.validate.row.RowValidationMessage;
import org.netspective.io.spreadsheet.validate.row.RowValidationRule;
import org.netspective.io.spreadsheet.validate.template.TemplateValidationMessage;
import org.netspective.io.spreadsheet.validate.template.TemplateValidationRule;

import java.util.ArrayList;
import java.util.List;

public class WorksheetConsumer
{
    public interface TemplateValidationResult
    {
        public boolean isValid();
        public boolean hasWarnings();
        public TemplateValidationMessage[] getErrors();
        public TemplateValidationMessage[] getWarnings();
    }

    public interface DataValidationResult
    {
        public Table getTable();
        public boolean isValid();
        public boolean hasWarnings();
        public RowValidationMessage[] getErrors();
        public RowValidationMessage[] getWarnings();
    }

    public interface OutlineStructureValidationResult
    {
        public TableOutline getTableOutline();

        public boolean isValid();
        public boolean hasWarnings();
        public OutlineValidationMessage[] getErrors();
        public OutlineValidationMessage[] getWarnings();
    }

    public interface OutlineDataValidationResult
    {
        public TableOutline getTableOutline();

        public boolean isValid();
        public boolean hasWarnings();
        public NodeValidationMessage[] getErrors();
        public NodeValidationMessage[] getWarnings();
    }

    private final WorksheetTemplate worksheetTemplate;
    private final WorksheetDataHandler dataHandler;
    private final CacheManager cacheManager;
    private final TableOutlineCreator outlineCreator;
    private final Sheet sheet;
    private final WorksheetConsumerStageHandler stageHandler;
    private WorksheetConsumerStageHandler.Stage stage = WorksheetConsumerStageHandler.Stage.INITIAL;

    public WorksheetConsumer(final Sheet sheet, final WorksheetTemplate worksheetTemplate,
                             final WorksheetDataHandler dataHandler,
                             final CacheManager cacheManager,
                             final WorksheetConsumerStageHandler stageHandler)
    {
        this.sheet = sheet;
        this.worksheetTemplate = worksheetTemplate;
        this.dataHandler = dataHandler;
        this.cacheManager = cacheManager;
        this.stageHandler = stageHandler;
        this.outlineCreator = null;
    }

    public WorksheetConsumer(final Sheet sheet, final WorksheetTemplate worksheetTemplate,
                             final WorksheetDataHandler dataHandler,
                             final CacheManager cacheManager,
                             final TableOutlineCreator outlineCreator,
                             final WorksheetConsumerStageHandler stageHandler)
    {
        this.sheet = sheet;
        this.worksheetTemplate = worksheetTemplate;
        this.dataHandler = dataHandler;
        this.outlineCreator = outlineCreator;
        this.cacheManager = cacheManager;
        this.stageHandler = stageHandler;
    }

    public TemplateValidationResult validateTemplate()
    {
        final List<TemplateValidationMessage> messages = new ArrayList<TemplateValidationMessage>();
        int errorCount = 0;

        for(final TemplateValidationRule templateRule : worksheetTemplate.getTemplateValidationRules())
        {
            boolean valid = templateRule.isValid(worksheetTemplate, sheet, messages);
            if(! valid)
            {
                errorCount++;
                if(templateRule.isFailureFatal())
                    break;
            }
        }

        final List<TemplateValidationMessage> errors = new ArrayList<TemplateValidationMessage>();
        final List<TemplateValidationMessage> warnings = new ArrayList<TemplateValidationMessage>();
        for(final TemplateValidationMessage m : messages)
            if(worksheetTemplate.isWarning(m))
                warnings.add(m);
            else
                errors.add(m);

        return new TemplateValidationResult()
        {
            public boolean isValid()
            {
                return errors.size() == 0;
            }

            public boolean hasWarnings()
            {
                return warnings.size() > 0;
            }

            public TemplateValidationMessage[] getErrors()
            {
                return errors.toArray(new TemplateValidationMessage[errors.size()]);
            }

            public TemplateValidationMessage[] getWarnings()
            {
                return warnings.toArray(new TemplateValidationMessage[warnings.size()]);
            }
        };
    }

    public DataValidationResult validateData()
    {
        final List<TableRow> tableRows = new ArrayList<TableRow>();

        for(final Row dataRow : sheet)
        {
            final WorksheetDataHandler.Result result = dataHandler.process(worksheetTemplate, sheet, dataRow);
            if(result.handle())
                tableRows.add(result.createTableRow());

            if(result.isLast())
                break;
        }

        final ValidationContext vc = worksheetTemplate.createValidationContext();
        final List<RowValidationMessage> rowMessages = new ArrayList<RowValidationMessage>();

        final Table table = dataHandler.createTable(worksheetTemplate, sheet, tableRows);
        for(final TableRow tr : table.getRows())
        {
            for(final RowValidationRule rule : worksheetTemplate.getRowValidationRules())
            {
                // validate and store messages in errors and warnings lists
                rule.isValid(vc, table, tr, rowMessages);
            }
        }

        final List<RowValidationMessage> rowErrors = new ArrayList<RowValidationMessage>();
        final List<RowValidationMessage> rowWarnings = new ArrayList<RowValidationMessage>();
        for(final RowValidationMessage m : rowMessages)
            if(worksheetTemplate.isWarning(m))
                rowWarnings.add(m);
            else
                rowErrors.add(m);

        if(cacheManager != null)
            cacheManager.cache(table);

        return new DataValidationResult()
        {
            public boolean isValid()
            {
                return rowErrors.size() == 0;
            }

            public boolean hasWarnings()
            {
                return rowWarnings.size() > 0;
            }

            public Table getTable()
            {
                return table;
            }

            public RowValidationMessage[] getErrors()
            {
                return rowErrors.toArray(new RowValidationMessage[rowErrors.size()]);
            }

            public RowValidationMessage[] getWarnings()
            {
                return rowWarnings.toArray(new RowValidationMessage[rowWarnings.size()]);
            }
        };
    }

    public OutlineStructureValidationResult validateOutlineStructure(final Table table)
    {
        final List<OutlineValidationMessage> structuralMessages = new ArrayList<OutlineValidationMessage>();
        final TableOutline outline = outlineCreator.createOutline(table, structuralMessages);

        final List<OutlineValidationMessage> structuralErrors = new ArrayList<OutlineValidationMessage>();
        final List<OutlineValidationMessage> structuralWarnings = new ArrayList<OutlineValidationMessage>();
        for(final OutlineValidationMessage m : structuralMessages)
            if(worksheetTemplate.isWarning(m))
                structuralWarnings.add(m);
            else
                structuralErrors.add(m);

        return new OutlineStructureValidationResult()
        {
            public TableOutline getTableOutline()
            {
                return outline;
            }

            public boolean isValid()
            {
                return structuralErrors.size() == 0;
            }

            public boolean hasWarnings()
            {
                return structuralWarnings.size() > 0;
            }

            public OutlineValidationMessage[] getErrors()
            {
                return structuralErrors.toArray(new OutlineValidationMessage[structuralErrors.size()]);
            }

            public OutlineValidationMessage[] getWarnings()
            {
                return structuralWarnings.toArray(new OutlineValidationMessage[structuralWarnings.size()]);
            }
        };
    }

    public OutlineDataValidationResult validateOutlineData(final TableOutline outline)
    {
        final ValidationContext vc = worksheetTemplate.createValidationContext();
        final List<NodeValidationMessage> nodeMessages = new ArrayList<NodeValidationMessage>();

        outline.isValid(vc, nodeMessages);

        final List<NodeValidationMessage> nodeErrors = new ArrayList<NodeValidationMessage>();
        final List<NodeValidationMessage> nodeWarnings = new ArrayList<NodeValidationMessage>();
        for(final NodeValidationMessage m : nodeMessages)
            if(worksheetTemplate.isWarning(m))
                nodeWarnings.add(m);
            else
                nodeErrors.add(m);

        return new OutlineDataValidationResult()
        {
            public TableOutline getTableOutline()
            {
                return outline;
            }

            public boolean isValid()
            {
                return nodeErrors.size() == 0;
            }

            public boolean hasWarnings()
            {
                return nodeWarnings.size() > 0;
            }

            public NodeValidationMessage[] getErrors()
            {
                return nodeErrors.toArray(new NodeValidationMessage[nodeErrors.size()]);
            }

            public NodeValidationMessage[] getWarnings()
            {
                return nodeWarnings.toArray(new NodeValidationMessage[nodeWarnings.size()]);
            }
        };
    }

    public void consume()
    {
        startConsumption();

        startStage(WorksheetConsumerStageHandler.Stage.VALIDATING_TEMPLATE);
        final TemplateValidationResult tvr = validateTemplate();
        if(tvr.isValid())
        {
            completeStage(getStage(), tvr.getWarnings());

            startStage(WorksheetConsumerStageHandler.Stage.VALIDATING_DATA);
            final DataValidationResult dvr = validateData();
            if(dvr.isValid())
            {
                completeStage(WorksheetConsumerStageHandler.Stage.VALIDATING_DATA, dvr.getWarnings());

                if(outlineCreator != null)
                {
                    startStage(WorksheetConsumerStageHandler.Stage.VALIDATING_OUTINE_STRUCT);
                    final OutlineStructureValidationResult osvr = validateOutlineStructure(dvr.getTable());
                    if(osvr.isValid())
                    {
                        completeStage(WorksheetConsumerStageHandler.Stage.VALIDATING_OUTINE_STRUCT, osvr.getWarnings());

                        startStage(WorksheetConsumerStageHandler.Stage.VALIDATING_OUTLINE_DATA);
                        final OutlineDataValidationResult odvr = validateOutlineData(osvr.getTableOutline());
                        if(odvr.isValid())
                        {
                            completeStage(WorksheetConsumerStageHandler.Stage.VALIDATING_OUTLINE_DATA, odvr.getWarnings());
                            endConsumption(true, dvr.getTable(), odvr.getTableOutline());
                        }
                        else
                        {
                            completeStage(WorksheetConsumerStageHandler.Stage.VALIDATING_OUTLINE_DATA, odvr.getErrors(), odvr.getWarnings());
                            endConsumption(false, dvr.getTable(), odvr.getTableOutline());
                        }
                    }
                    else
                    {
                        completeStage(WorksheetConsumerStageHandler.Stage.VALIDATING_OUTLINE_DATA, osvr.getErrors(), osvr.getWarnings());
                        endConsumption(false, dvr.getTable());
                    }
                }
                else
                    endConsumption(true, dvr.getTable());
            }
            else
            {
                completeStage(WorksheetConsumerStageHandler.Stage.VALIDATING_DATA, dvr.getErrors(), dvr.getWarnings());
                endConsumption(false, dvr.getTable());
            }
        }
        else
        {
            completeStage(WorksheetConsumerStageHandler.Stage.VALIDATING_TEMPLATE, tvr.getErrors(), tvr.getWarnings());
            endConsumption(false, null);
        }

    }

    public WorksheetConsumerStageHandler.Stage getStage()
    {
        return stage;
    }

    protected void startConsumption()
    {
        stageHandler.startConsumption();
    }

    protected void startStage(final WorksheetConsumerStageHandler.Stage stage)
    {
        this.stage = stage;
        stageHandler.startStage(stage);
    }

    protected void completeStage(final WorksheetConsumerStageHandler.Stage stage, final Message[] warnings)
    {
        stageHandler.completeStage(stage, warnings);
    }

    protected void completeStage(final WorksheetConsumerStageHandler.Stage stage, final Message[] errors, final Message[] warnings)
    {
        stageHandler.completeStage(stage, errors, warnings);
    }

    protected void endConsumption(boolean successful, final Table table, final TableOutline outline)
    {
        if(successful)
            this.stage = WorksheetConsumerStageHandler.Stage.FINAL;
        stageHandler.endConsumption(successful, table, outline);
    }

    protected void endConsumption(boolean successful, final Table table)
    {
        endConsumption(successful, table, null);
    }
}
