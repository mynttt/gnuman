package de.hshannover.inform.gnuman.app.mapeditor;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.mapeditor.Enums.BackgroundRenderStates;
import de.hshannover.inform.gnuman.app.mapeditor.Enums.TwoPointsSelectionStates;
import de.hshannover.inform.gnuman.app.mapeditor.Enums.Tools;
import de.hshannover.inform.gnuman.app.model.storage.MapCell;
import de.hshannover.inform.gnuman.app.model.storage.MapData;
import de.hshannover.inform.gnuman.app.model.storage.PathNode;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;

public class Helper {
    private static final Font f = new Font(20);

    static void redraw(MapData currentMap, Canvas canvas, Pane canvasPane, ViewPort viewPort, State state, Image[] textures, int scale, List<PathNode> pathList) {
        GraphicsContext g = canvas.getGraphicsContext2D();

        g.setFill(Color.GRAY);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if(currentMap == null) { return; }

        int vY = viewPort.cellY(scale), vX = viewPort.cellX(scale);

        int dataYFrom = vY,
            dataXFrom = vX,
            dataYTo = (int) Math.ceil(canvasPane.getHeight()/scale)+vY <= currentMap.getHeightInBlocks() ? (int) Math.ceil(canvasPane.getHeight()/scale)+vY : currentMap.getHeightInBlocks(),
            dataXTo = (int) Math.ceil(canvasPane.getWidth()/scale)+vX <= currentMap.getWidthInBlocks() ? (int) Math.ceil(canvasPane.getWidth()/scale)+vX : currentMap.getWidthInBlocks();
            if(dataYTo+1 < currentMap.getHeightInBlocks()) { dataYTo++; }
            if(dataXTo+1 < currentMap.getWidthInBlocks()) { dataXTo++; }

        boolean renderMapAsBlocks = state.renderSettings.backgroundState == BackgroundRenderStates.BLOCKS_COLOR
                || (state.renderSettings.backgroundState == BackgroundRenderStates.FLASHMAP && currentMap.getBackgroundFlash() == null)
                || (state.renderSettings.backgroundState == BackgroundRenderStates.BACKGROUND && currentMap.getBackground() == null);

        double skipFromFirstX = Math.abs((viewPort.x < scale ? scale-viewPort.x : Math.abs((viewPort.x-scale)-dataXFrom*scale))-scale);
        double skipFromFirstY = Math.abs((viewPort.y < scale ? scale-viewPort.y : Math.abs((viewPort.y-scale)-dataYFrom*scale))-scale);
        double xSkipWidth = scale - skipFromFirstX;
        double ySkipHeight = scale -skipFromFirstY;

        BiFunction<Integer, Integer, Double> xFrom =  (x, rx) -> x != 0 ? (rx*scale)-skipFromFirstX : 0;
        BiFunction<Integer, Integer, Double> yFrom = (y, ry) -> y != 0 ? (ry*scale)-skipFromFirstY : 0;
        Function<Integer, Double> blockWidth = x -> x == 0 ? xSkipWidth : scale;
        Function<Integer, Double> blockHeight = y -> y == 0 ? ySkipHeight : scale;

        /*Layer 1 Map as Image */
        if(!renderMapAsBlocks) {
            Image i = state.renderSettings.backgroundState == BackgroundRenderStates.BACKGROUND ? currentMap.getBackground() : currentMap.getBackgroundFlash();
            double imageScaleW = i.getWidth() / currentMap.getWidthInBlocks();
            double imageScaleH = i.getHeight() / currentMap.getHeightInBlocks();
            double relationBetweenScaleAndImageScaleW = imageScaleW  / scale;
            double relationBetweenScaleAndImageScaleH = imageScaleH / scale;
            g.drawImage(i,
                    dataXFrom*imageScaleW+skipFromFirstX*relationBetweenScaleAndImageScaleW,
                    dataYFrom*imageScaleH+skipFromFirstY*relationBetweenScaleAndImageScaleH,
                    (dataXTo-dataXFrom)*imageScaleW,
                    (dataYTo-dataYFrom)*imageScaleH,
                    0,
                    0,
                    (dataXTo-dataXFrom)*scale,
                    (dataYTo-dataYFrom)*scale);
        }

        /*Shared Rendering for Layer 1, 1.5*/
        for(int y = dataYFrom, ry = 0; y < dataYTo; y++, ry++) {
            for(int x = dataXFrom, rx = 0; x < dataXTo; x++, rx++) {
                if(renderMapAsBlocks) {
                    /*Layer 1 Map as Blocks */
                    g.setFill(Constants.EDITOR_BACKGROUND_COLOR[currentMap.getData()[y][x].ordinal()]);
                    g.fillRect(xFrom.apply(x, rx), yFrom.apply(y, ry), blockWidth.apply(x), blockHeight.apply(y));
                } else {
                    /*Layer 1.5 Ghostwalls if rendering via background image*/
                    if(currentMap.getData()[y][x] == StaticObjects.INVISIBLE_PLAYER_WALL) {
                        g.drawImage(textures[2], xFrom.apply(x, rx), yFrom.apply(y, ry), scale, scale);
                    }
                }
            }
        }

        /*Shared Rendering for Layer 2, 3*/
        for(int y = dataYFrom, ry = 0; y < dataYTo; y++, ry++) {
            for(int x = dataXFrom, rx = 0; x < dataXTo; x++, rx++) {
                double pX = xFrom.apply(x, rx), pY = yFrom.apply(y, ry);
                /*Layer 2 Items */
                if(state.renderSettings.withItems) {
                    if(currentMap.getData()[y][x] == StaticObjects.FOOD) {
                        g.drawImage(textures[0], pX+scale*0.25, pY+scale*0.25, scale*0.5, scale*0.5);
                        }
                    if(currentMap.getData()[y][x] == StaticObjects.POWERUP) {
                        g.drawImage(textures[1], pX+scale*0.1, pY+scale*0.1, scale*0.8, scale*0.8);
                    }
                }
                /*Layer 3 Slowdown Modifier */
                if(state.renderSettings.withModifiers) {
                    g.setFill(Color.BLUE);
                    if(currentMap.getSlowdown()[x][y]) {
                        g.fillPolygon(new double[] { pX+scale/4, pX+scale/2, pX + (scale-scale/4) }, new double[] { pY+scale/4, pY + (scale-scale/4), pY+scale/4 }, 3);
                        g.fillPolygon(new double[] { pX+scale/4, pX+scale/2, pX + (scale-scale/4) }, new double[] { pY+(scale-scale/4), pY+scale/4 , pY+(scale-scale/4) }, 3);
                    }
                }
            }
        }

        /*Layer 3 Up Blocked Modifier*/
        if(state.renderSettings.withModifiers) {
            g.setFill(Color.DARKRED);
            for(Directions d : currentMap.getBlocked().keySet()) {
                for(MapCell c : currentMap.getBlocked().get(d)) {
                    if(c.getCellX() >= dataXFrom && c.getCellX() <= dataXTo && c.getCellY() >= dataYFrom && c.getCellY() <= dataYTo) {
                        double pX = xFrom.apply(c.getCellX(), c.getCellX()-vX), pY = yFrom.apply(c.getCellY(), c.getCellY()-vY);
                        switch(d) {
                        case DOWN:
                            g.fillPolygon(new double[] { pX+scale/8, pX+scale/2, pX+(scale-scale/8) }, new double[] { pY+(scale-scale/6), pY+scale, pY+(scale-scale/6) }, 3);
                            break;
                        case LEFT:
                            g.fillPolygon(new double[] { 1+pX+scale/8, pX+1, 1+pX+scale/8 }, new double[] { pY+scale/6 , pY+scale / 2, pY+(scale-scale/6) }, 3);
                            break;
                        case RIGHT:
                            g.fillPolygon(new double[] { pX+(scale-scale/8)-1, pX+scale-1, pX+(scale-scale/8)-1 }, new double[] { pY+scale/6 , pY+scale/2, pY+(scale-scale/6) }, 3);
                            break;
                        case UP:
                            g.fillPolygon(new double[] { pX+scale/8, pX+scale/2, pX+(scale-scale/8) }, new double[] { pY+scale/6, pY+1, pY+scale/6 }, 3);
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        }

        /*Layer 4 Grid */
        if(state.renderSettings.drawGrid) {
            g.setFill(Color.GRAY);
            for(int x = dataXFrom+1, xr = 1; x < dataXTo; x++, xr++) {
                g.fillRect(xFrom.apply(x, xr), 0, 1, scale*(dataYTo-viewPort.cellY(scale))-skipFromFirstY);
            }
            for(int y = dataYFrom+1, yr = 1; y < dataYTo; y++, yr++) {
                g.fillRect(0, yFrom.apply(y, yr), scale*(dataXTo-viewPort.cellX(scale))-skipFromFirstX, 1);
            }
        }

        /*Layer 5 Line Tool Arrow */
        if(state.tool == Tools.LINE && state.line.state == TwoPointsSelectionStates.FIRST_SET) {
            drawArrow((state.line.points[0].x*scale-viewPort.x)+(scale/2), (state.line.points[0].y*scale-viewPort.y)+(scale/2), state.renderSettings.arrowX, state.renderSettings.arrowY, Math.ceil(scale*0.02), scale, g);
        }

        /*Layer 6 Line Tool Marker */
        if(state.line.state == TwoPointsSelectionStates.FIRST_SET && state.line.points[0].x >= dataXFrom && state.line.points[0].x <= dataXTo && state.line.points[0].y >= dataYFrom && state.line.points[0].y <= dataYTo) {
            g.setFill(Color.GREEN);
            g.fillOval((state.line.points[0].x*scale-viewPort.x), (state.line.points[0].y*scale-viewPort.y), scale, scale);
        }

        /*Layer 6 A Star Markers & Results */
        if(state.tool == Tools.A_STAR_ALGORITHM) {
            if(state.aStar.state == TwoPointsSelectionStates.FIRST_SET && state.aStar.points[0].x >= dataXFrom && state.aStar.points[0].x <= dataXTo && state.aStar.points[0].y >= dataYFrom && state.aStar.points[0].y <= dataYTo) {
                g.setFill(Color.TEAL);
                g.fillRoundRect((state.aStar.points[0].x*scale-viewPort.x), (state.aStar.points[0].y*scale-viewPort.y), scale, scale, 15, 15);
            }
            if(state.aStar.state == TwoPointsSelectionStates.BOTH_SET && state.aStar.points[1].x >= dataXFrom && state.aStar.points[1].x <= dataXTo && state.aStar.points[1].y >= dataYFrom && state.aStar.points[1].y <= dataYTo) {
                g.setFill(Color.INDIANRED);
                g.fillRoundRect((state.aStar.points[1].x*scale-viewPort.x), (state.aStar.points[1].y*scale-viewPort.y), scale, scale, 15, 15);
            }
            if(state.renderSettings.withAStar) {
                g.setFill(Color.TEAL);
                g.fillRect(((state.aStar.points[0].x)*scale-viewPort.x), ((state.aStar.points[0].y)*scale-viewPort.y), scale, scale);

                /* A Star Algorithm adds a dummy gird to realize teleportation, that's why we do -1, we convert it back.*/
                for(int i = 0; i < pathList.size(); i++) {
                    if(pathList.get(i).getX()-1 > currentMap.getWidthInBlocks() || pathList.get(i).getY()-1 > currentMap.getHeightInBlocks()) continue;
                    g.setFill(i == pathList.size() - 1 ? Color.INDIANRED : Color.SALMON);
                    g.fillRect(((pathList.get(i).getX()-1)*scale-viewPort.x), ((pathList.get(i).getY()-1)*scale-viewPort.y), scale, scale);
                }
            }
        }
    }

    static void drawArrow(double x1, double y1, double x2, double y2, double arrowScale, double scale, GraphicsContext g) {
        Affine def = new Affine(g.getTransform());
        g.setFill(Color.RED);
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        Transform transform = Transform.translate(x1, y1);
        transform = transform.createConcatenation(Transform.rotate(Math.toDegrees(angle), 0, 0));
        g.setTransform(new Affine(transform));
        g.fillRect(0, 0, len-arrowScale, arrowScale);
        g.fillPolygon(new double[]{len, len-8*arrowScale, len-8*arrowScale, len}, new double[]{0, -8*arrowScale, 8*arrowScale, 0}, 4);
        g.setFont(f);
        int distance = (int) Math.ceil(de.hshannover.inform.gnuman.app.util.Helper.euclideanDistance(x1+scale/2.0, y1+scale/2.0, x2, y2)/scale);
        g.fillText(Integer.toString(distance), len, -16);
        g.setTransform(def);
    }

    static void preparePathNodesWithTeleportation(PathNode[][] nodes, boolean[][] navigationData) {
        //Copy map data, set up teleporation dummies
        for(int y = 0; y < nodes.length; y++) {
            for(int x = 0; x < nodes[0].length; x++) {
                if((x == nodes[0].length-1 && y == 0 || x == 0 && y == nodes.length-1) || ((x == 0 || y == 0) ^ (x == nodes[0].length-1 || y == nodes.length-1))) {
                    nodes[y][x] = new PathNode(x, y, false);
                } else {
                    nodes[y][x] = new PathNode(x, y, navigationData[y-1][x-1]);
                }
            }
        }
        //Create teleporation links
        for(int y = 1; y < nodes.length-1; y++) {
            for(int x = 1; x < nodes[0].length-1; x++) {
                if(!navigationData[y-1][x-1]) continue;
                if(y == 1 && navigationData[nodes.length-3][x-1]) {
                    nodes[y-1][x] = new PathNode(x, nodes.length-1, true);
                    nodes[nodes.length-1][x] = new PathNode(x, y-1, true);
                }
                if(x == 1 && navigationData[y-1][nodes[0].length-3]) {
                    nodes[y][x-1] = new PathNode(nodes[0].length-1, y, true);
                    nodes[y][nodes[0].length-1] = new PathNode(x-1, y, true);
                }
            }
        }
    }

}
