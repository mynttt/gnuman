package de.hshannover.inform.gnuman.app.model.chunks;

import java.util.HashMap;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.model.AbstractEntity;
import de.hshannover.inform.gnuman.app.model.Static;

/**
 * Item chunk holding items.
 * @author Marc Herschel
 */
public class ItemChunk {
    private ChunkCoordinator c;
    private HashMap<Integer, HashMap<Integer, Static>> allExistingItems, currentlyExistingItems;
    private int blockdimension;

    /**
     * Construct a new item chunk.
     * @param blockdimension dimension of blocks.
     * @param fromY starting from this y coordinate.
     * @param dimension of the chunk.
     * @param c reference to coordinator to count items once removed.
     */
    public ItemChunk(int blockdimension, int fromY, int dimension, ChunkCoordinator c) {
        allExistingItems = new HashMap<>();
        currentlyExistingItems = new HashMap<>();
        this.blockdimension = blockdimension;
        this.c = c;
        for(int y = fromY == 0 ? -1 : fromY; y <= fromY+dimension; y++) {
            allExistingItems.put(y, new HashMap<>());
            currentlyExistingItems.put(y, new HashMap<>());
        }
    }

    /**
     * Add a new item to the chunk.
     * @param s static to add.
     */
    public void addItem(Static s) {
        allExistingItems.putIfAbsent((int) s.getY() / blockdimension, new HashMap<>());
        currentlyExistingItems.putIfAbsent((int) s.getY() / blockdimension, new HashMap<>());
        allExistingItems.get((int) s.getY() / blockdimension).put((int) s.getX() / blockdimension, s);
        currentlyExistingItems.get((int) s.getY() / blockdimension).put((int) s.getX() / blockdimension, s);
    }

    /**
     * Check for collision with this chunk. If collision the item will be removed from the map.
     * @param e entity to check for.
     * @return null if no collision, else the type of item.
     */
    public StaticObjects checkForCollision(AbstractEntity e) {
        Static s = currentlyExistingItems.get(e.centerCellY()).get(e.centerCellX());
        if(s == null) { return null; }
        if(s.intersects(e.getBounds())) {
            currentlyExistingItems.get(e.centerCellY()).remove(e.centerCellX());
            c.decreaseItems(); }
        else {
            return null;
        }
        return s.getBlockType();
    }

    /**
     * @return representation of currently available item in this chunk.
     */
    public HashMap<Integer, HashMap<Integer, Static>> getItemsToRender() { return currentlyExistingItems; }

    /**
     * Reset the chunk to its initial build state.
     */
    void reset() {
        currentlyExistingItems.values().forEach(HashMap::clear);
        allExistingItems.forEach((k, v) -> {
            v.forEach((kk, vv) -> {
                currentlyExistingItems.get(k).put(kk, vv);
            });
        });
    }
}
