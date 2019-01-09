package de.hshannover.inform.gnuman.app.model;

import java.awt.Point;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.GameVariableTracker;
import de.hshannover.inform.gnuman.app.model.storage.MapData;
import de.hshannover.inform.gnuman.app.modules.TileMap;
import de.hshannover.inform.gnuman.app.rules.EntitySpeedRules.SpeedTypes;

/**
 * Represents the player.
 * @author Marc Herschel
 */

public class Player extends AbstractEntity {
    private Directions playerDirection, bufferDirection;
    private Point spawnPoint;

    /**
     * Constructs a player.
     * @param dyn Dynamic user options depending values.
     * @param map Map of game
     * @param mapData Parsed mapdata
     * @param tracker game variable tracker
     */
    public Player(DynamicVariables dyn, TileMap map, MapData mapData, GameVariableTracker tracker) {
        super(0, 0, EntityObjects.PLAYER, mapData.getPlayerNavigationData(), dyn, tracker);
        this.spawnPoint = map.getPlayerSpawn();
        reset();
    }

    /**
     * Assigns a new key buffer.
     * @param nextAction Key buffer to set.
     */
    public void setDirectionBuffer(Directions nextAction) {
        if(nextAction == null) { return; }
        if(canSetBufferAsDirection(nextAction)) {
            playerDirection = nextAction;
            bufferDirection = Directions.NONE;
            return;
        }
        bufferDirection = nextAction;
    }

    @Override
    protected void move() {
        checkMapBounds();

        switch(getTileType()) {
            case INTERSECTION: {
                dealWithIntersection(); break;
            }
            case VERTICAL: {
                moveVertical(); break;
            }
            case HORIZONTAL: {
                moveHorizontal(); break;
            }
            default: break;
        }
    }

    @Override
    public void reset() {
        playerDirection = Directions.LEFT;
        bufferDirection = Directions.NONE;
        transientTasks.clear();
        timedTasks.clearTasks();
        setSpawn(spawnPoint);
        computeSpeed(SpeedTypes.PACMAN_NORMAL);
    }

    /**
     * @return current direction
     */
    public Directions getDirection() {
        return playerDirection;
    }

    /**
     * Handles switching of buffer and movement methods when we reach an intersection
     */
    private void dealWithIntersection() {
        boolean currentlyHorizontal = (playerDirection == Directions.LEFT || playerDirection == Directions.RIGHT);

        if(bufferDirection == Directions.NONE && currentlyHorizontal) { moveHorizontal(); return; }
        if(bufferDirection == Directions.NONE && !currentlyHorizontal) { moveVertical(); return; }

        setTargetCell(clampCellX(), clampCellY());

        if(occupiesTargetCell() && isNextMovePossible(currentlyHorizontal)) {
        //Correct any "bad" positions duo to imprecise approximations.
            centerPositionOnCurrentCell();
            playerDirection = bufferDirection;
            bufferDirection = Directions.NONE;
            currentlyHorizontal = (playerDirection == Directions.LEFT || playerDirection == Directions.RIGHT);
        }

        if(currentlyHorizontal) { moveHorizontal(); } else { moveVertical(); }
    }

    /**
     * Check if in the middle of an intersection the buffered direction is a legal move.
     * @param horizontal if we're horizontal
     * @return if the next move is possible without collision
     */
    private boolean isNextMovePossible(boolean horizontal) {
        int nextXCell = clampCellX() + (horizontal ? 0 : ((bufferDirection == Directions.LEFT) ? -1 : 1));
        int nextYCell = clampCellY() + (!horizontal ? 0 : ((bufferDirection == Directions.UP) ? -1 : 1));
        return collisionData[nextYCell][nextXCell];
    }

    /**
     * Horizontal Movement<br>
     * If movement is not possible because the player would get stuck in a wall we will set the position to 1px distanced from that wall.
     */
    private void moveHorizontal() {
        double x = getX();
        switch(playerDirection) {
            case LEFT: x-=getSpeed(); break;
            case RIGHT: x+=getSpeed(); break;
            default: return;
        }

        int nextXCell = (int) (x / dyn.getBlockWidth()) + ((playerDirection == Directions.LEFT) ? 0 : 1);
        if(nextXCell > dyn.getBlockAmountHorizontal()-1 || nextXCell < 0) { setX(x); return; }

        if(!collisionData[clampCellY()][nextXCell]) {
            x = (playerDirection == Directions.LEFT ? nextXCell+1 : nextXCell-1) * dyn.getBlockWidth() + 1;
        }

        setX(x);
    }

    /**
     * Vertical Movement<br>
     * If movement is not possible because the player would get stuck in a wall we will set the position to 1px distanced from that wall.
     */
    private void moveVertical() {
        double y = getY();
        switch(playerDirection) {
            case UP: y-=getSpeed(); break;
            case DOWN: y+=getSpeed(); break;
            default: return;
        }

        int nextYCell = (int) (y / dyn.getBlockHeight()) + ((playerDirection == Directions.UP) ? 0 : 1);
        if(nextYCell > dyn.getBlockAmountVertical()-1 || nextYCell < 0) { setY(y); return; }

        if(!collisionData[nextYCell][clampCellX()]) {
            y = (playerDirection == Directions.UP ? nextYCell+1 : nextYCell-1) * dyn.getBlockHeight() + 1;
        }

        setY(y);
    }

    /**
     * Is it okay to update our player direction without setting a new buffer?
     * @param buffer Key buffer to check on.
     * @return If we can set the buffer as our direction.
     */
    private boolean canSetBufferAsDirection(Directions buffer) {
        return ((buffer == Directions.LEFT || buffer == Directions.RIGHT) &&
            (playerDirection == Directions.LEFT || playerDirection == Directions.RIGHT) ||
            (buffer == Directions.UP || buffer == Directions.DOWN) &&
            (playerDirection == Directions.UP || playerDirection == Directions.DOWN));
    }

    @Override
    public void applyFrightening() {
        computeSpeed(SpeedTypes.PACMAN_FRIGHT);
    }

    @Override
    public void removeFrightening() {
        computeSpeed(SpeedTypes.PACMAN_NORMAL);
    }

    @Override
    public void adjustDeltaTime(long toAdjust) {
        timedTasks.adjustDeltaTimeForAllTasks(toAdjust);
    }

    @Override
    protected void checkMapBounds() {
        if(this.getX() <= (-this.getWidth())-1) { this.setX(dyn.getGameAreaWidth()-2); return; }
        if(this.getX() >= (dyn.getGameAreaWidth()-1)) { this.setX(-this.getWidth()); return; }
        if(this.getY() <= (-this.getHeight())-1) { this.setY(dyn.getGameAreaHeight()-2); return; }
        if(this.getY() >= (dyn.getGameAreaHeight()-1)) { this.setY(-this.getHeight()); return; }
    }

    @Override
    public String toString() {
        return "[Player @ " + getTileType() + " -> X: " + this.getX() + " Y: " + this.getY() + " Current: " + playerDirection + " Buffer: " + bufferDirection + "]";
    }
}
