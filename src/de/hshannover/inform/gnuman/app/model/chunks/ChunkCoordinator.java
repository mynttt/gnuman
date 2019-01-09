package de.hshannover.inform.gnuman.app.model.chunks;

import java.util.HashMap;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.model.AbstractEntity;
import de.hshannover.inform.gnuman.app.model.Static;

/**
 * We need to chunk items for rendering and collision check otherwise this game runs at 2FPS.<br>
 * large.gnuman without chunks and O(N) for collision check and O(N) draw image operations , N -> amount of items on the map MEANS 2FPS then crash.<br>
 * With chunks and O(1) for collision and c*O(N) draw image operations, c -> amount of visible chunks, N -> amount of items in chunk it runs smoothly on large.gnuman.
 * @author Marc Herschel
 */

public class ChunkCoordinator {
    private HashMap<Integer, HashMap<Integer, ItemChunk>> chunks;
    private HashMap<Integer, HashMap<Integer, Static>> outOfBounds;
    private int dimension, blockDimension, allItems, existingItems, maxX, maxY;

    /**
     * Construct a chunk coordinator with the default size of 35 blocks per chunk.
     * @param blocksHorizontal amount of horizontal blocks on the map.
     * @param blocksVertical amount of vertical blocks on the map.
     * @param blockDimension dimension of a block.
     */
    public ChunkCoordinator(int blocksHorizontal, int blocksVertical, int blockDimension) { this(35, blocksVertical, blocksHorizontal, blockDimension); }

    /**
     * Construct a chunk coordinator with a custom chunk size.
     * @param dimension width/height of this chunk in blocks.
     * @param blocksHorizontal amount of horizontal blocks on the map.
     * @param blocksVertical amount of vertical blocks on the map.
     * @param blockDimension dimension of a block.
     */
    public ChunkCoordinator(int dimension, int blocksHorizontal, int blocksVertical, int blockDimension) {
        chunks = new HashMap<>();
        outOfBounds = new HashMap<>();
        this.dimension = dimension;
        this.blockDimension = blockDimension;
        for(int y = 0; y<=blocksVertical/dimension+1; y++, maxY++) {
            chunks.put(y, new HashMap<>());
            for(int x = 0; x<=blocksHorizontal/dimension+1; x++) {
                if(x > maxX) maxX = x;
                chunks.get(y).putIfAbsent(x, new ItemChunk(blockDimension, y*dimension, dimension, this));
            }
        }
    }

    /**
     * Add an item to a chunk management system.
     * @param s static to add.
     */
    public void addItem(Static s) {
        chunks.get(((int) s.getY() / blockDimension) / dimension).get(((int) s.getX() / blockDimension) / dimension).addItem(s);
        existingItems++; allItems++;
    }

    /**
     * Find correct chunk and return collision query.
     * @param e entity to check for.
     * @return object at the chunk
     */
    public StaticObjects checkForCollision(AbstractEntity e) {
        return chunks.get(e.floorCellY() / dimension).get(e.floorCellX() / dimension).checkForCollision(e);
    }

    /**
     * @return dimension of this chunk
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Reset this chunk to its initial state after adding all items.
     */
     public void reset() {
        chunks.values().stream().flatMap(chunk -> chunk.values().stream()).forEach(ItemChunk::reset);
        existingItems = allItems;
    }

     /**
      * @return count of items in all chunks.
      */
    public int getExistingItems() {
        return existingItems;
    }

    /**
     * Return a region that matches the coordinates.<br>
     * We're talking about chunk coordinates here so 1 = One chunk.<br>
     * Calculate it via blocks / (chunk)dimension.
     * @param xc x coordinate of the chunk.
     * @param yc y coordinate of the chunk.
     * @return the region or an empty dummy list so you don't have to check for null.
     */
    public HashMap<Integer, HashMap<Integer, Static>> getRegion(int xc, int yc) {
        if(xc < 0 || yc < 0 || xc >= maxX || yc >= maxY) { return outOfBounds; }
        return chunks.get(yc).get(xc).getItemsToRender();
    }

    /**
     * Decrease item counter.
     */
    void decreaseItems() {
        existingItems--;
    }
}
