package de.hshannover.inform.gnuman.app.model;
import java.awt.Point;
import java.util.LinkedList;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.enums.TileType;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.interfaces.PersistentGameTask;
import de.hshannover.inform.gnuman.app.interfaces.TransientGameTask;
import de.hshannover.inform.gnuman.app.model.coordination.TimedTasks;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.GameVariableTracker;
import de.hshannover.inform.gnuman.app.rules.EntitySpeedRules;
import de.hshannover.inform.gnuman.app.rules.EntitySpeedRules.SpeedTypes;

/**
 * Superclass for moving objects. An entity will always have a movement collision hit box depending on the block width/height.
 * @author Marc Herschel
 */

public abstract class AbstractEntity extends GameObject {
    protected TimedTasks timedTasks;
    protected DynamicVariables dyn;
    protected LinkedList<PersistentGameTask> persistentTasks;
    protected LinkedList<TransientGameTask> transientTasks;
    protected GameVariableTracker tracker;
    protected boolean[][] collisionData;
    private EntityObjects entityType;
    private boolean isMoving;
    private double currentSpeed, targetCenterX, targetCenterY;
    private int movementTrackX, movementTrackY;

    /**
     * Constructs an entity.
     * @param x X-Position
     * @param y Y-Position
     * @param entity Type of the entity.
     * @param collisionData Collision data.
     * @param dyn Dynamic user options depending values.
     * @param tracker to track game variables
     */
    AbstractEntity(double x, double y, EntityObjects entity, boolean[][] collisionData, DynamicVariables dyn, GameVariableTracker tracker) {
        super(x, y, dyn.getEntityHitboxWidth(), dyn.getEntityHitboxHeight());
        Log.info(getClass().getSimpleName(), "Creating Entity: " + entity);
        this.collisionData = collisionData;
        this.dyn = dyn;
        this.entityType = entity;
        this.timedTasks = new TimedTasks();
        this.transientTasks = new LinkedList<>();
        this.persistentTasks = new LinkedList<>();
        this.tracker = tracker;
    }

    /**
     * Update the entity.
     */
    public void update() {
       registerPreMovementCoordinates();

       //Call persistent tasks.
       persistentTasks.forEach(PersistentGameTask::execute);

       //Remove transient tasks if they finished.
       transientTasks.removeIf(TransientGameTask::isFinished);

       move();
       updateMovementStatus();
    }

    /**
     * @return The type of the entity.
     */
    public EntityObjects getEntityType() {
        return entityType;
    }

    /**
     * @return center x, only for collision chunk lookup
     */
    public int centerCellX() {
        return (int) Math.round((this.getX()+getWidth()) / dyn.getBlockWidth()) - 1;
    }

    /**
     * @return center y, only for collision chunk lookup
     */
    public int centerCellY() {
        return (int) Math.round((this.getY()+getHeight()) / dyn.getBlockHeight()) - 1;
    }

    /**
     * This is out of bounds safe.
     * @return current floored x-cell of the map.
     */
    public int floorCellX() {
        return (int) Math.floor(this.getX() / dyn.getBlockWidth());
    }

    /**
     * This is out of bounds safe.
     * @return current floored y-cell of the map.
     */
    public int floorCellY() {
        return (int) Math.floor(this.getY() / dyn.getBlockHeight());
    }

    /**
     * This is not out of bounds safe.
     * @return current clamped x-cell of the map.
     */
    public int clampCellX() {
        return (int) Math.round(this.getX() / dyn.getBlockWidth());
    }

    /**
     * This is not out of bounds safe.
     * @return current clamped y-cell of the map.
     */
    public int clampCellY() {
        return (int) Math.round(this.getY() / dyn.getBlockHeight());
    }

    /**
     * Must be implemented via registerPreMovementCoordinates() and updateMovementStatus()
     * @return true if entity is moving.
     */
    public boolean isMoving() {
        return isMoving;
    }

    /**
     * @return true if in bounds of map.
     */
    public boolean isInsideMap() {
        return  getX() >= 0 &&
                getY() >= 0 &&
                (int) getX() / dyn.getBlockWidth()  < dyn.getBlockAmountHorizontal() &&
                (int) getY() / dyn.getBlockHeight() < dyn.getBlockAmountVertical();
    }

    /**
     * Sets the entity to a spawn point.
     * @param coordinates The coordinates to set the spawn to.
     */
    protected void setSpawn(Point coordinates) {
        this.setX(coordinates.getX());
        this.setY(coordinates.getY());
    }

    /**
     * @return Current speed.
     */
    protected double getSpeed() {
        return currentSpeed;
    }

    /**
     * Set a new speed for the entity.
     * @param newSpeed to set
     */
    protected void setSpeed(double newSpeed) {
        currentSpeed = newSpeed;
    }

    /**
     * Modify speed according to game rules.
     * @param speedFactor modifier type
     */
    protected void computeSpeed(SpeedTypes speedFactor) {
        setSpeed(dyn.getBaseSpeed() * EntitySpeedRules.getSpeedMultiplier(speedFactor, tracker.getLevel()));
    }

    /**
     * Set a target cell to check for occupation.
     * @param cellX xCell to check for.
     * @param cellY yCell to check for.
     */
    protected void setTargetCell(int cellX, int cellY) {
        targetCenterX  = cellX * dyn.getBlockWidth() + (dyn.getBlockWidth() / 2.0);
        targetCenterY = cellY * dyn.getBlockHeight() + (dyn.getBlockHeight() / 2.0);
    }

    /**
     * If an entity fully occupies the targeted cell.
     * @return true if fully occupying the cell.
     */
    protected boolean occupiesTargetCell() {
        double entityCenterX = getX() + (getWidth() / 2.0);
        double entityCenterY = getY() + (getHeight() / 2.0);
        return Math.abs(targetCenterX-entityCenterX) <= currentSpeed &&
               Math.abs(targetCenterY-entityCenterY) <= currentSpeed;
    }

    /**
     * Center entity position in the middle of the current cell.<br>
     * This is the case when the approximation thinks we're in a cell, but the position is probably not exactly the center.
     */
    protected void centerPositionOnCurrentCell() {
        setX(clampCellX()*dyn.getBlockWidth()+1); setY(clampCellY()*dyn.getBlockHeight()+1);
    }

    /**
     * @return path object for the current cell.
     */
    protected TileType getTileType() {
        int cellX = (int) Math.round(getX() / dyn.getBlockWidth()), cellY = (int) Math.round(getY() / dyn.getBlockHeight());
        if(cellX <= 0 || cellX >= dyn.getBlockAmountHorizontal() - 1) { return TileType.HORIZONTAL; }
        if(cellY <= 0 || cellY >= dyn.getBlockAmountVertical() - 1) { return TileType.VERTICAL; }
        if(collisionData[cellY][cellX+1] && collisionData[cellY][cellX-1] && !(collisionData[cellY+1][cellX] || collisionData[cellY-1][cellX])) {
            return TileType.HORIZONTAL;
        }
        if(collisionData[cellY+1][cellX] && collisionData[cellY-1][cellX] && !(collisionData[cellY][cellX+1] || collisionData[cellY][cellX-1])) {
            return TileType.VERTICAL;
        }
        return TileType.INTERSECTION;
    }

    /**
     * Register coordinates for a movement check.
     */
    private void registerPreMovementCoordinates() {
        movementTrackX = (int) getX();
        movementTrackY = (int) getY();
    }

    /**
     * Check if coordinates changed.
     */
    private void updateMovementStatus() {
        isMoving = !(movementTrackX == (int) getX() && movementTrackY == (int) getY());
    }

    /*
     * Abstracts to implement.
     */

    /**
     * Apply frightening behavior.
     */
    public abstract void applyFrightening();

    /**
     * Remove frightening behavior.
     */
    public abstract void removeFrightening();

    /**
     * Adjust timed events by adding a delta time to their expiration.
     * @param toAdjust delta time to change.
     */
    public abstract void adjustDeltaTime(long toAdjust);

    /**
     * Reset the entity completely.
     */
    public abstract void reset();

    /**
     * Move the entity.
     */
    protected abstract void move();

    /**
     * Checks the map bounds and moves the entity at the opposite of the map if it goes out too far.
     */
    protected abstract void checkMapBounds();

    @Override
    public String toString() {
        return this.entityType + " -> " + super.toString();
    }
}
