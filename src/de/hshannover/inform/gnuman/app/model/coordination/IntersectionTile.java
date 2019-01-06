package de.hshannover.inform.gnuman.app.model.coordination;

import java.util.Collections;
import java.util.List;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.model.AbstractGhost.MovementFlags;
import de.hshannover.inform.gnuman.app.model.storage.MapCell;
import de.hshannover.inform.gnuman.app.util.Helper;

/**
 * Intersection that can decide the next direction of a ghost.
 * @author Marc Herschel
 */

public class IntersectionTile {
    private int x, y;
    private List<Directions> possibleDirections;
    private List<MapCell> ghostWalls;

    /**
     * Construct a new intersection tile.
     * @param x cell of intersection.
     * @param y cell of intersection.
     * @param directions possible directions.
     * @param ghostWalls ghostwalls to take care of.
     */
    public IntersectionTile(int x, int y, List<Directions> directions, List<MapCell> ghostWalls) {
        this.possibleDirections = directions;
        this.ghostWalls = ghostWalls;
        this.x = x;
        this.y = y;
        sortList();
    }

    /**
     * Decide the next move from this intersection.
     * @param targetCell target
     * @param current current direction
     * @param flag movement flag to take into consideration
     * @return next direction to move to.
     */
    public Directions nextMove(MapCell targetCell, Directions current, MovementFlags flag) {
        double heuristic = Double.MAX_VALUE, heuristicTmp;
        Directions result = Directions.NONE;
        for(Directions d : possibleDirections) {
            if(fulfillsRequirements(current, d, flag)) {
                heuristicTmp = Helper.euclideanDistance(x + d.getXModifier(), y + d.getYModifier(), targetCell.getCellX(), targetCell.getCellY());
                if(heuristicTmp < heuristic) {
                    heuristic = heuristicTmp;
                    result = d;
                }
            }
        }
        return result;
    }

    /**
     * Move randomly from this intersection and does not take ghost walls into consideration
     * @param current current direction
     * @return random possible direction.
     */
    public Directions randomMove(Directions current) {
        Collections.shuffle(possibleDirections);
        for(Directions d : possibleDirections) {
            if(fulfillsRequirements(current, d, MovementFlags.NONE)) { return d; }
        }
        sortList();
        return Directions.NONE;
    }

    /**
     * Check if a directions fulfills all requirements given a flag.
     * @param current current direction
     * @param toCheck direction to check for
     * @param flag movement flag of entity
     * @return true if it fulfills the requirements
     */
    private boolean fulfillsRequirements(Directions current, Directions toCheck, MovementFlags flag) {

        if(flag == MovementFlags.MUST_REVERSE) {
            if(current.invert() == toCheck) { return true; }
        }

        if(flag != MovementFlags.CAN_REVERSE && current.invert() == toCheck) { return false; }

        if(flag != MovementFlags.CAN_PASS_GHOSTWALL) {
            for(MapCell ghostWall : ghostWalls) {
                if(ghostWall.collision(x + toCheck.getXModifier(), y + toCheck.getYModifier())) { return false; }
            }
        }

        return true;
    }

    /**
     * Restore priorities after random screwed everything up.
     */
    private void sortList() {
        possibleDirections.sort((Directions d1, Directions d2) -> (Integer.compare(d1.ordinal(), d2.ordinal())));
    }

    @Override
    public String toString() { return possibleDirections.toString(); }
}
