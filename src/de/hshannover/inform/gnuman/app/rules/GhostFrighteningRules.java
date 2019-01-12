package de.hshannover.inform.gnuman.app.rules;

import java.util.HashMap;

import de.hshannover.inform.gnuman.app.enums.Difficulty;

/**
 * Frightening times for ghosts per level.
 * @author Marc Herschel
 */

@SuppressWarnings("serial")
public class GhostFrighteningRules {

    private final static HashMap<Integer, Integer> FRIGHTENED_TIME = new HashMap<Integer, Integer>() {{
        put(1, 6000);
        put(2, 5000);
        put(3, 4000);
        put(4, 3000);
        put(5, 2000);
        put(6, 5000);
        put(7, 2000);
        put(8, 2000);
        put(9, 1000);
        put(10, 5000);
        put(11, 2000);
        put(12, 1000);
        put(13, 1000);
        put(14, 3000);
        put(15, 1000);
        put(16, 1000);
        put(17, 0);
        put(18, 1000);
    }};

    /**
     * @param level current level
     * @param difficulty difficulty to use for frightening time
     * @return time in ms
     */
    public static int getFrighteningTimeInMs(int level, Difficulty difficulty) {
        if(level <= 18) { return (int) (difficulty.getTimingScale() * FRIGHTENED_TIME.get(level)); }
        return 0;
    }
}
