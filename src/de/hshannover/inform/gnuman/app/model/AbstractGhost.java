package de.hshannover.inform.gnuman.app.model;

import java.util.LinkedList;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.GhostBehaviorState;
import de.hshannover.inform.gnuman.app.enums.GhostMovementStates;
import de.hshannover.inform.gnuman.app.enums.TileType;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.model.storage.PathNode;
import de.hshannover.inform.gnuman.app.rules.EntitySpeedRules.SpeedTypes;
import de.hshannover.inform.gnuman.app.model.coordination.GhostMovementCoordinator;
import de.hshannover.inform.gnuman.app.model.coordination.InternalGhostStateSupervisor;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.GameVariableTracker;
import de.hshannover.inform.gnuman.app.model.storage.MapCell;
import de.hshannover.inform.gnuman.app.util.AStarPathfinding;

/**
 * Superclass for ghosts. Uses a mixture of A* and Pacman's intersection based movement algorithm to decide movement.
 * @author Marc Herschel
 */

public abstract class AbstractGhost extends AbstractEntity {
    protected GhostMovementCoordinator coordinator;
    protected MapCell targetedPathTile;
    protected boolean isSlow;
    private InternalGhostStateSupervisor stateSupervisor;
    private AStarPathfinding pathfinding;
    private LinkedList<PathNode> path;
    private Directions currentDirection, oldDirection;
    private GhostBehaviorState behaviorState;
    private GhostMovementStates movementState, queuedMovementState;
    private MovementFlags movementFlag;
    private MapCell[] moveAroundInsideSpawn;
    private boolean targetCellReached, pathCompleted, movementStateSwitchQueued, died;
    private int moveAroundInsideSpawnIndex;

    /**
     * Special flags for ghost movement operations.
     * @author Marc Herschel
     */
    public enum MovementFlags {
        CAN_PASS_GHOSTWALL, MUST_REVERSE, CAN_REVERSE, NONE
    }

    /**
     * Creates a ghost superclass.
     * @param entity type of the entity.
     * @param dyn Dynamic user options depending values.
     * @param coordinator Movement coordinator for ghosts.
     * @param tracker to track game variables
     */
    protected AbstractGhost(EntityObjects entity, DynamicVariables dyn, GhostMovementCoordinator coordinator, GameVariableTracker tracker) {
        super(0, 0, entity, coordinator.getCollisionData(), dyn, tracker);
        this.pathfinding = new AStarPathfinding(collisionData);
        this.path = new LinkedList<>();
        this.persistentTasks = new LinkedList<>();
        this.moveAroundInsideSpawn = new MapCell[3];
        this.coordinator = coordinator;
        this.stateSupervisor = new InternalGhostStateSupervisor(this);
        reset();

    //Task for gracefully dealing with state switches.
        persistentTasks.add(() -> {
            if(movementStateSwitchQueued && targetCellReached) { setMovementState(queuedMovementState); movementStateSwitchQueued = false; }
        });

    //Task for dealing with slow down fields
        persistentTasks.add(() -> {
           if(isInsideMap() && !isSlow && behaviorState != GhostBehaviorState.DEAD && coordinator.isSlowDown(floorCellX(), floorCellY())) {
               isSlow = true;
               computeSpeed(SpeedTypes.GHOST_TUNNEL);
               transientTasks.add(() -> {
                   if(behaviorState == GhostBehaviorState.DEAD) { isSlow = false; return true; }
                   if(isInsideMap() && !coordinator.isSlowDown(floorCellX(), floorCellY())) {
                       isSlow = false;
                       computeSpeed(behaviorState == GhostBehaviorState.NORMAL ? SpeedTypes.GHOST_NORMAL : SpeedTypes.GHOST_FRIGHT);
                       return true;
                   }
                   return false;
               });
           }
        });
    }

    /**
     * Set a new movement state gracefully.
     * @param newState to set.
     */
    public void setMovementState(GhostMovementStates newState) {
    //Reset slows if we switch states, states that allow slows will set it again once the persistent task runs.
        if(isSlow) { isSlow = false; }
    //Return if we are dead but the callback is trying to set a new state
        if(isEaten() && (newState == GhostMovementStates.SCATTER || newState == GhostMovementStates.CHASE)) { return; }
    //Queue a state switch if we're not in the middle of a target cell. Forcing a switch here will make the ghost behave unexpected and buggy.
        if(!targetCellReached) {
            movementStateSwitchQueued = true;
            queuedMovementState = newState;
            return;
        }
    //Null the targetedPathTile if we're not dealing with a cooldown, doing this while waiting for a cooldown will prevent the base dispatcher from freeing the base again.
        if(newState != GhostMovementStates.COOLDOWN) {
            targetedPathTile = null;
        }
    //Set reverse flag according to pacman rules
        if((movementState == GhostMovementStates.SCATTER && (newState == GhostMovementStates.CHASE || newState == GhostMovementStates.FRIGHTENED)) ||
           (movementState == GhostMovementStates.CHASE && (newState == GhostMovementStates.SCATTER || newState == GhostMovementStates.FRIGHTENED))) {
            movementFlag = MovementFlags.CAN_REVERSE;
        }
   //Reverse if in frightened
        if(newState == GhostMovementStates.FRIGHTENED) {
            currentDirection = currentDirection.invert();
            stateSupervisor.pause();
        }
    //Reset some values and do the switch
        targetCellReached = true;
        pathCompleted = false;
        path.clear();
        movementState = newState;
    }

    /**
     * @return behavior state of entity.
     */
    public GhostBehaviorState getBehaviorState() {
        return behaviorState;
    }

    /**
     * @return movement state of entity.
     */
    public GhostMovementStates getMovementState() {
        return movementState;
    }

    /**
     * @return Current direction of ghost.
     */
    public Directions getDirection() {
        return currentDirection;
    }

    /**
     * @param p is needed in case we are in chase mode
     * @return Current target tile (null if none set)
     */
    public MapCell getCurrentTargetTile(Player p) {
        if(movementState == GhostMovementStates.SCATTER) { return coordinator.getScatterPoint(getEntityType()); }
        if(movementState == GhostMovementStates.FRIGHTENED || movementState == GhostMovementStates.COOLDOWN) { return null; }
        if(movementState == GhostMovementStates.WAITING && moveAroundInsideSpawn[0] != null) { return moveAroundInsideSpawn[moveAroundInsideSpawnIndex]; }
        if(movementState == GhostMovementStates.CHASE) { return decideChaseBehavior(p); }
        return targetedPathTile;
    }

    /**
     * @return true if frightening nearly over
     */
    public boolean isFrighteningFlashOver() {
        return tracker.isFrightenedFlash();
    }

    /**
     * @return true if ghost is currently eaten
     */
    private boolean isEaten() {
        return behaviorState == GhostBehaviorState.DEAD;
    }

    /**
     * Decide what to do!
     */
    @Override
    protected void move() {
        checkMapBounds();
        stateSupervisor.update();

        switch(movementState) {
            case WAITING:
                startGhost();
                break;
            case LEAVE_BASE:
                leaveBase();
                break;
            case CHASE:
                chase();
                break;
            case FRIGHTENED:
                frightened();
                break;
            case SCATTER:
                scatter();
                break;
            case DEAD:
                backToBase();
                break;
            default:
                return;
        }
    }

    /**
     * Notify the ghost that the internal state has just changed.
     */
    public void globalStateHasChanged() {
        if(getMovementState() == GhostMovementStates.CHASE || getMovementState() == GhostMovementStates.SCATTER) {
            setMovementState(stateSupervisor.getCurrentGlobalState());
        }
    }

    @Override
    public void applyFrightening() {
    //Can't apply that to dead ghosts
        if(movementState == GhostMovementStates.DEAD || died) { return; }
    //Shared actions
        behaviorState = GhostBehaviorState.FRIGHTENED;
        computeSpeed(SpeedTypes.GHOST_FRIGHT);
    //Apply behavior but don't switch movement state yet (for ghosts inside the base, they will switch once the leave and become ready!)
        if(movementState == GhostMovementStates.COOLDOWN || movementState == GhostMovementStates.LEAVE_BASE || movementState == GhostMovementStates.WAITING) { return; }
   //If we're not in the FRIGHTENED state already it's time to change now
        if(movementState != GhostMovementStates.FRIGHTENED) {
            setMovementState(GhostMovementStates.FRIGHTENED);
        }
    }

    @Override
    public void removeFrightening() {
   //Normal speed again
        computeSpeed(SpeedTypes.GHOST_NORMAL);
   //Resume state supervisor if the ghost survived
        if(behaviorState == GhostBehaviorState.FRIGHTENED) { behaviorState = GhostBehaviorState.NORMAL; stateSupervisor.resume(); }
   //Set movement state if we're in frightening
        if(movementState == GhostMovementStates.FRIGHTENED) { setMovementState(stateSupervisor.getCurrentGlobalState()); }
    }

    /**
     * Evaluate what happens when player and ghost meet each other.
     * @param player to check for
     * @return true if the player dies, false if not.
     */
    public boolean evaluatePlayerCollision(Player player) {
        if(this.intersects(player.getBounds())) {
        //Dead or not Dead?
            if((movementState == GhostMovementStates.CHASE || movementState == GhostMovementStates.SCATTER || movementState == GhostMovementStates.LEAVE_BASE) && behaviorState != GhostBehaviorState.FRIGHTENED && !isEaten()) {
                return !Constants.DEBUG_CANT_DIE;
            }
        //Eat the ghost
            if(behaviorState == GhostBehaviorState.FRIGHTENED && !isEaten()) {
                GameLauncher.am().playSound("EATING_GHOST");
                tracker.eatGhost();
                behaviorState = GhostBehaviorState.DEAD;
                stateSupervisor.pause();
                died = true;
                computeSpeed(SpeedTypes.GHOST_NORMAL);
                if(isInsideMap()) {
                    setMovementState(GhostMovementStates.DEAD);
                } else {
            //Don't let the return to base start if the ghost is out of bounds, it will kill the A* Algorithm
                    transientTasks.add(() -> {
                       if(isInsideMap()) {
                           setMovementState(GhostMovementStates.DEAD);
                           return true;
                       }
                       return false;
                    });
                }
            }
        }
        return false;
    }

    /**
     * Moves to the current target position.
     */
    private void moveToCurrentTarget() {
        switch (currentDirection) {
            case UP: setY(getY() - getSpeed()); break;
            case DOWN: setY(getY() + getSpeed()); break;
            case RIGHT: setX(getX() + getSpeed()); break;
            case LEFT: setX(getX() - getSpeed()); break;
            default: return;
        }

        if(targetCellReached && getTileType() == TileType.INTERSECTION && oldDirection != currentDirection) { centerPositionOnCurrentCell(); }
        targetCellReached = occupiesTargetCell();
    }

   /*     ____  ________  _____ _    __________  ____
    *    / __ )/ ____/ / / /   | |  / /  _/ __ \/ __ \
    *   / __  / __/ / /_/ / /| | | / // // / / / /_/ /
    *  / /_/ / /___/ __  / ___ | |/ // // /_/ / _, _/
    * /_____/_____/_/ /_/_/  |_|___/___/\____/_/ |_|
    */

    /**
     * Start the ghosts state switch cycle.
     */
    private void startGhost() {
        switch(coordinator.canStart(this)) {
            case LEAVE:
                prepareLeave();
                break;
            case DELAY:
                if(!timedTasks.taskExists("startdelay")) {
                    timedTasks.createTask("startdelay", getEntityType().getLeaveDelay());
                    transientTasks.add(() -> {
                        if(timedTasks.isFinished("startdelay")) {
                            prepareLeave();
                            return true;
                        }
                        return false;
                    });
                }
            default:
            //In both cases we want the ghosts to move up and down while waiting
                moveAroundInsideSpawn();
        }
    }

    /**
     * Let the ghosts move up and down if they are waiting in the ghost house.
     */
    private void moveAroundInsideSpawn() {
        if(moveAroundInsideSpawn[0] == null) { moveAroundInsideSpawn[0] = new MapCell(clampCellX(), clampCellY()+1); moveAroundInsideSpawn[1] = new MapCell(clampCellX(), clampCellY()); moveAroundInsideSpawn[2] = new MapCell(clampCellX(), clampCellY()-1); }
        if(!pathCompleted) {
            followPathWithAStar(moveAroundInsideSpawn[moveAroundInsideSpawnIndex]);
        } else {
            moveAroundInsideSpawnIndex = ++moveAroundInsideSpawnIndex%moveAroundInsideSpawn.length;
            pathCompleted = false;
        }
    }

    /**
     * Prepare the leave operation
     */
    private void prepareLeave() {
    //Can leave because either in front of ghost house or in the middle cell
        if(movementFlag != MovementFlags.CAN_PASS_GHOSTWALL || moveAroundInsideSpawn[0] == null || (!pathCompleted && clampCellY() == moveAroundInsideSpawn[1].getCellY())) {
            pathCompleted = false; targetedPathTile = null; targetCellReached = true;
            setMovementState(GhostMovementStates.LEAVE_BASE);
            stateSupervisor.start();
        } else {
    //Need to reach middle cell of up and down animation first
            pathCompleted = false;
            followPathWithAStar(moveAroundInsideSpawn[1]);
        }
    }

    /**
     * Guide ghosts outside of spawn and then sets mode to current game state.
     */
    private void leaveBase() {
    //Outside of base, okay
        if(movementFlag != MovementFlags.CAN_PASS_GHOSTWALL) { setMovementState(stateSupervisor.getCurrentGlobalState()); return; }
    //Inside base
        if(!pathCompleted) {
            if(targetedPathTile == null) { targetedPathTile = new MapCell(clampCellX(), clampCellY() - 3); }
            followPathWithAStar(targetedPathTile);
        } else {
    //Base left
            movementFlag = MovementFlags.NONE;
            setMovementState(tracker.isFrightened() && !died ? GhostMovementStates.FRIGHTENED : stateSupervisor.getCurrentGlobalState());
            died = false;
            move();
        }
    }

    /**
     * Frightened mode
     */
    private void frightened() {
        moveRandomly();
    }

    /**
     * Scatter mode.
     */
    private void scatter() {
        intersectionDecidedMovementToCell(coordinator.getScatterPoint(getEntityType()));
    }

    /**
     * Chase mode
     */
    private void chase() {
        if(targetCellReached) { targetedPathTile = decideChaseBehavior(coordinator.getPlayer()); }
        intersectionDecidedMovementToCell(targetedPathTile);
    }

    /**
     * Moves a ghost back to the base to initiate the re-spawn cool down.
     */
    private void backToBase() {
    //All bases occupied, patrol
        if(targetedPathTile == coordinator.baseOccupied()) {
            targetedPathTile = coordinator.getBasePoint();
            moveRandomly();
            return;
        }
    //Finished
        if(pathCompleted) {
            behaviorState = GhostBehaviorState.NORMAL;
            computeSpeed(SpeedTypes.GHOST_NORMAL);
            setMovementState(GhostMovementStates.COOLDOWN);
        //Add cooldown task.
            timedTasks.createTask("cooldown", 1000);
            transientTasks.add(() -> {
                if(timedTasks.isFinished("cooldown")) {
                    stateSupervisor.resume();
                    coordinator.freeBase(targetedPathTile);
                    targetedPathTile = null;
                    movementFlag = MovementFlags.CAN_PASS_GHOSTWALL;
                    setMovementState(GhostMovementStates.LEAVE_BASE);
                    return true;
                }
                return false;
            });
            return;
        }
    //Assign new base
        if(targetedPathTile == null) {
            targetedPathTile = coordinator.getBasePoint();
            pathCompleted = false;
            return;
        }
    //Follow base
        followPathWithAStar(targetedPathTile);
    }

   /*
    *     _____     __  _____   __  __  __    __  ___  _____ _____   ___     __     ___    _    __    __   ___
    *     \_   \ /\ \ \/__   \ /__\/__\/ _\  /__\/ __\/__   \\_   \ /___\ /\ \ \   / __\  /_\  / _\  /__\ /   \
    *      / /\//  \/ /  / /\//_\ / \//\ \  /_\ / /     / /\/ / /\///  ///  \/ /  /__\// //_\\ \ \  /_\  / /\ /
    *   /\/ /_ / /\  /  / /  //__/ _  \_\ \//__/ /___  / / /\/ /_ / \_/// /\  /  / \/  \/  _  \_\ \//__ / /_//
    *   \____/ \_\ \/   \/   \__/\/ \_/\__/\__/\____/  \/  \____/ \___/ \_\ \/   \_____/\_/ \_/\__/\__//___,'
    */

    /**
     * Move to a target using the intersection based method.
     * @param target to move to.
     */
    private void intersectionDecidedMovementToCell(MapCell target) {
        if(targetCellReached) {
            if(getTileType() == TileType.INTERSECTION) {
                updateDirection(coordinator.evaluateIntersection(clampCellX(), clampCellY(), target, currentDirection, movementFlag));
                if(movementFlag == MovementFlags.MUST_REVERSE || movementFlag == MovementFlags.CAN_REVERSE) { movementFlag = MovementFlags.NONE; }
            }
            evaluateNextCell(currentDirection, clampCellX(), clampCellY());
        }
        moveToCurrentTarget();
    }

    /**
     * Move randomly at intersections.
     */
    private void moveRandomly() {
        if(targetCellReached) {
            if(getTileType() == TileType.INTERSECTION) {
                updateDirection(coordinator.randomDirectionNoGhostWalls(clampCellX(), clampCellY(), currentDirection));
            }
            evaluateNextCell(currentDirection, clampCellX(), clampCellY());
        }
        moveToCurrentTarget();
    }

    /**
     * Evaluate the next move and set the target values if we're using tile based decided movement.
     * @param d direction to move to.
     * @param cellX current cellX
     * @param cellY current cellY
     */
    private void evaluateNextCell(Directions d, int cellX, int cellY) {
        if(d == Directions.NONE) { return; }
        cellX+=(d != Directions.UP && d != Directions.DOWN) ? (d == Directions.LEFT ? -1 : 1) : 0;
        cellY+=(d != Directions.LEFT && d != Directions.RIGHT) ? (d == Directions.UP ? -1 : 1) : 0;
        setTargetCell(cellX, cellY);
    }

    /*    _             _      __    ___   ___   __   _____  _____
     *   /_\  __/\__   /_\    / /   / _ \ /___\ /__\  \_   \/__   \ /\  /\ /\/\
     *  //_\\ \    /  //_\\  / /   / /_\///  /// \//   / /\/  / /\// /_/ //    \
     * /  _  \/_  _\ /  _  \/ /___/ /_\\/ \_/// _  \/\/ /_   / /  / __  // /\/\ \
     * \_/ \_/  \/   \_/ \_/\____/\____/\___/ \/ \_/\____/   \/   \/ /_/ \/    \/
     *
     */

    /**
     * Calculate the path once and then follow it. Needed if we want to follow a static target
     * The boolean value pathFollowed will be set to true once the ghost has arrived at the location.
     * @param cellX goal-X
     * @param cellY goal-Y
     */
    private void followPathWithAStar(int cellX, int cellY) {
        if(!pathCompleted) {
            path = pathfinding.findPath(clampCellX(), clampCellY(), cellX, cellY);
            pathCompleted = false;
        }
        if(targetCellReached) {
            if(path.size() == 0 ) { pathCompleted = true; return; }
            updateDirection(evaluateNextAStarNode(path.pop()));
        }
        moveToCurrentTarget();
    }

    /**
     * Shortcut for actual method.
     * @param cell to follow path.
     */
    private void followPathWithAStar(MapCell cell) { followPathWithAStar(cell.getCellX(), cell.getCellY()); }

    /**
     * Evaluate the next move and set the target values if we're using the A* algorithm.
     * @param current Node to evaluate for.
     * @return A direction to move towards to.
     */
    private Directions evaluateNextAStarNode(PathNode current) {
        setTargetCell(current.getX(), current.getY());
        if(clampCellX() == current.getX()) {
            return clampCellY() < current.getY() ? Directions.DOWN : Directions.UP;
        } else {
            return clampCellX() < current.getX() ? Directions.RIGHT : Directions.LEFT;
        }
    }

    /**
     * Deal with out of bounds teleport
     * @param offsetX x to teleport to, set to 0 if only teleport to y
     * @param offsetY y to teleport to, set to 0 if only teleport to x
     */
    private void handleTeleportation(double offsetX, double offsetY) {
        offsetX = (offsetX == 0) ? getX() : offsetX;
        offsetY = (offsetY == 0) ? getY() : offsetY;
        setX(offsetX); setY(offsetY);
    //Get grip in map task
        transientTasks.add(() -> {
            if(isInsideMap()) {
                evaluateNextCell(currentDirection, clampCellX(), clampCellY());
                return true;
            }
            return false;
        });
    }

    /**
     * Set correction flag if direction changes.
     * @param d new direction to set
     */
    private void updateDirection(Directions d) {
        oldDirection = currentDirection;
        currentDirection = d;
    }

    /*
     * Abstracts
     */

    /**
     * Set a chase target depending on the ghost behavior or switch to another state.
     * @param player player to chase.
     * @return a map cell with the current target
     */
    protected abstract MapCell decideChaseBehavior(Player player);

    /*
     * Overrides
     */

    @Override
    public void reset() {
        stateSupervisor.reset();
        stateSupervisor.loadLevel(tracker.getLevel(), tracker.getDifficulty());
        coordinator.reset();
        transientTasks.clear();
        timedTasks.clearTasks();
        movementState = GhostMovementStates.WAITING;
        behaviorState = GhostBehaviorState.NORMAL;
        currentDirection = oldDirection = Directions.NONE;
        movementStateSwitchQueued = pathCompleted = isSlow = died = false;
        movementFlag = (getEntityType() == EntityObjects.BLINKY) ? MovementFlags.NONE : MovementFlags.CAN_PASS_GHOSTWALL;
        targetedPathTile = null;
        targetCellReached = true;
        moveAroundInsideSpawnIndex = 0; for(int i = 0; i < moveAroundInsideSpawn.length; i++) { moveAroundInsideSpawn[i] = null; }
        path.clear();
        setSpawn(coordinator.getSpawn(getEntityType()));
        computeSpeed(SpeedTypes.GHOST_NORMAL);
    }

    @Override
    public void adjustDeltaTime(long toAdjust) {
        timedTasks.adjustDeltaTimeForAllTasks(toAdjust);
        if(!stateSupervisor.isPaused()) {
            stateSupervisor.adjustDeltaTime(toAdjust);
        }
    }

    @Override
    protected void checkMapBounds() {
        if(this.getX() <= (-this.getWidth())-1) { handleTeleportation(dyn.getGameAreaWidth()-2, 0); return; }
        if(this.getX() >= (dyn.getGameAreaWidth()-1)) { handleTeleportation(-this.getWidth(), 0); return; }
        if(this.getY() <= (-this.getHeight())-1) { handleTeleportation(0, dyn.getGameAreaHeight()-2); return; }
        if(this.getY() >= (dyn.getGameAreaHeight()-1)) { handleTeleportation(0, -this.getHeight()); return; }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(400);
        sb.append("===");
        sb.append(getEntityType());
        sb.append("===");
        sb.append("\nTile: ");
        sb.append(getTileType());
        sb.append(" | X: ");
        sb.append(getX());
        sb.append(" | Y: ");
        sb.append(getY());
        sb.append(" | Direction: ");
        sb.append(currentDirection);
        if(movementStateSwitchQueued) { sb.append("\nQueued "); sb.append(queuedMovementState); }
        sb.append("\nMovement: ");
        sb.append(getMovementState());
        sb.append("\nBehavior: ");
        sb.append(getBehaviorState());
        sb.append("\nInternal State Timer -> Started: ");
        sb.append(stateSupervisor.isStarted());
        sb.append(" | Paused: ");
        sb.append(stateSupervisor.isPaused());
        sb.append("\nMovement Flag: ");
        sb.append(movementFlag);
        if(died) { sb.append("\nI am dead."); }
        return sb.toString();
    }
}
