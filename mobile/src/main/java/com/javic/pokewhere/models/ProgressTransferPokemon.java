package com.javic.pokewhere.models;

/**
 * Created by vagprogrammer on 08/10/16.
 */

public class ProgressTransferPokemon {

    private String progressMessage;
    private Boolean updateProgress;

    public ProgressTransferPokemon(String progressMessage, Boolean updateProgress) {
        this.progressMessage = progressMessage;
        this.updateProgress = updateProgress;
    }

    public ProgressTransferPokemon() {

    }

    public String getProgressMessage() {
        return progressMessage;
    }

    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }

    public Boolean getUpdateProgress() {
        return updateProgress;
    }

    public void setUpdateProgress(Boolean updateProgress) {
        this.updateProgress = updateProgress;
    }


}
