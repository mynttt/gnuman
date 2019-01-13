package de.hshannover.inform.gnuman.app.model.ghosts;

import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.model.AbstractGhost;
import de.hshannover.inform.gnuman.app.model.Player;
import de.hshannover.inform.gnuman.app.model.coordination.GhostMovementCoordinator;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.GameVariableTracker;
import de.hshannover.inform.gnuman.app.model.storage.MapCell;

/**
 * Responds to Pinkys behavior.<br>
 * Pinky will always try to be four blocks in front of the player.<br>
 * The top-left bug from the original pacman is taken into consideration.
 * @author Marc Herschel
 */

public class Pinky extends AbstractGhost {

    public Pinky(DynamicVariables dyn, GhostMovementCoordinator coordinator, GameVariableTracker tracker) {
        super(EntityObjects.PINKY, dyn, coordinator, tracker);
    }

    @Override
    protected MapCell decideChaseBehavior(Player player) {
        Directions d = player.getDirection();
        int xPlayerOffset, yPlayerOffset;
    //X Calculation will take the original games offset bug into consideration!
        xPlayerOffset = (d == Directions.LEFT || d == Directions.RIGHT || d == Directions.UP) ? (d == Directions.RIGHT && d != Directions.UP ? 4 : -4) : 0;
        yPlayerOffset = (d == Directions.DOWN || d == Directions.UP) ? (d == Directions.DOWN ? 4 : -4) : 0;
        return new MapCell(player.clampCellX() + xPlayerOffset, player.clampCellY() + yPlayerOffset);
    }

}
