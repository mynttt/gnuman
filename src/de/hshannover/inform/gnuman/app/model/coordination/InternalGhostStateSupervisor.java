package de.hshannover.inform.gnuman.app.model.coordination;

import de.hshannover.inform.gnuman.app.enums.Difficulty;
import de.hshannover.inform.gnuman.app.enums.GhostMovementStates;
import de.hshannover.inform.gnuman.app.interfaces.PersistentGameTask;
import de.hshannover.inform.gnuman.app.model.AbstractGhost;
import de.hshannover.inform.gnuman.app.rules.GhostStateRules;
import de.hshannover.inform.gnuman.app.rules.GhostStateRules.GhostStateBehavior;

/**
 * Keeps track of every ghosts internal timed state regarding the original pacman rules.
 * @author Marc Herschel
 */

public class InternalGhostStateSupervisor {
    private TimedTasks timed;
    private PersistentGameTask task;
    private GhostStateRules rules;
    private GhostStateBehavior behavior;
    private AbstractGhost ghost;
    private boolean maximumTaskReached, started, paused;
    private long pausedAt;

    /**
     * Construct an supervisor for the ghost state.
     * @param observe for callbacks once the state changed.
     */
    public InternalGhostStateSupervisor(AbstractGhost observe) {
        rules = new GhostStateRules();
        timed = new TimedTasks();
        ghost = observe;
    }

    /**
     * Load settings for level
     * @param level to load for
     * @param diff difficulty to load for
     */
    public void loadLevel(int level, Difficulty diff) {
        timed.clearTasks();
        behavior = rules.createBehaviorForLevel(level, diff);
    }

    /**
     * Start the state supervisor.
     */
    public void start() {
        timed.createTask("gamestate", behavior.stateLengthInMs());
    //Shift state if maximum state has not reached yet.
        task = () -> {
            if(timed.isFinished("gamestate")) {
                maximumTaskReached = behavior.maximumStateReached();
                if(!maximumTaskReached) {
                    behavior.nextState();
                    timed.createOrOverrideTask("gamestate", behavior.stateLengthInMs());
                }
                ghost.globalStateHasChanged();
            }
        };
        started = true;
    }

    /**
     * Update per tick
     */
    public void update() {
        if(started && !paused && !maximumTaskReached ) {
            task.execute();
        }
    }

    /**
     * Adjust time if paused
     * @param toAdjust time delta to adjust to-
     */
    public void adjustDeltaTime(long toAdjust) {
        timed.adjustDeltaTimeForAllTasks(toAdjust);
    }

    /**
     * @return current global tracked state for ghost.
     */
    public GhostMovementStates getCurrentGlobalState() {
        return maximumTaskReached ? GhostMovementStates.CHASE : behavior.currentState();
    }

    /**
     * Reset supervisor
     */
    public void reset() {
        task = null;
        started = paused = false;
    }

    /**
     * Pause supervisor
     */
    public void pause() {
        paused = true;
        pausedAt = System.currentTimeMillis();
    }

    /**
     * Resume after paused
     */
    public void resume() {
        if(paused) { timed.adjustDeltaTimeForAllTasks(System.currentTimeMillis() - pausedAt); paused = false; }
    }

    /**
     * @return true if paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * @return true if started
     */
    public boolean isStarted() {
        return started;
    }
}
