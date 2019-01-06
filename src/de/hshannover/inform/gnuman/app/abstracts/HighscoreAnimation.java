package de.hshannover.inform.gnuman.app.abstracts;

/**
 * Abstract class for the animation of the highscore sequence.
 * @author Marc Herschel
 */
public abstract class HighscoreAnimation {
    protected boolean reset = true;
    protected double ghostX, playerX, gplX;

    public abstract void renderNextFrame();
    public void reset() { reset = true; }
}
