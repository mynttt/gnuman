package de.hshannover.inform.gnuman.app.rules;

/**
 * Rules that do not fit anywhere else and that may or may not impact the game quite a lot.
 * @author Marc Herschel
 */

public class GeneralRules {

    /*
     * Will scale the timers with the speed multiplier to even out powerup abuse with faster speed.
     *
     * Default: True
     */
    public final static boolean SCALE_RULE_TIMERS_WITH_SPEED = true;

    /* Parameter < 0: Infinite number of extra lives.
     * Parameter = 0: Disabled
     * Parameter > 0: n-Amount of extra lives.
     *
     * Default: 1
     * */
    public static final int POSSIBLE_EXTRALIVES = 1;

    /*
     * Must be > 0.
     *
     * Default: 10000
     */
    public static final int EXTRA_LIVE_EVERY = 10000;
}
