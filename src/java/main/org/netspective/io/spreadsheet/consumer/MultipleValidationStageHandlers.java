package org.netspective.io.spreadsheet.consumer;

import org.netspective.io.spreadsheet.message.Message;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.outline.TableOutline;

public class MultipleValidationStageHandlers implements WorksheetConsumerStageHandler
{
    private final WorksheetConsumerStageHandler[] handlers;

    public MultipleValidationStageHandlers(final WorksheetConsumerStageHandler ... handlers)
    {
        this.handlers = handlers;
    }

    public void startConsumption()
    {
        for(final WorksheetConsumerStageHandler h : handlers) h.startConsumption();
    }

    public void endConsumption(boolean successful, final Table table, final TableOutline outline)
    {
        for(final WorksheetConsumerStageHandler h : handlers) h.endConsumption(successful, table, outline);
    }

    public void startStage(final Stage stage)
    {
        for(final WorksheetConsumerStageHandler h : handlers) h.startStage(stage);
    }

    public void completeStage(final Stage stage, final Message[] warnings)
    {
        for(final WorksheetConsumerStageHandler h : handlers) h.completeStage(stage, warnings);
    }

    public void completeStage(final Stage stage, final Message[] errors, final Message[] warnings)
    {
        for(final WorksheetConsumerStageHandler h : handlers) h.completeStage(stage, errors, warnings);
    }
}
