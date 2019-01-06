package de.hshannover.inform.gnuman.app.interfaces;

/**
 * Persistent tasks will always get executed.
 * @author Marc Herschel
 */

@FunctionalInterface
public interface PersistentGameTask {

    /**
     * Executes a task that has been defined through this interface.
     */
    void execute();
}
