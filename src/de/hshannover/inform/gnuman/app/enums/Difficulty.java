package de.hshannover.inform.gnuman.app.enums;

import de.hshannover.inform.gnuman.app.rules.GeneralRules;

/**
 * Representing the games difficulty levels, high score uses this to build itself.
 * @author Marc Herschel
 */

public enum Difficulty {
    SLOW(0.7),
    NORMAL(1),
    FAST(2.25);

    private double speedScale;
    Difficulty(double d) { this.speedScale = d; }

    /**
     * @return speed scale for given difficulty.
     */
    public double getSpeedScale() { return speedScale; }

    /**
     * @return timing scale for given difficulty.
     */
    public double getTimingScale() {
        return GeneralRules.SCALE_RULE_TIMERS_WITH_SPEED ? 1 / speedScale : 1;
    }
}
