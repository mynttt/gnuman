package de.hshannover.inform.gnuman.app.model.coordination;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.MapCell;

/**
  * Dispatcher that queues ghosts if all spawn points inside the ghost house are full.
  * @author Marc Herschel
  */

public class BaseDispatcher {
    private HashSet<MapCell> queuedPoints;
    private List<MapCell> bases;
    private MapCell occupied;
    private int dispatchIndex;

    /**
     * Construct a return to base dispatcher.
     * @param dyn Dynamic user options depending values.
     * @param possibleBases Possible bases for ghosts to return.
     */
    public BaseDispatcher(DynamicVariables dyn, List<Point> possibleBases) {
        queuedPoints = new HashSet<>();
        bases = new ArrayList<>();
        dispatchIndex = 0;
        occupied = new MapCell(Integer.MAX_VALUE, Integer.MIN_VALUE);
        for(Point p : possibleBases) {
            bases.add(new MapCell((int) p.getX() / dyn.getBlockWidth(), (int) p.getY() / dyn.getBlockHeight()));
        }
    }

    /**
     * Get a free base point and lock the point.
     * @return Either a valid point or the dummy element if all bases are occupied.
     */
    public MapCell getBasePoint() {
        int tries = 0;
        while(queuedPoints.contains(bases.get(dispatchIndex)) && tries < bases.size()) {
            dispatchIndex = ++dispatchIndex % bases.size();
            tries++;
        }
        if(tries < bases.size()) {
            queuedPoints.add(bases.get(dispatchIndex));
            return bases.get(dispatchIndex);
        }
        return occupied;
    }

    /**
     * Get occupied point for checking.
     * @return occupied point.
     */
    public MapCell occupied() { return occupied; }

    /**
     * Free base after a ghost spawns again.
     * @param base to free.
     */
    public void free(MapCell base) { queuedPoints.remove(base); if(queuedPoints.size() == 0) { dispatchIndex = 0; } }

    /**
     * Clear for game reset.
     */
    public void reset() { queuedPoints.clear(); dispatchIndex = 0; }

    @Override
    public String toString() { return "Dispatcher: " + queuedPoints.toString(); }
}
