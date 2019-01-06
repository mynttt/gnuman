package de.hshannover.inform.gnuman.app.model.animation;

/**
 * Toggles an animation (on/off) in a certain interval.
 * @author Marc Herschel
 */

public class AnimationToggle {
    private int animationOnTill, animationOffTill, animationTick;

    /**
     * Construct an AnimationToggle with a consecutive cycle.
     * @param cycle of frames
     */
    public AnimationToggle(int cycle)  {
        animationOnTill = cycle;
        animationOffTill = cycle*2;
    }

    /**
     * Construct an AnimationToggle with a custom defined cycle.
     */
    public AnimationToggle(int onTill, int offTill) {
        animationOnTill = onTill;
        animationOffTill = offTill;
    }

    /**
     * Update the ticks and evaluates if the state is on display.
     * @return true if in display cycle.
     */
    public boolean updateAndIsDisplay() {
        if(++animationTick >= animationOffTill) { animationTick = 0; }
        return animationTick <= animationOnTill;
    }

    /**
     * Update with no evaluation.
     */
    public void update() {
        if(++animationTick >= animationOffTill) { animationTick = 0; }
    }

    /**
     * @return true if currently displaying
     */
    public boolean isDisplay() { return animationTick <= animationOnTill; }

    /**
     * @return true if display is currently inverted.
     */
    public boolean isInvertedDisplay() { return animationTick >= animationOffTill; }

    /**
     * Resets the state to initial.
     */
    public void reset() { animationTick = 0; }
}
