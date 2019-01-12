package de.hshannover.inform.gnuman.app.rules;

import de.hshannover.inform.gnuman.app.enums.Difficulty;
import de.hshannover.inform.gnuman.app.enums.GhostMovementStates;

/**
 * Set pattern of modes and their length in ms. -1 means for infinity (till the level completes).
 * @author Marc Herschel
 */

public class GhostStateRules {

    private final static GhostMovementStates[] STATE_CYCLE = new GhostMovementStates[] {
            GhostMovementStates.SCATTER,
            GhostMovementStates.CHASE,
            GhostMovementStates.SCATTER,
            GhostMovementStates.CHASE,
            GhostMovementStates.SCATTER,
            GhostMovementStates.CHASE,
            GhostMovementStates.SCATTER,
            GhostMovementStates.CHASE,
    };

    private final static long[] TIME_LEVEL_ONE = new long[] {
            7000,
            20000,
            7000,
            2000,
            5000,
            20000,
            5000,
            -1
    };

    private final static long[] TIME_LEVEL_TWO = new long[] {
            7000,
            20000,
            7000,
            2000,
            5000,
            1033000,
            17,
            -1
    };

    private final static long[] TIME_LEVEL_FIVE = new long[] {
            5000,
            20000,
            5000,
            2000,
            5000,
            1037000,
            17,
            -1
    };

    /**
     * Manage the modes.
     * @author Marc Herschel
     */
    public class GhostStateBehavior {
        private int index;
        private double difficultyScale;
        private long[] lengthOfModesInMs;

        public GhostStateBehavior(long[] lengthOfModes, Difficulty diff) {
            this.lengthOfModesInMs = lengthOfModes;
            this.difficultyScale = diff.getTimingScale();
        }

        public void nextState() {
            if(maximumStateReached()) { return; }
            index++;
        }

        public GhostMovementStates currentState() {
            return STATE_CYCLE[index];
        }

        public long stateLengthInMs() {
            return (long) (lengthOfModesInMs[index] * difficultyScale);
        }

        public boolean maximumStateReached() {
            return index + 1 >= STATE_CYCLE.length;
        }
    }

    /**
     * Create a behavior for the current level.
     * @param level to generate for.
     * @param diff difficulty to use when creating the behavior
     * @return object to handle the ghost states.
     */
    public GhostStateBehavior createBehaviorForLevel(int level, Difficulty diff) {
        if(level  == 1) { return new GhostStateBehavior(TIME_LEVEL_ONE, diff); }
        if(level >= 2 && level <= 4) { return new GhostStateBehavior(TIME_LEVEL_TWO, diff); }
        return new GhostStateBehavior(TIME_LEVEL_FIVE, diff);
   }
}
