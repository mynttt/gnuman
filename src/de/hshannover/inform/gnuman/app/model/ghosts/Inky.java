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
 * Responds to Inkys behavior. <br>
 * Inky will first target a tile that is 2 cells in front of the player (takes 4 tile bug from original pacman into consideration).<br>
 * Then he will extend that point with the difference between the point and Blinky.<br>
 * The resulting tile is the new target tile.
 * @author Marc Herschel
 */

public class Inky extends AbstractGhost {

    public Inky(DynamicVariables dyn, GhostMovementCoordinator coordinator, GameVariableTracker tracker) {
        super(EntityObjects.INKY, dyn, coordinator, tracker);
    }

    @Override
    protected MapCell decideChaseBehavior(Player player) {
        Directions d = player.getDirection();
        int initialOffsetX, initialOffsetY, blinkyToOffsetX, blinkyToOffsetY;
    //X Calculation will take the original games offset bug into consideration!
        initialOffsetX = player.clampCellX() + ((d == Directions.LEFT || d == Directions.RIGHT || d == Directions.UP) ? (d == Directions.RIGHT && d != Directions.UP ? 2 : -2) : 0);
        initialOffsetY = player.clampCellY() + ((d == Directions.DOWN || d == Directions.UP) ? (d == Directions.DOWN ? 2 : -2) : 0);
        blinkyToOffsetX = (initialOffsetX - coordinator.getBlinkyX())*2;
        blinkyToOffsetY = (initialOffsetY - coordinator.getBlinkyY())*2;
        return new MapCell(coordinator.getBlinkyX() + blinkyToOffsetX, coordinator.getBlinkyY()+blinkyToOffsetY);
    }

}
