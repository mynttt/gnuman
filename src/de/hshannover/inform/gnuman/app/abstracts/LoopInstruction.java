package de.hshannover.inform.gnuman.app.abstracts;

/**
 * Instructions for the GameLoop.
 * @author Marc Herschel
 */

public abstract class LoopInstruction {

    /**
     * What to do on each tick.
     * @param now Now parameter of the AnimationTimer that the GameLoop extends.
     */
    public abstract void handle(long now);
}
