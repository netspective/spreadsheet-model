package gov.omb.io.a11.exhibit.ex53;

import org.netspective.io.spreadsheet.consumer.DefaultWorksheetDataHandler;
import org.netspective.io.spreadsheet.consumer.WorksheetConsumer;
import org.netspective.io.spreadsheet.consumer.WorksheetDataHandler;
import org.netspective.io.spreadsheet.message.Message;

public class Exhibit53WorksheetConsumer
{
    public enum ValidationStage
    {
        INITIAL("Validation not started yet"),
        VALIDATING_TEMPLATE("Validating sheet to see if it matches the template"),
        VALIDATING_DATA("Validating common data items across all rows"),
        VALIDATING_OUTINE_STRUCT("Validating structure of portfolio, parts, mission areas, investments, and investment lines"),
        VALIDATING_OUTLINE_DATA("Validating data in portfolio, parts, mission areas, investments, and investment lines"),
        FINAL("Validation process complete");

        private final String description;

        ValidationStage(final String description)
        {
            this.description = description;
        }

        public String description()
        {
            return description;
        }
    }

    public interface ValidationStageHandler
    {
        public void startStage(final ValidationStage stage);
        public void completeStage(final ValidationStage stage, final Message[] warnings);
        public void completeStage(final ValidationStage stage, final Message[] errors, final Message[] warnings);
        public void completeFinalStage(final Exhibit53Parameters parameters, final Exhibit53WorksheetTemplate.Exhibit53 exhibit53);
    }

    private final Exhibit53Parameters parameters;
    private final ValidationStageHandler[] stageHandlers;
    private final ValidationStageHandler stageHandler = new ValidationStageHandlerWrapper();
    private ValidationStage stage = ValidationStage.INITIAL;

    public class ValidationStageHandlerWrapper implements ValidationStageHandler
    {
        public void startStage(final ValidationStage stage)
        {
            for(final ValidationStageHandler h : stageHandlers) h.startStage(stage);
        }

        public void completeStage(final ValidationStage stage, final Message[] warnings)
        {
            for(final ValidationStageHandler h : stageHandlers) h.completeStage(stage, warnings);
        }

        public void completeStage(final ValidationStage stage, final Message[] errors, final Message[] warnings)
        {
            for(final ValidationStageHandler h : stageHandlers) h.completeStage(stage, errors, warnings);
        }

        public void completeFinalStage(final Exhibit53Parameters parameters, final Exhibit53WorksheetTemplate.Exhibit53 exhibit53)
        {
            for(final ValidationStageHandler h : stageHandlers) h.completeFinalStage(parameters, exhibit53);
        }
    }

    public Exhibit53WorksheetConsumer(final Exhibit53Parameters parameters, final ValidationStageHandler[] handlers)
    {
        this.parameters = parameters;
        this.stageHandlers = handlers;
    }

    public void consume()
    {
        final Exhibit53WorksheetTemplate template = new Exhibit53WorksheetTemplate(parameters);
        final WorksheetDataHandler exhibit53DataHandler = new DefaultWorksheetDataHandler(9, 2, 17, new int[] { 2, 3 });

        setStage(ValidationStage.VALIDATING_TEMPLATE);
        final WorksheetConsumer consumer = new WorksheetConsumer(template, exhibit53DataHandler, template, template, parameters.getSheet());
        final WorksheetConsumer.TemplateValidationResult tvr = consumer.validateTemplate();
        if(tvr.isValid())
            stageHandler.completeStage(getStage(), tvr.getWarnings());
        else
        {
            stageHandler.completeStage(getStage(), tvr.getErrors(), tvr.getWarnings());
            return;
        }

        setStage(ValidationStage.VALIDATING_DATA);
        final WorksheetConsumer.DataValidationResult dvr = consumer.validateData();
        if(dvr.isValid())
            stageHandler.completeStage(getStage(), dvr.getWarnings());
        else
        {
            stageHandler.completeStage(getStage(), dvr.getErrors(), dvr.getWarnings());
            return;
        }

        setStage(ValidationStage.VALIDATING_OUTINE_STRUCT);
        final WorksheetConsumer.OutlineStructureValidationResult osvr = consumer.validateOutlineStructure(dvr.getTable());
        if(osvr.isValid())
            stageHandler.completeStage(getStage(), osvr.getWarnings());
        else
        {
            stageHandler.completeStage(getStage(), osvr.getErrors(), osvr.getWarnings());
            return;
        }

        setStage(ValidationStage.VALIDATING_OUTLINE_DATA);
        final WorksheetConsumer.OutlineDataValidationResult odvr = consumer.validateOutlineData(osvr.getTableOutline());
        if(odvr.isValid())
            stageHandler.completeStage(getStage(), odvr.getWarnings());
        else
            stageHandler.completeStage(getStage(), odvr.getErrors(), odvr.getWarnings());

        setStage(ValidationStage.FINAL);
        stageHandler.completeFinalStage(parameters, (Exhibit53WorksheetTemplate.Exhibit53) odvr.getTableOutline());
    }

    public ValidationStage getStage()
    {
        return stage;
    }

    protected void setStage(final ValidationStage stage)
    {
        this.stage = stage;
        if(stage != ValidationStage.FINAL)
            stageHandler.startStage(stage);
    }
}
