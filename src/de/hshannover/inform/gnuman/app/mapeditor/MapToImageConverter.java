package de.hshannover.inform.gnuman.app.mapeditor;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import javafx.embed.swing.SwingFXUtils;

/**
 * Generate a background image for a map from its data.<br>
 * Uses ImageIO to write the image, which is not optimized for large images a 1.4 gigapixel image @ 25px will take roughly 6GB ram.
 * @author Marc Herschel
 */

public class MapToImageConverter {
    private final static int NUMBER_OF_MAP_SPRITES = 51;
    private final static int COLUMNS = 8;
    private final static int ROWS = 7;
    private int xCell, yCell, dimension;
    private StaticObjects[][] m;
    private LinkedList<SpriteTile> tiles;
    private SpriteTile current;
    private HashMap<Integer, BufferedImage> sprites;
    private BufferedImage result;

    /**
     * Hard coded from sprite sheet. <br>
     * Feel free to change the sprite sheet, but don't change the order.<br>
     * If you really want to don't forget to adjust the numbers!
     * @author myntt Marc Herschel
     */
    private enum MapTile {
        EMPTY_WALKABLE(0),
        EMPTY_VOID(1),
        LINE_HORIZONTAL(2),
        LINE_VERTICAL(4),
        INNER_CORNER(6),
        OUTER_CORNER(10),
        SINGLE_HORIZONTAL(14),
        SINGLE_VERTICAL(17),
        SINGLE_CORNER(20),
        SINGLE_4_CORNER_INTERSECTION(24),
        SINGLE_SINGLE(25),
        N_TO_SINGLE_CONNECTOR(26),
        TWO_TO_ONE_SINGLE_CONNECTOR(30),
        TWO_SINGLE_TO_DOUBLE_CONNECTOR(34),
        CORNER_TO_CORNER_CONNECTOR(38),
        CORNER_TO_CORNER_CONNECTOR_MIRRORED(42),
        OUTER_CORNER_DOUBLE(46),
        SINGLE_GHOSTWALL_CONNECTION(48),
        UNDEFINED(NUMBER_OF_MAP_SPRITES - 1);

        int index;
        MapTile(int i) { index = i; }
    }

    /**
     * Represents a tile and its corresponding sprite.
     * @author Marc Herschel
     */
    private class SpriteTile {
        int x, y;
        int spriteIndex;

        SpriteTile() { this.x = xCell; this.y = yCell; }
        public void update(MapTile tile, int spriteIndex) { this.spriteIndex = spriteIndex + tile.index; }
    }

    /**
     * Construct a MapToImage converter
     * @param mapData data to convert.
     * @param tileset to use.
     * @param dimension of a cell in the final result.
     * @param external true if not loaded from jar.
     * @throws IOException if we can't load the tileset.
     */
    public MapToImageConverter(StaticObjects[][] mapData, String tileset, int dimension, boolean external) throws IOException {
        this.m = mapData;
        this.dimension = dimension;
        this.tiles = new LinkedList<>();
        this.sprites = new HashMap<>();

        Log.info(getClass().getSimpleName(), String.format("Starting conversion for %dx%d map @ %dpx per tile", mapData[0].length, mapData.length, dimension));

        Image img = external ? ImageIO.read(new File(tileset)) : ImageIO.read(getClass().getResourceAsStream(tileset));
        BufferedImage bimage = new BufferedImage(COLUMNS*dimension, ROWS*dimension, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, COLUMNS*dimension, ROWS*dimension, null);
        bGr.dispose();

        Log.info(getClass().getSimpleName(), "Loading sprites...");

        int index = 0, x = 0, y = 0;
        for(int i = 0; i < ROWS; i++) {
            for(int j = 0; j < COLUMNS; j++) {
                if(index >= NUMBER_OF_MAP_SPRITES) { break; }
                sprites.put(index++, bimage.getSubimage(x, y, dimension, dimension));
                x+=dimension;
            }
            x=0;
            y+=dimension;
        }

        Log.info(getClass().getSimpleName(), "Generating tiles...");

        for(yCell = 0; yCell < m.length; yCell++) {
            for(xCell = 0; xCell < m[yCell].length; xCell++) {
                current = new SpriteTile();
                decideTileAndRotation();
                tiles.add(current);
            }
        }

        Log.info(getClass().getSimpleName(), "Rendering result...");

        renderResult();

        Log.info(getClass().getSimpleName(), "Internal representation ready!");
    }

    /**
     * Render the map on an AWT canvas.
     * @param graphics canvas to render on.
     */
    public void renderOnCanvas(Graphics graphics) {
        for(SpriteTile sp : tiles) {
            graphics.drawImage(sprites.get(sp.spriteIndex), sp.x*dimension, sp.y*dimension, dimension, dimension, null);
        }
    }

    /**
     * @return internal BufferedImage as JavaFX compatible image.
     */
    public javafx.scene.image.Image getAsFxImage() {
        javafx.scene.image.WritableImage wimg = new javafx.scene.image.WritableImage(m[0].length*dimension, m.length*dimension);
        return (javafx.scene.image.Image) SwingFXUtils.toFXImage(result, wimg);
    }

    /**
     * Write the output as PNG image.
     * @param filename file to write to.
     * @throws IOException if operation failed.
     */
    public void saveToPNG(String filename) throws IOException {
        Log.info(getClass().getSimpleName(), "Writing png...");
        ImageIO.write(result, "png", new File(filename));
        Log.info(getClass().getSimpleName(), "Written png.");
    }

    /**
     * Render the image once and store it as a class variable.
     */
    private void renderResult() {
        result = new BufferedImage(m[0].length*dimension, m.length*dimension, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();
        for(SpriteTile sp : tiles) {
            g.drawImage(sprites.get(sp.spriteIndex), sp.x*dimension, sp.y*dimension, null);
        }
    }

    /**
     * Decide which sprite to use for the current tile.<p>
     * We have no information about the rotation, we only know that there is either a wall or not.<br>
     * Each of these 8 combinations is a hook that we have to place our wall instruction in.<br>
     * This is really tedious but necessary since we don't have any other information about the map stored.<br>
     * The decision is made in an exclusion process, once something is found we will GTFO of here, there is an order that ensures we don't have to<br>
     * check for every combination, only for the (probably) minimal amount.<br>
     * DON'T EVER TOUCH THIS! IF YOU'VE NOT WRITTEN IT YOU MIGHT NOT UNDERSTAND THE BOOLEAN COMBINATIONS BEHIND IT AND THE ORDER THEY FOLLOW!<br>
     * WHICH WILL RESULT IN YOU BREAKING THE ENTIRE THING! THIS FUNCTIONS IS VERY EVIL, OKAY?<p>
     *
     * TopLeftWall           TopWall           TopRightWall     <br>
     *            \             |             /                 <br>
     * LeftWall------------YOU_ARE_HERE--------RightWall        <br>
     *               /          |             \                 <br>
     * BottomLeftWall       BottomWall         BottomRightWall  <p>
     *
     * GOOD LUCK!
     */
    private void decideTileAndRotation() {
        //WALKABLE
        if(m[yCell][xCell] == StaticObjects.INVISIBLE_PLAYER_WALL || walkable()) { current.update(MapTile.EMPTY_WALKABLE, 0); return; }
        //VOID
        if(bottomLeftWall() && bottomRightWall() && topLeftWall() && topRightWall() && topWall() && bottomWall() && rightWall() && leftWall()) {
            current.update(MapTile.EMPTY_VOID, 0); return;
        }
        //HORIZONTALS
        if(leftWall() && rightWall()) {
            if(!topWall() && bottomWall()) {
                //TWO TO 1 CONNECTORS
                if(!bottomRightWall() && !bottomLeftWall()) { current.update(MapTile.TWO_TO_ONE_SINGLE_CONNECTOR, 1); return; }
                //CORNER TO CORNER CONNECTOR
                if(!bottomRightWall()) { current.update(MapTile.CORNER_TO_CORNER_CONNECTOR, 3); return; }
                //CORNER_TO_CORNER_CONNECTOR_MIRRORED
                if(!bottomLeftWall() && bottomRightWall()) { current.update(MapTile.CORNER_TO_CORNER_CONNECTOR_MIRRORED, 1); return; }
                //NORMAL HORIZONTALS
                current.update(MapTile.LINE_HORIZONTAL, 0); return;
            }
            if(topWall() && !bottomWall()) {
                //TWO TO 1 CONNECTORS
                if(!topRightWall() && !topLeftWall()) { current.update(MapTile.TWO_TO_ONE_SINGLE_CONNECTOR, 0); return; }
                //CORNER TO CORNER CONNECTOR
                if(!topLeftWall() && topRightWall()) { current.update(MapTile.CORNER_TO_CORNER_CONNECTOR, 1); return; }
                //CORNER_TO_CORNER_CONNECTOR_MIRRORED
                if(!topRightWall() && topLeftWall()) { current.update(MapTile.CORNER_TO_CORNER_CONNECTOR_MIRRORED, 3); return; }
                //NORMAL HORIZONTALS
                current.update(MapTile.LINE_HORIZONTAL, 1); return;
            }
            if(topWall() && bottomWall()) {
                //SINGLE CORNER 4 INTERSECTION
                if(!bottomLeftWall() && !bottomRightWall() && !topRightWall() && !topLeftWall()) { current.update(MapTile.SINGLE_4_CORNER_INTERSECTION, 0); return; }
                if(!topRightWall() && !bottomRightWall()) {
                    //TWO_SINGLE_TO_DOUBLE_CONNECTOR
                    if(bottomLeftWall() && !topLeftWall()) { current.update(MapTile.TWO_SINGLE_TO_DOUBLE_CONNECTOR, 2); return; }
                    //N TO 1 CONNECTORS
                    if(bottomLeftWall()) { current.update(MapTile.N_TO_SINGLE_CONNECTOR, 0); return; }
                }
                if(!topLeftWall() && !bottomLeftWall()) {
                    //TWO_SINGLE_TO_DOUBLE_CONNECTOR
                    if(bottomRightWall() && !topRightWall()) { current.update(MapTile.TWO_SINGLE_TO_DOUBLE_CONNECTOR, 1); return; }
                    //N TO 1 CONNECTORS
                    if(bottomRightWall()) { current.update(MapTile.N_TO_SINGLE_CONNECTOR, 1); return; }
                }
            }
        }
        //VERTICALS
        if(topWall() && bottomWall()) {
            if(!rightWall() && leftWall()) {
                //TWO TO 1 CONNECTORS
                if(!topLeftWall() && !bottomLeftWall()) { current.update(MapTile.TWO_TO_ONE_SINGLE_CONNECTOR, 2); return; }
                //CORNER TO CORNER CONNECTOR
                if(!bottomLeftWall()) { current.update(MapTile.CORNER_TO_CORNER_CONNECTOR, 0); return; }
                //CORNER_TO_CORNER_CONNECTOR_MIRRORED
                if(bottomLeftWall() && !topLeftWall()) { current.update(MapTile.CORNER_TO_CORNER_CONNECTOR_MIRRORED, 2); return; }
                //NORMAL VERTICALS
                current.update(MapTile.LINE_VERTICAL, 0); return;
             }
            if(rightWall() && !leftWall()) {
                //TWO TO 1 CONNECTORS
                if(!topRightWall() && !bottomRightWall()) { current.update(MapTile.TWO_TO_ONE_SINGLE_CONNECTOR, 3); return; }
                //CORNER TO CORNER CONNECTOR
                if(!topRightWall()) { current.update(MapTile.CORNER_TO_CORNER_CONNECTOR, 2); return; }
                //CORNER_TO_CORNER_CONNECTOR_MIRRORED
                if(topRightWall() && !bottomRightWall()) { current.update(MapTile.CORNER_TO_CORNER_CONNECTOR_MIRRORED, 0); return; }
                //NORMAL VERTICALS
                current.update(MapTile.LINE_VERTICAL, 1); return;
            }
            if(rightWall() && leftWall()) {
                if(!topRightWall() && !topLeftWall()) {
                    //N TO 1 CONNECTORS
                    current.update(MapTile.N_TO_SINGLE_CONNECTOR, 2); return;
                }
                if(!bottomRightWall() && !bottomLeftWall()) {
                    //TWO_SINGLE_TO_DOUBLE_CONNECTOR
                    if(!topLeftWall() && topRightWall()) { current.update(MapTile.TWO_SINGLE_TO_DOUBLE_CONNECTOR, 0); return; }
                    if(!topRightWall() && topLeftWall()) { current.update(MapTile.TWO_SINGLE_TO_DOUBLE_CONNECTOR, 3); return; }
                    //N TO 1 CONNECTORS
                    current.update(MapTile.N_TO_SINGLE_CONNECTOR, 3); return;
                }
            }
        }
        //SINGLE SINGLE
        if(!leftWall() && !rightWall() && !topWall() && !bottomWall()) {
            current.update(MapTile.SINGLE_SINGLE, 0); return;
        }
        //LOWER INNER CORNERS
        if(topWall() && !bottomWall()) {
            //2xN CORNERS
            if(leftWall() && !rightWall() && topLeftWall()) { current.update(MapTile.INNER_CORNER, 1); return; }
            if(rightWall() && !leftWall() && topRightWall()) { current.update(MapTile.INNER_CORNER, 0); return;}
            //CONNECT SINGLE AND 2 TO 1 CORNERS
            if(!leftWall() && rightWall()) { current.update(MapTile.SINGLE_CORNER, 0); return; }
            if(!rightWall() && leftWall()) { current.update(MapTile.SINGLE_CORNER, 1); return; }
        }
        //UPPER INNER CORNERS
        if(!topWall() && bottomWall()) {
            //2xN CORNERS
            if(leftWall() && !rightWall() && bottomLeftWall()) { current.update(MapTile.INNER_CORNER, 3); return; }
            if(rightWall() && !leftWall() && bottomRightWall()) { current.update(MapTile.INNER_CORNER, 2); return; }
            //CONNECT SINGLE AND 2 TO 1 CORNERS
            if(!leftWall() && rightWall()) { current.update(MapTile.SINGLE_CORNER, 2); return; }
            if(!rightWall() && leftWall()) { current.update(MapTile.SINGLE_CORNER, 3); return; }
        }
        //OUTER CORNERS
        if(topWall() && leftWall() && rightWall() && bottomWall()) {
            //DOUBLE OUTER CORNERS
            if(!topLeftWall() && !bottomRightWall()) { current.update(MapTile.OUTER_CORNER_DOUBLE, 0); return; }
            if(!bottomLeftWall() && !topRightWall()) { current.update(MapTile.OUTER_CORNER_DOUBLE, 1); return; }
            //SINGLE OUTER CORNERS
            if(!bottomRightWall() && bottomLeftWall()) { current.update(MapTile.OUTER_CORNER, 0); return; }
            if(!bottomLeftWall()) { current.update(MapTile.OUTER_CORNER, 1); return; }
            if(topLeftWall()) { current.update(MapTile.OUTER_CORNER, 2); return; }
            if(topRightWall()) { current.update(MapTile.OUTER_CORNER, 3); return; }
        }
        //SINGLE HORIZONTAL
        if(!topWall() && !bottomWall()) {
            if(ghostWall(-1, 0)) { current.update(MapTile.SINGLE_GHOSTWALL_CONNECTION, 0); return; }
            if(ghostWall(1, 0)) { current.update(MapTile.SINGLE_GHOSTWALL_CONNECTION, 1); return; }
            if(!leftWall()) { current.update(MapTile.SINGLE_HORIZONTAL, 0); return; }
            if(!rightWall()) { current.update(MapTile.SINGLE_HORIZONTAL, 2); return; }
            current.update(MapTile.SINGLE_HORIZONTAL, 1); return;
        }
        //SINGLE VERTICAL
        if(!leftWall() && !rightWall()) {
            if(!topWall()) { current.update(MapTile.SINGLE_VERTICAL, 0); return; }
            if(!bottomWall()) { current.update(MapTile.SINGLE_VERTICAL, 2); return; }
            current.update(MapTile.SINGLE_VERTICAL, 1); return;
        }
        //SINGLE CORNERS
        if(!topLeftWall() && !topRightWall() && !bottomRightWall() && !bottomLeftWall()) {
            //LOWER CORNERS
            if(!topWall()) {
                if(!leftWall() && rightWall()) { current.update(MapTile.SINGLE_CORNER, 2); return; }
                if(leftWall() && !rightWall()) { current.update(MapTile.SINGLE_CORNER, 3); return; }
            }
            //UPPER CORNERS
            if(topWall()) {
                if(!leftWall() && rightWall()) { current.update(MapTile.SINGLE_CORNER, 0); return; }
                if(leftWall() && !rightWall()) { current.update(MapTile.SINGLE_CORNER, 1); return; }
            }
        }
        //WHAT THE FUCK?
        current.update(MapTile.UNDEFINED, 0);
    }

    private boolean walkable() {
        return !(m[yCell][xCell] == StaticObjects.WALL || m[yCell][xCell] == StaticObjects.INVISIBLE_PLAYER_WALL);
    }

    private boolean walkable(int x, int y) {
        x+=xCell; y+=yCell;
        if(x < 0 || x >= m[0].length || y < 0 || y >= m.length) { return false; }
        return !(m[y][x] == StaticObjects.WALL || m[y][x] == StaticObjects.INVISIBLE_PLAYER_WALL);
    }

    private boolean ghostWall(int x, int y) {
        x+=xCell; y+=yCell;
        if(x < 0 || x >= m[0].length || y < 0 || y >= m.length) { return false; }
        return m[y][x] == StaticObjects.INVISIBLE_PLAYER_WALL;
    }

    private boolean topWall() { return !walkable(0, -1); }
    private boolean bottomWall() { return !walkable(0, 1); }
    private boolean leftWall() { return !walkable(-1, 0); }
    private boolean rightWall() { return !walkable(1, 0); }
    private boolean topRightWall() { return !walkable(1, -1); }
    private boolean topLeftWall() { return !walkable(-1, -1); }
    private boolean bottomLeftWall() { return !walkable(-1, 1); }
    private boolean bottomRightWall() { return !walkable(1, 1);}
}
