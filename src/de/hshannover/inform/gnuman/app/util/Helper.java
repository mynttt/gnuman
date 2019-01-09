package de.hshannover.inform.gnuman.app.util;

import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;

/**
 * Helper Functions
 * @author Marc Herschel
 */

public class Helper {

    /**
     * Shutdown the logger and kill everything running if something goes very wrong.
     */
    public static void exitOnCritical() {
        System.out.println("Critical exception occurred. Check the log file for more info or enable logging if disabled.");
        Log.critical("Helper", "Aborting execution duo a critical error.");
        Log.logger().suspend();
        System.exit(1);
    }

    /**
     * Gets all nodes of a JavaFX root.
     * @param root to search through
     * @return List of nodes.
     */
    public static List<Node> getAllNodes(Parent root) {
        LinkedList<Node> nodes = new LinkedList<>();
        addAllDescendants(root, nodes);
        return nodes;
    }

    /**
     * Helper to create a list of all nodes.
     * @param parent parent to search for.
     * @param nodes list to add to.
     */
    private static void addAllDescendants(Parent parent, LinkedList<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent)
                addAllDescendants((Parent)node, nodes);
        }
    }

    /**
     * Convert exception to stack trace string.
     * @param e Exception
     * @return Formatted stack trace.
     */
    public static String stackTraceToString(Exception e) {
        return "\nCause: " + e + "\n" + Arrays.stream(e.getStackTrace()).map(Objects::toString).collect(Collectors.joining("\n"));
    }

    /**
     * Remove UTF characters from a string.
     * @param s String to sanitize
     * @return String with only ASCII chars.
     */
    public static String onlyAscii(String s) { return s.replaceAll("[^ -~]", ""); }

    /**
     * Checks if a String is an Integer
     * @param s String to check
     * @return true if integer
     */
    public static boolean isInteger(String s) { return s.matches("^[+-]?\\d+$"); }

    /**
     * Interpolates the coordinates of an object in case the texture size differs with the block size to place it in the middle of a cell.
     * @param x x-Coordinate to place.
     * @param y y-Coordinate to place.
     * @param width width of object.
     * @param height height of object.
     * @param dyn dynamic variable object.
     * @return A point containing the two interpolated values.
     */
    public static Point interpolate(int x, int y, int width, int height, DynamicVariables dyn) {
        if(width != dyn.getBlockWidth()) { x += (dyn.getBlockWidth() - width) / 2.0; }
        if(height != dyn.getBlockHeight()) { y += (dyn.getBlockHeight() - height) / 2.0; }
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    /**
     * Calculates the largest possible block dimension for the default map.
     * @param width of the screen.
     * @param height of the screen.
     * @return maximum possible block size that works with the default map.
     */
    public static int calculateMaxBlockDimensionForDefaultMap(int width, int height) {
        return calculateMaxBlockDimensionsForCustomMap(width, height, Constants.DEFAULT_MAP_WIDTH, Constants.DEFAULT_MAP_HEIGHT);
    }

    /**
     * Calculates the largest possible block dimension for a map.
     * @param width of the screen.
     * @param height of the screen.
     * @param mapWidth width of map in blocks.
     * @param mapHeight height of map in blocks.
     * @return maximum possible block size that works with the default map.
     */
    public static int calculateMaxBlockDimensionsForCustomMap(int width, int height, int mapWidth, int mapHeight) {
        //Iteration 1: Get closest without UI
        int tmp = Integer.min((int) Math.floor(width/mapWidth), (int) Math.floor(height/mapHeight));
        //Iteration 2: Get closest with UI, leave 4 blocks space for task bar and other stuff.
        return Integer.min((int) Math.floor(width-4*tmp/mapWidth), (int) Math.floor(height-(tmp*1.2)-4*tmp)/mapHeight);
    }

    /**
     * Determine if a ghost wall block should be displayed vertical or horizontal.
     * @param coordinates coordinates of block
     * @param mapData data of map
     * @param blockWidth width of block
     * @param blockHeight height of block
     * @return 1 if horizontal, 0 if impossible to determine, -1 if vertical
     */
    public static int evaluateAlignment(Point coordinates, StaticObjects[][] mapData, int blockWidth, int blockHeight) {
        int xCell = (int) coordinates.getX() / blockWidth;
        int yCell = (int) coordinates.getY() / blockHeight;
        if(mapData[yCell-1][xCell] == StaticObjects.INVISIBLE_PLAYER_WALL || mapData[yCell+1][xCell] == StaticObjects.INVISIBLE_PLAYER_WALL) { return -1; }
        if(mapData[yCell][xCell-1] == StaticObjects.INVISIBLE_PLAYER_WALL || mapData[yCell][xCell+1] == StaticObjects.INVISIBLE_PLAYER_WALL) { return 1; }
        return 0;
    }

    /**
     * Euclidean Distance
     * @param x1 first x
     * @param y1 first y
     * @param x2 second x
     * @param y2 second y
     * @return euclidean distance
     */
    public static double euclideanDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
    }

    /**
     * Scale a JavaFX image.
     * @param source of the image
     * @param targetWidth of the image
     * @param targetHeight target height of the image
     * @param preserveRatio true if preserve ratio
     * @param smooth true if apply AA to the image
     * @return scaled image
     */
    public static Image scale(Image source, int targetWidth, int targetHeight, boolean preserveRatio, boolean smooth) {
        ImageView imageView = new ImageView(source);
        imageView.setPreserveRatio(preserveRatio);
        imageView.setSmooth(smooth);
        imageView.setFitWidth(targetWidth);
        imageView.setFitHeight(targetHeight);
        return imageView.snapshot(null, null);
    }
}
