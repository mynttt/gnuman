package de.hshannover.inform.gnuman.app.rules;

/**
 * Rules for Blinkys elroy mode.
 * @author Marc Herschel
 */
public class ElroyRules {

    /**
     * @param level current level
     * @return speed modifier for stage one
     */
    public static double modifierElroyStageOne(int level) {
        if(level == 1) { return 0.8; }
        if(level >= 2 && level <= 4) { return 0.9; }
        return 1.0;
    }

    /**
     * @param level current level
     * @return speed modifier for stage two
     */
    public static double modifierElroyStageTwo(int level) {
        if(level == 1) { return 0.9; }
        if(level >= 2 && level <= 4) { return 0.95; }
        return 1.05;
    }

    /**
     * @param level current level
     * @return dot modifier for stage one
     */
    public static double triggerStageOneDotModifier(int level) {
        if(level == 1) return 0.08;
        if(level == 2) return 0.125;
        if(level >= 3 && level <= 5) return 0.167;
        if(level >= 6 && level <= 8) return 0.208;
        if(level >= 9 && level <= 11) return 0.25;
        if(level >= 12 && level <= 14) return 0.33;
        if(level >= 15 && level <= 18) return 0.41;
        return 0.5;
    }

    /**
     * @param level current level
     * @return dot modifier for stage two
     */
    public static double triggerStageTwoDotModifier(int level) {
        if(level == 1) return 0.04;
        if(level == 2) return 0.063;
        if(level >= 3 && level <= 5) return 0.08;
        if(level >= 6 && level <= 8) return 0.104;
        if(level >= 9 && level <= 11) return 0.125;
        if(level >= 12 && level <= 14) return 0.167;
        if(level >= 15 && level <= 18) return 0.208;
        return 0.25;
    }
}
