package de.hshannover.inform.gnuman.app.model.ghosts;

import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.app.enums.GhostBehaviorState;
import de.hshannover.inform.gnuman.app.enums.GhostMovementStates;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.model.AbstractGhost;
import de.hshannover.inform.gnuman.app.model.Player;
import de.hshannover.inform.gnuman.app.model.coordination.GhostMovementCoordinator;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.GameVariableTracker;
import de.hshannover.inform.gnuman.app.model.storage.MapCell;
import de.hshannover.inform.gnuman.app.rules.ElroyRules;
import de.hshannover.inform.gnuman.app.rules.EntitySpeedRules;
import de.hshannover.inform.gnuman.app.rules.EntitySpeedRules.SpeedTypes;

/**
 * Responds to Blinkys behavior.<br>
 * Blinky will always try to chase the player directly.<br>
 * There is also an elroy mode that will even or even raise his speed above the players one that will be triggered<br>
 * once a certain percentage of points have been eaten.
 * @author Marc Herschel
 */

public class Blinky extends AbstractGhost {
    private ElroyStates elroy;
    private boolean clydeHasLeft;

    /**
     * States for Blinkys elroy mode.
     */
    enum ElroyStates {
        NONE, ONE, TWO
    }

    public Blinky(DynamicVariables dyn, GhostMovementCoordinator coordinator, GameVariableTracker tracker) {
        super(EntityObjects.BLINKY, dyn, coordinator, tracker);
        coordinator.setBlinky(this);
        elroy = ElroyStates.NONE;

        //Task for elroy mode
        persistentTasks.add(() -> {
            if(!tracker.isElroyAfterLifeLost() || (tracker.isElroyAfterLifeLost() && clydeHasLeft)) {
                checkElroy();
            }
        });
    }

    @Override
    protected MapCell decideChaseBehavior(Player player) {
        return new MapCell(player.clampCellX(), player.clampCellY());
    }

    @Override
    protected void computeSpeed(SpeedTypes speedFactor) {
        if(!isSlow && getBehaviorState() == GhostBehaviorState.NORMAL && (getMovementState() == GhostMovementStates.SCATTER || getMovementState() == GhostMovementStates.CHASE)) {
            if(elroy != ElroyStates.NONE) { tracker.enableElroy(); }
            if(elroy == ElroyStates.ONE) {
                setSpeed(dyn.getBaseSpeed() * ElroyRules.modifierElroyStageOne(tracker.getLevel()));
                return;
            }
            if(elroy == ElroyStates.TWO) {
                setSpeed(dyn.getBaseSpeed() * ElroyRules.modifierElroyStageTwo(tracker.getLevel()));
                return;
            }
        }
        setSpeed(dyn.getBaseSpeed() * EntitySpeedRules.getSpeedMultiplier(speedFactor, tracker.getLevel()));
    }

    @Override
    public void reset() {
        super.reset();
        elroy = ElroyStates.NONE;
        clydeHasLeft = false;
    }

    /**
     * Check conditions for elroy mode.
     */
    private void checkElroy() {
        if(getMovementState() == GhostMovementStates.SCATTER || getMovementState() == GhostMovementStates.CHASE && getBehaviorState() == GhostBehaviorState.NORMAL) {
            if(elroy == ElroyStates.NONE && tracker.getAllPacDots()-tracker.getEatenPacDots() < tracker.getAllPacDots() * ElroyRules.triggerStageOneDotModifier(tracker.getLevel())) {
                elroy = ElroyStates.ONE;
                computeSpeed(SpeedTypes.GHOST_NORMAL);
            }
            if(elroy == ElroyStates.ONE && tracker.getAllPacDots()-tracker.getEatenPacDots() < tracker.getAllPacDots() * ElroyRules.triggerStageTwoDotModifier(tracker.getLevel())) {
                elroy = ElroyStates.TWO;
                computeSpeed(SpeedTypes.GHOST_NORMAL);
                GameLauncher.am().decideMusic(true, tracker.isFrightened());
            }
        }
    }

    /**
     * Notify blinky that clyde has left the base
     */
    public void clydeHasLeft() {
        clydeHasLeft = true;
    }
}
