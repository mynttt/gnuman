package de.hshannover.inform.gnuman.app.modules;

import java.util.LinkedList;
import java.util.List;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.model.AbstractGhost;
import de.hshannover.inform.gnuman.app.model.Player;
import de.hshannover.inform.gnuman.app.model.coordination.GhostMovementCoordinator;
import de.hshannover.inform.gnuman.app.model.ghosts.Blinky;
import de.hshannover.inform.gnuman.app.model.ghosts.Clyde;
import de.hshannover.inform.gnuman.app.model.ghosts.Inky;
import de.hshannover.inform.gnuman.app.model.ghosts.Pinky;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.GameVariableTracker;
import de.hshannover.inform.gnuman.app.model.storage.MapData;

/**
 * Controls entity creation and destruction.
 * @author Marc Herschel
 */

public class Entities {
    private DynamicVariables dyn;
    private TileMap map;
    private Player player;
    private List<AbstractGhost> ghosts;

    /**
     * Construct a new entity manager.
     * @param map to place entity on.
     * @param dyn Dynamic user options depending values.
     */
    public Entities(TileMap map, DynamicVariables dyn) {
        this.dyn = dyn;
        this.map = map;
        this.ghosts = new LinkedList<>();
    }

    /**
     * Unloads all components so you can load a new map.
     */
    public void unloadEntities() {
        player = null;
        ghosts.clear();
    }

    /**
     * Create entities and set them all at (0,0).
     * @param mapData data needed for creation.
     * @param tracker game variable tracker
     */
    public void createEntities(MapData mapData, GameVariableTracker tracker) {
    //Entity creation
        player = new Player(dyn, map, mapData, tracker);
    //Ghosts
        GhostMovementCoordinator movementCoordinator = new GhostMovementCoordinator(dyn, map, mapData, player, tracker);
        ghosts.add(new Blinky(dyn, movementCoordinator, tracker));
        ghosts.add(new Clyde(dyn, movementCoordinator, tracker));
        ghosts.add(new Pinky(dyn, movementCoordinator, tracker));
        ghosts.add(new Inky(dyn, movementCoordinator, tracker));
   //OK
        Log.info(getClass().getSimpleName(), "Entites placed and loaded successfully.");
    }

    /**
     * Reset entities to their default spawns and their behavior.
     */
    public void resetEntities() {
        player.reset();
        for(AbstractGhost g : ghosts) {
            g.reset();
        }
    }

    /**
     * Send update signal to the player.
     */
    public void updatePlayer() {
        player.update();
    }

    /**
     * Send update signal to the ghosts.
     */
    public void updateGhosts() {
        for(AbstractGhost ghost : ghosts) {
            ghost.update();
        }
    }

    /**
     * Update the players input.
     * @param direction to update
     */
    public void updatePlayerInputBuffer(Directions direction) {
        player.setDirectionBuffer(direction);
    }

    /**
     * Retrieve player.
     * @return player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Retrieve ghosts.
     * @return ghosts
     */
    public List<AbstractGhost> getGhosts() {
        return ghosts;
    }

    /**
     * Adjust deltatime for entities.
     * @param toAdjust time delta to take into consideration
     */
    public void adjustDeltaTime(long toAdjust) {
        player.adjustDeltaTime(toAdjust);
        for(AbstractGhost g : ghosts) {
            g.adjustDeltaTime(toAdjust);
        }
    }

    /**
     * Apply frightening behavior to entities.
     */
    public void applyFrightening() {
        player.applyFrightening();
        for(AbstractGhost g : ghosts) { g.applyFrightening(); }
    }

    /**
     * Check player <=> ghost collision behavior
     * @return true if player gets killed by the collision
     */
    public boolean checkIfCollisionKillsPlayer() {
        for(AbstractGhost g : ghosts) {
            if(g.evaluatePlayerCollision(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove frightening effect on all entities.
     */
    public void removeFrightening() {
        player.removeFrightening();
        for(AbstractGhost g : ghosts) { g.removeFrightening(); }
    }
}
