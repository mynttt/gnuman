package de.hshannover.inform.gnuman.app.rules;

import java.util.HashMap;
import java.util.Map;

/**
 * Speed percentage rules for different levels.
 * @author Marc Herschel
 */

@SuppressWarnings("serial")
public class EntitySpeedRules {

    public enum SpeedTypes {
        PACMAN_NORMAL,
        PACMAN_FRIGHT,
        GHOST_NORMAL,
        GHOST_FRIGHT,
        GHOST_TUNNEL
    }

    private static final Map<SpeedTypes, Double> LEVEL_1 = new HashMap<SpeedTypes, Double>() {{
        put(SpeedTypes.PACMAN_NORMAL,       .8);
        put(SpeedTypes.PACMAN_FRIGHT ,      .9);
        put(SpeedTypes.GHOST_NORMAL ,       .75);
        put(SpeedTypes.GHOST_FRIGHT ,       .5);
        put(SpeedTypes.GHOST_TUNNEL ,       .4);
    }};

    private static final Map<SpeedTypes, Double> LEVEL_2 = new HashMap<SpeedTypes, Double>() {{
        put(SpeedTypes.PACMAN_NORMAL,       .9);
        put(SpeedTypes.PACMAN_FRIGHT ,      .95);
        put(SpeedTypes.GHOST_NORMAL ,       .85);
        put(SpeedTypes.GHOST_FRIGHT ,       .55);
        put(SpeedTypes.GHOST_TUNNEL ,       .45);
    }};

    private static final Map<SpeedTypes, Double> LEVEL_5 = new HashMap<SpeedTypes, Double>() {{
        put(SpeedTypes.PACMAN_NORMAL,       1.0);
        put(SpeedTypes.PACMAN_FRIGHT ,      1.0);
        put(SpeedTypes.GHOST_NORMAL ,       .95);
        put(SpeedTypes.GHOST_FRIGHT ,       .6);
        put(SpeedTypes.GHOST_TUNNEL ,       .5);
    }};

    private static final Map<SpeedTypes, Double> LEVEL_20 = new HashMap<SpeedTypes, Double>() {{
        put(SpeedTypes.PACMAN_NORMAL,       .9);
        put(SpeedTypes.PACMAN_FRIGHT ,      1.0);
        put(SpeedTypes.GHOST_NORMAL ,       .95);
        put(SpeedTypes.GHOST_FRIGHT ,       1.0);
        put(SpeedTypes.GHOST_TUNNEL ,       .5);
    }};

    /**
     * @param type of speed
     * @param level current level
     * @return speed modifier
     */
    public static double getSpeedMultiplier(SpeedTypes type, int level) {
        if(level == 1) { return LEVEL_1.get(type); }
        if(level >= 2 && level <= 4) { return LEVEL_2.get(type); }
        if(level >= 5 && level <= 20) { return LEVEL_5.get(type); }
        return LEVEL_20.get(type);
    }
}
