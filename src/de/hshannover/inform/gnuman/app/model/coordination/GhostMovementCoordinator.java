package de.hshannover.inform.gnuman.app.model.coordination;

import java.awt.Point;
import java.util.HashMap;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.model.AbstractGhost;
import de.hshannover.inform.gnuman.app.model.AbstractGhost.MovementFlags;
import de.hshannover.inform.gnuman.app.model.Player;
import de.hshannover.inform.gnuman.app.model.ghosts.Blinky;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.GameVariableTracker;
import de.hshannover.inform.gnuman.app.model.storage.MapCell;
import de.hshannover.inform.gnuman.app.model.storage.MapData;
import de.hshannover.inform.gnuman.app.modules.TileMap;

/**
 * Manages coordination of ghost movement for all ghosts on a map.
 * @author Marc Herschel
 */

public class GhostMovementCoordinator {

    /**
     * Status for that ghosts get if the ask to leave the base.
     * @author Marc Herschel
     */
    public enum ReadyStatus {
        LEAVE, DELAY, WAIT
    }

    private BaseDispatcher dispatch;
    private IntersectionLookUpTable table;
    private GameVariableTracker tracker;
    private HashMap<EntityObjects, MapCell> scatterPoints;
    private HashMap<EntityObjects, Point> spawnPoints;
    private Player player;
    private Blinky blinky;
    private boolean[][] collisionData, slowdown;
    private int pacDots;

    /**
     * Create a new coordinator.
     * @param dyn Dynamic user options depending values.
     * @param map tile map.
     * @param mapData mapData.
     * @param player Player object for ghosts to access.
     * @param tracker GameVariableTracker for required state data.
     */
    public GhostMovementCoordinator(DynamicVariables dyn, TileMap map, MapData mapData, Player player, GameVariableTracker tracker) {
        this.dispatch = new BaseDispatcher(dyn, map.getGhostBases());
        this.table = map.getIntersectionLookUpTable();
        this.player = player;
        this.scatterPoints = mapData.getScatterPoints();
        this.collisionData = mapData.getGhostNavigationData();
        this.spawnPoints = new HashMap<>();
        this.tracker = tracker;
        this.pacDots = mapData.getPacDots();
        this.slowdown = mapData.getSlowdown();
        for(int i = 0; i < EntityObjects.values().length-1; i++) {
            spawnPoints.put(EntityObjects.values()[i+1], map.getGhostSpawns().get(i));
        }
    }

    /**
     * Calculates a direction at an intersection.
     * @param cellX current x cell
     * @param cellY current y cell
     * @param targetCell target cell
     * @param current current direction
     * @param flag movement flag to take into consideration
     * @return the next direction
     */
    public Directions evaluateIntersection(int cellX, int cellY, MapCell targetCell, Directions current, MovementFlags flag) {
        return table.get(cellX, cellY).nextMove(targetCell, current, flag);
    }

    /**
     * Calculates a random direction.
     * @param cellX current x cell
     * @param cellY current y cell
     * @param current current direction
     * @return the next direction
     */
    public Directions randomDirectionNoGhostWalls(int cellX, int cellY, Directions current) {
        return table.get(cellX, cellY).randomMove(current);
    }

    /**
     * @param type of entity
     * @return Scatter point for entity
     */
    public MapCell getScatterPoint(EntityObjects type) {
        return scatterPoints.get(type);
    }

    /**
     * @return Targeted base.
     */
    public MapCell getBasePoint() {
        return dispatch.getBasePoint();
    }

    /**
     * @return Occupied base dummy.
     */
    public MapCell baseOccupied() {
        return dispatch.occupied();
    }

    /**
     * Tell the dispatcher to remove the lock from the base.
     * @param baseToReturn lock to remove.
     */
    public void freeBase(MapCell baseToReturn) {
        dispatch.free(baseToReturn);
    }

    /**
     * @return Blinkys x cell
     */
    public int getBlinkyX() {
        return blinky.clampCellX();
    }

    /**
     * @return Blinkys y cell
     */
    public int getBlinkyY() {
        return blinky.clampCellY();
    }

    /**
     * @return player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return ghost collision data array
     */
    public boolean[][] getCollisionData() {
        return collisionData;
    }

    /**
     * @param cellX of entity
     * @param cellY of entity
     * @return true if in slow down area
     */
    public boolean isSlowDown(int cellX, int cellY) {
        return slowdown[cellX][cellY];
    }

    /**
     * Reset Coordinator
     */
    public void reset() {
        dispatch.reset();
    }

    /**
     * Decide if a ghost can start to be operational.
     * @param ghost to check for
     * @return a ready status for the ghost
     */
    public ReadyStatus canStart(AbstractGhost ghost) {
        if(ghost.getEntityType() == EntityObjects.BLINKY || ghost.getEntityType() == EntityObjects.PINKY) { return ReadyStatus.LEAVE; }
        if(tracker.getLevel() >= 3) {
            if(ghost.getEntityType() == EntityObjects.CLYDE) { blinky.clydeHasLeft(); }
            return ReadyStatus.DELAY;
        } else {
            if(tracker.getLevel() == 1) {
                if(ghost.getEntityType() == EntityObjects.INKY && tracker.getEatenPacDots() >= (int) Math.round(0.125*pacDots)) {
                    return ReadyStatus.LEAVE; }
                if(ghost.getEntityType() == EntityObjects.CLYDE && tracker.getEatenPacDots() >= (int) Math.round(0.25*pacDots)) {
                    blinky.clydeHasLeft();
                    return ReadyStatus.LEAVE;
                }
            }
            if(tracker.getLevel() == 2) {
                if(ghost.getEntityType() == EntityObjects.INKY) { return ReadyStatus.DELAY; }
                if(ghost.getEntityType() == EntityObjects.CLYDE && tracker.getEatenPacDots() >= (int) Math.round(0.2*pacDots)) {
                    blinky.clydeHasLeft();
                    return ReadyStatus.LEAVE;
                }
            }
        }
        return ReadyStatus.WAIT;
    }

    /**
     * @param entityType type of entity
     * @return spawnpoint for entity
     */
    public Point getSpawn(EntityObjects entityType) {
        return spawnPoints.get(entityType);
    }

    /**
     * Set Blinky because Inky needs the information.
     * @param blinky Blinky
     */
    public void setBlinky(Blinky blinky) {
        this.blinky = blinky;
    }
}
