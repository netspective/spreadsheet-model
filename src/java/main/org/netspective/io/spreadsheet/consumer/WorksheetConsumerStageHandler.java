package org.netspective.io.spreadsheet.consumer;

import org.netspective.io.spreadsheet.message.Message;
import org.netspective.io.spreadsheet.model.Table;
import org.netspective.io.spreadsheet.outline.TableOutline;

public interface WorksheetConsumerStageHandler
{
    public enum Stage
    {
        INITIAL("Validation not started yet"),
        VALIDATING_TEMPLATE("Validating sheet to see if it matches the template"),
        VALIDATING_DATA("Validating common data items across all rows"),
        VALIDATING_OUTINE_STRUCT("Validating structure of sheet rows (to create an outline)"),
        VALIDATING_OUTLINE_DATA("Validating data in sheet's outline structure"),
        FINAL("Validation process complete");

        private final String description;

        Stage(final String description)
        {
            this.description = description;
        }

        public String description()
        {
            return description;
        }

        public Stage[] remaining(boolean creatingOutline)
        {
            switch(this)
            {
                case INITIAL:
                    return creatingOutline ? new Stage[] { VALIDATING_TEMPLATE, VALIDATING_DATA, VALIDATING_OUTINE_STRUCT, VALIDATING_OUTLINE_DATA } : new Stage[] { VALIDATING_TEMPLATE, VALIDATING_DATA };

                case VALIDATING_TEMPLATE:
                    return creatingOutline ? new Stage[] { VALIDATING_DATA, VALIDATING_OUTINE_STRUCT, VALIDATING_OUTLINE_DATA } : new Stage[] { VALIDATING_DATA };

                case VALIDATING_DATA:
                    return creatingOutline ? new Stage[] { VALIDATING_OUTINE_STRUCT, VALIDATING_OUTLINE_DATA } : new Stage[0];

                case VALIDATING_OUTINE_STRUCT:
                    return creatingOutline ? new Stage[] { VALIDATING_OUTLINE_DATA } : new Stage[0];

                case VALIDATING_OUTLINE_DATA:
                    return new Stage[0];

                case FINAL:
                    return new Stage[0];
            }

            throw new RuntimeException(String.format("Unable to figure out what stages are remaining from current stage %s and outlining %s.", this, creatingOutline));
        }
    }

    public void startConsumption();
    public void startStage(final Stage stage);
    public void completeStage(final Stage stage, final Message[] warnings);
    public void completeStage(final Stage stage, final Message[] errors, final Message[] warnings);
    public void endConsumption(boolean successful, final Table table, final TableOutline outline);
}
