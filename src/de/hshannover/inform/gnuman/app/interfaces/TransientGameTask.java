package de.hshannover.inform.gnuman.app.interfaces;

/**
 * Temporary tasks that expire once they return true.
 * @author Marc Herschel
 */

@FunctionalInterface
public interface TransientGameTask {

    /**
     * Execute a task and evaluate if the task finished.
     * @return true if the task finished, false if the task needs further execution.
     */
    boolean isFinished();
}
