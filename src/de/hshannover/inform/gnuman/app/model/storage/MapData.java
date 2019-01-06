package de.hshannover.inform.gnuman.app.model.storage;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import javafx.scene.image.Image;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;

/**
 * Stores a map.
 * @author Marc Herschel
 */

public class MapData {
    private String name, author;
    private Date creationDate;
    private StaticObjects[][] data;
    private Integer widthInBlocks, heightInBlocks;
    private int pacDots;
    private HashMap<EntityObjects, MapCell> scatterPoints;
    private HashMap<Directions, LinkedList<MapCell>> blocked;
    private boolean[][] playerNavigationData, ghostNavigationData, slowdown;
    private boolean trackHighscore;
    private Image background, backgroundFlash;

    public MapData(boolean trackHighscore) {
        this.name = null;
        this.author = null;
        this.creationDate = null;
        this.widthInBlocks = 0;
        this.heightInBlocks = 0;
        this.ghostNavigationData = null;
        this.playerNavigationData = null;
        this.data = null;
        this.scatterPoints = new HashMap<>();
        this.blocked = new HashMap<>();
        blocked.put(Directions.DOWN, new LinkedList<>());
        blocked.put(Directions.UP, new LinkedList<>());
        blocked.put(Directions.LEFT, new LinkedList<>());
        blocked.put(Directions.RIGHT, new LinkedList<>());
        this.trackHighscore = trackHighscore;
        this.background = null;
        this.backgroundFlash = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate() {
        this.creationDate = new Date();
    }

    public void setCreationDate(String creationDate) throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        this.creationDate = parser.parse(creationDate);
    }

    public void setMapData(char c, int x, int y) {
        setMapData(StaticObjects.values()[Character.getNumericValue(c)], x, y);
    }

    public void setMapData(StaticObjects object, int x, int y) {
        nullCheck();
        if(object == StaticObjects.FOOD) { pacDots++; }
        data[y][x] = object;
        ghostNavigationData[y][x] = (object != StaticObjects.WALL);
        playerNavigationData[y][x] = !(object == StaticObjects.WALL || object == StaticObjects.INVISIBLE_PLAYER_WALL);
    }

    public StaticObjects[][] getData() {
        return data;
    }

    public Integer getWidthInBlocks() {
        return widthInBlocks;
    }

    public void setWidthInBlocks(int widthInBlocks) {
        this.widthInBlocks = widthInBlocks;
    }

    public Integer getHeightInBlocks() {
        return heightInBlocks;
    }

    public void setHeightInBlocks(int heightInBlocks) {
        this.heightInBlocks = heightInBlocks;
    }

    public boolean[][] getGhostNavigationData() {
        return ghostNavigationData;
    }

    public boolean[][] getPlayerNavigationData() {
        return playerNavigationData;
    }

    public boolean trackHighscore() {
        return trackHighscore;
    }

    public Image getBackground() {
        return background;
    }

    public Image getBackgroundFlash() {
        return backgroundFlash;
    }

    public void setBackground(Image image) {
        this.background = image;
    }

    public void setBackground(InputStream inputStream) throws IOException {
        background = new Image(inputStream);
        inputStream.close();
    }

    public void setBackgroundFlash(Image image) {
        this.backgroundFlash = image;
    }

    public void setBackgroundFlash(InputStream inputStream) throws IOException {
        backgroundFlash = new Image(inputStream);
        inputStream.close();
    }

    public void setScatterData(HashMap<EntityObjects, MapCell> scatter) {
        scatterPoints = scatter;
    }

    public void addScatterPoint(EntityObjects entityType, int cellX, int cellY) {
        scatterPoints.put(entityType, new MapCell(cellX, cellY));
    }

    public MapCell getScatterPoint(EntityObjects entityType) {
        return scatterPoints.get(entityType);
    }

    public HashMap<EntityObjects, MapCell> getScatterPoints() {
        return scatterPoints;
    }

    public int getPacDots() {
        return pacDots;
    }

    public void addBlocked(Directions d, int x, int y) {
        blocked.get(d).add(new MapCell(x, y));
    }

    public HashMap<Directions, LinkedList<MapCell>> getBlocked() {
        return blocked;
    }

    public void initiateEmptySlowdown() {
        slowdown = new boolean[getWidthInBlocks()][getHeightInBlocks()];
    }

    public void addSlowdown(int x, int y) {
        if(slowdown == null) { slowdown = new boolean[getWidthInBlocks()][getHeightInBlocks()]; }
        try {
            slowdown[x][y] = true;
        } catch(ArrayIndexOutOfBoundsException e) {
            Log.critical(getClass().getSimpleName(), "Failed to add slowdown field at: X: " + x + " Y:" + y);
        }
    }

    public boolean[][] getSlowdown() {
        return slowdown;
    }

    private void nullCheck() {
        if(ghostNavigationData == null || playerNavigationData == null || data == null) {
            ghostNavigationData = new boolean[heightInBlocks][widthInBlocks];
            playerNavigationData = new boolean[heightInBlocks][widthInBlocks];
            data = new StaticObjects[heightInBlocks][widthInBlocks];
        }
    }

    @Override
    public String toString() {
        return "MapData [name=" + name + ", author=" + author + ", creationDate=" + creationDate + ", widthInBlocks=" + widthInBlocks + ", heightInBlocks=" + heightInBlocks + "]";
    }
}
