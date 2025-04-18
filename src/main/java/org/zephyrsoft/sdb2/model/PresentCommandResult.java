package org.zephyrsoft.sdb2.model;

public enum PresentCommandResult {
    SUCCESS(true, true),
    ONLY_SCOLLED(true, true),
    NOTHING_TO_DO(true, false),
    FAILURE(false, true);

    private final boolean successful;
    private final boolean stateChanged;

    PresentCommandResult(boolean successful, boolean stateChanged) {
        this.successful = successful;
        this.stateChanged = stateChanged;
    }

    public boolean wasSuccessful() {
        return successful;
    }

    public boolean wasStateChanged() {
        return stateChanged;
    }
}
