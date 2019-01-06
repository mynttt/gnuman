package de.hshannover.inform.gnuman.app.model.coordination;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.model.GhostWall;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.MapCell;

/**
 * Lookup table for intersections used by the ghosts.
 * @author Marc Herschel
 */

public class IntersectionLookUpTable {
    private HashMap<Integer, HashMap<Integer, IntersectionTile>> intersections;
    private List<MapCell> ghostWalls;

    /**
     * Construct a lookup table for intersections.
     */
    public IntersectionLookUpTable() {
        this.intersections = new HashMap<>();
        this.ghostWalls = new LinkedList<>();
    }

    /**
     * Set ghost walls in the map and convert them to a useful format.
     * @param ghostWalls in map
     * @param dyn user setting dependent variables.
     */
    public void updateGhostWalls(List<GhostWall> ghostWalls, DynamicVariables dyn) {
        for(GhostWall w : ghostWalls) {
            this.ghostWalls.add(new MapCell((int) w.getX() / dyn.getBlockWidth(), (int) w.getY() / dyn.getBlockHeight()));
        }
    }

    /**
     * Add an intersection
     * @param x x cell
     * @param y y cell
     * @param directions possible directions
     */
    public void add(int x, int y, List<Directions> directions) {
        if(!intersections.containsKey(x)) { intersections.put(x, new HashMap<>()); }
        intersections.get(x).put(y, new IntersectionTile(x, y, directions, ghostWalls));
    }

    /**
     * @param x cell of intersection
     * @param y cell of intersection
     * @return intersection at that point or null if not exists
     */
    public IntersectionTile get(int x, int y) {
        return intersections.get(x).get(y);
    }

    /**
     * Clear for reuse.
     */
    public void clear() { intersections.clear(); ghostWalls.clear(); }

    @Override
    public String toString() {
        TreeMap<Integer, TreeMap<Integer, IntersectionTile>> temp = new TreeMap<>();
        StringBuilder sb = new StringBuilder();
        for(Integer x : intersections.keySet()) {
            temp.put(x, new TreeMap<>());
            for(Integer y : intersections.get(x).keySet()) {
                temp.get(x).put(y, intersections.get(x).get(y));
            }
        }
        sb.append("-------------------------------------------\n");
        sb.append("Intersection Lookup Table:\n");
        for(Integer x : temp.keySet()) {
            for(Integer y : temp.get(x).keySet()) {
                sb.append("X:-> " + x + " Y:-> " + y + " Directions: " + temp.get(x).get(y).toString());
                sb.append('\n');
            }
        }
        sb.append("-------------------------------------------");
        return sb.toString();
    }
}
