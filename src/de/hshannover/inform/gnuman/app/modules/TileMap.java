package de.hshannover.inform.gnuman.app.modules;

import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.model.GhostWall;
import de.hshannover.inform.gnuman.app.model.Player;
import de.hshannover.inform.gnuman.app.model.Static;
import de.hshannover.inform.gnuman.app.model.chunks.ChunkCoordinator;
import de.hshannover.inform.gnuman.app.model.coordination.IntersectionLookUpTable;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.MapCell;
import de.hshannover.inform.gnuman.app.model.storage.MapData;
import de.hshannover.inform.gnuman.app.util.Helper;
/**
 * Represents a map.
 * @author Marc Herschel
 */

public class TileMap {
    private DynamicVariables dyn;
    private List<GhostWall> ghostWalls;
    private List<Point> ghostSpawns, ghostBases, bonusItemSpawns;
    private Point ghostHouseFrontSpawn;
    private Point playerSpawn;
    private StaticObjects[][] mapObjectData;
    private IntersectionLookUpTable intersectionLookup;
    private ChunkCoordinator c;
    private Static bonusItem;
    private boolean bonusItemSpawned;
    private int pacDots, timesBonusItemSpawned;

    /**
     * Construct a tile map.
     * @param dyn Dynamic user options depending values.
     */
    public TileMap(DynamicVariables dyn) {
        this.dyn = dyn;
        this.ghostBases = new LinkedList<>();
        this.ghostSpawns = new LinkedList<>();
        this.ghostWalls = new LinkedList<>();
        this.intersectionLookup = new IntersectionLookUpTable();
        this.bonusItemSpawns = new LinkedList<>();
    }

    /**
     * Unloads all components so you can load a new map.
     */
    public void unloadMap() {
        intersectionLookup.clear();
        ghostSpawns.clear();
        ghostBases.clear();
        ghostWalls.clear();
        bonusItemSpawns.clear();
        c = null;
        playerSpawn = null;
        mapObjectData = null;
        bonusItem = null;
        bonusItemSpawned = false;
        pacDots = 0;
    }

    /**
     * Resets the map to the initial state.
     * @param keepItemsOnMap true if you don't want to reset the items on the map.
     */
    public void resetMap(boolean keepItemsOnMap) {
        if(!keepItemsOnMap) {
            c.reset();
            bonusItemSpawned = false;
            bonusItem = null;
            timesBonusItemSpawned = 0;
        }
        Log.info(getClass().getSimpleName(), "Reset map to initial state.");
    }

    /**
     * Loads a map from the parsed data.
     * @param data data of map to load.
     */
    public void loadMap (MapData data) {
        c = new ChunkCoordinator(10, data.getWidthInBlocks(), data.getHeightInBlocks(), dyn.getBlockWidth());
        mapObjectData = data.getData();
        int x = 0, y = 0;
        Log.info(getClass().getSimpleName(), "Building Map.");
        for(StaticObjects[] row : mapObjectData) {
            for(StaticObjects block : row) {
                if(block != StaticObjects.EMPTY) { processBlock(block, x, y); }
                x+=dyn.getBlockWidth();
            }
            x=0;
            y+=dyn.getBlockHeight();
        }
        ghostSpawns.add(ghostHouseFrontSpawn);
        ghostSpawns.addAll(ghostBases);
        buildIntersectionLookupTable(data.getGhostNavigationData(), data.getBlocked());
        Log.info(getClass().getSimpleName(), "Map built.");
    }

    /**
     * Spawn an bonus item.
     */
    public void spawnBonusItem() {
        if(bonusItemSpawns.size() == 0) { return; }
        Collections.shuffle(bonusItemSpawns);
        bonusItem = new Static(bonusItemSpawns.get(0).getX(), bonusItemSpawns.get(0).getY(), dyn.getItemWidth(), dyn.getItemHeight(), StaticObjects.BONUS_ITEM);
        bonusItemSpawned = true;
        timesBonusItemSpawned++;
    }

    /**
     * Remove an bonus item.
     */
    public void removeBonusItem() {
        bonusItem = null;
        bonusItemSpawned = false;
    }

    /**
     * Places a Static in the correct list.
     * @param blocktype Object to be placed.
     * @param x x-Coordinate to place block at.
     * @param y y-Coordinate to place block at.
     */
    private void processBlock(StaticObjects blocktype, int x, int y) {
        if(blocktype == StaticObjects.EMPTY) { return; }

        int w = 0, h = 0;

        switch(blocktype) {
            case FOOD: w = dyn.getItemFoodWidth(); h = dyn.getItemFoodHeight(); pacDots++; break;
            case POWERUP: case BONUS_ITEM: w = dyn.getItemWidth(); h = dyn.getItemHeight(); break;
            default: w = dyn.getBlockWidth(); h = dyn.getBlockHeight(); break;
        }

        Point interpolatedCoordinates = Helper.interpolate(x, y, w, h, dyn);

        switch(blocktype) {
            case PLAYER_SPAWN: playerSpawn = Helper.interpolate(x, y, dyn.getEntityHitboxWidth() , dyn.getEntityHitboxHeight(), dyn); break;
            case GHOST_SPAWN: ghostHouseFrontSpawn = Helper.interpolate(x, y, dyn.getEntityHitboxWidth() , dyn.getEntityHitboxHeight(), dyn); break;
            case GHOST_HOUSE_SPAWN: ghostBases.add(Helper.interpolate(x, y, dyn.getEntityHitboxWidth() , dyn.getEntityHitboxHeight(), dyn)); break;
            case FOOD: case POWERUP:
                c.addItem(new Static(interpolatedCoordinates.getX(), interpolatedCoordinates.getY(), w, h, blocktype));
                break;
            case INVISIBLE_PLAYER_WALL:
                ghostWalls.add(new GhostWall(interpolatedCoordinates.getX(), interpolatedCoordinates.getY(), w, h, blocktype, Helper.evaluateAlignment(interpolatedCoordinates, mapObjectData, w, h)));
                break;
            case BONUS_ITEM:
                bonusItemSpawns.add(interpolatedCoordinates);
            default: return;
        }

        Log.debug(getClass().getSimpleName(), String.format("Added: %s at [X: %d | Y: %d]", blocktype, (int)interpolatedCoordinates.getX(), (int) interpolatedCoordinates.getY()));
    }

    /**
     * Populate the intersection lookup table with data.<br>
     * Intersections cannot exist at the border of the map.
     * @param ghostPathData pathdata of ghosts
     * @param blockedPaths direction, cells map that restricts ghosts movement
     */
    private void buildIntersectionLookupTable(boolean[][] ghostPathData, HashMap<Directions, LinkedList<MapCell>> blockedPaths) {
        intersectionLookup.updateGhostWalls(ghostWalls, dyn);
        for(int cellY = 1; cellY < mapObjectData.length - 1; cellY ++) {
            for(int cellX = 1; cellX < mapObjectData[0].length - 1; cellX++) {
            //Not walkable? Fuck off!
                if(!ghostPathData[cellY][cellX]) { continue; }
            //Not an intersection? Continue.
                if((ghostPathData[cellY][cellX+1] && ghostPathData[cellY][cellX-1] && !(ghostPathData[cellY+1][cellX] || ghostPathData[cellY-1][cellX]))
                || (ghostPathData[cellY+1][cellX] && ghostPathData[cellY-1][cellX] && !(ghostPathData[cellY][cellX+1] || ghostPathData[cellY][cellX-1]))) {
                    continue;
                }

                boolean upwardsPossible = true;
                for(MapCell c : blockedPaths.get(Directions.UP)) { if(c.getCellX() == cellX && c.getCellY() == cellY) { upwardsPossible = false; break; } }
                boolean downwardsPossible = true;
                for(MapCell c : blockedPaths.get(Directions.DOWN)) { if(c.getCellX() == cellX && c.getCellY() == cellY) { downwardsPossible = false; break; } }
                boolean rightwardsPossible = true;
                for(MapCell c : blockedPaths.get(Directions.RIGHT)) { if(c.getCellX() == cellX && c.getCellY() == cellY) { rightwardsPossible = false; break; } }
                boolean leftwardsPossible = true;
                for(MapCell c : blockedPaths.get(Directions.LEFT)) { if(c.getCellX() == cellX && c.getCellY() == cellY) { leftwardsPossible = false; break; } }

            //Assign data via boolean operation
                LinkedList<Directions> temp = new LinkedList<>();
                if(ghostPathData[cellY][cellX+1] && rightwardsPossible) { temp.add(Directions.RIGHT); }
                if(ghostPathData[cellY-1][cellX] && upwardsPossible) { temp.add(Directions.UP); }
                if(ghostPathData[cellY][cellX-1] && leftwardsPossible) { temp.add(Directions.LEFT); }
                if(ghostPathData[cellY+1][cellX] && downwardsPossible) { temp.add(Directions.DOWN); }
                intersectionLookup.add(cellX, cellY, temp);
            }
        }
    }

    public List<Point> getGhostSpawns() {
        return ghostSpawns;
    }

    public List<Point> getGhostBases() {
        return ghostBases;
    }

    public List<GhostWall> getGhostWalls() {
        return ghostWalls;
    }

    public Point getPlayerSpawn() {
        return playerSpawn;
    }

    public StaticObjects[][] getMapObjectData() {
        return mapObjectData;
    }

    public IntersectionLookUpTable getIntersectionLookUpTable() {
        return intersectionLookup;
    }

    public MapCell getGhostHouseFrontSpawn() {
        return new MapCell((int) ghostHouseFrontSpawn.getX() / dyn.getBlockWidth(), (int) ghostHouseFrontSpawn.getY() / dyn.getBlockHeight());
    }

    public int allPacDots() {
        return pacDots;
    }

    public boolean bonusItemSpawned() {
        return bonusItemSpawned;
    }

    public boolean bonusItemSpawnable() {
        return bonusItemSpawns.size() > 0 && timesBonusItemSpawned < 2 && !bonusItemSpawned;
    }

    public Static getBonusItem() {
        return bonusItem;
    }

    public int numberOfItems() {
        return c.getExistingItems();
    }

    public StaticObjects checkItemIntersection(Player player) {
        return c.checkForCollision(player);
    }

    public ChunkCoordinator getChunkCoordinator() {
        return c;
    }
}
