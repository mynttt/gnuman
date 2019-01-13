package de.hshannover.inform.gnuman.app.modules.renderers;

import java.util.List;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.app.abstracts.RenderInstruction;
import de.hshannover.inform.gnuman.app.enums.GhostMovementStates;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.model.AbstractGhost;
import de.hshannover.inform.gnuman.app.model.GhostWall;
import de.hshannover.inform.gnuman.app.model.Player;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.MapCell;
import de.hshannover.inform.gnuman.app.modules.Entities;
import de.hshannover.inform.gnuman.app.modules.TileMap;
import de.hshannover.inform.gnuman.app.util.Helper;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;

/*
 *  ________    _______  _______   ____  ____   _______
 * |"      "\  /"     "||   _  "\ ("  _||_ " | /" _   "|
 * (.  ___  :)(: ______)(. |_)  :)|   (  ) : |(: ( \___)
 * |: \   ) || \/    |  |:     \/ (:  |  | . ) \/ \
 * (| (___\ || // ___)_ (|  _  \\  \\ \__/ //  //  \ ___
 * |:       :)(:      "||: |_)  :) /\\ __ //\ (:   _(  _|
 * (________/  \_______)(_______/ (__________) \_______)
 */

public class DebugRenderer extends RenderInstruction {
    private int tick;
    private Font gameFont;
    private final Color DEBUG_GHOST_CURRENT_CELL_COLOR = Color.PINK;
    private final Font DEBUG_FONT = new Font("System", 10);
    private final Font DEBUG_FONT_SMALL = new Font("System", 7);
    private final Color DEBUG_RENDER_COLOR_GHOST_DEAD = Color.DARKGRAY;
    private final Color DEBUG_RENDER_COLOR_GHOST_FRIGHT = Color.web("#0033FF");
    private final Color[] DEBUG_RENDERER_COLOR = {
            Color.WHITE,         //Empty
            Color.BLACK,         //Wall
            Color.WHITE,         //Food (Cell)
            Color.WHITE,         //Power up (Cell)
            Color.web("#FF2A99"),//Ghost Spawn
            Color.GREEN,         //Player Spawn
            Color.GRAY,          //Invisible Player Wall
            Color.web("#ffc231"),//Special Item Spawn
            Color.RED            //Ghost House Spawn
    };
    private final Color[] DEBUG_RENDER_COLOR_ENTITY = {
            Color.DARKGREEN,      //Player
            Color.web("#FF0000"), //Blinky
            Color.web("#33FFFF"), //Inky
            Color.web("#FF99CC"), //Pinky
            Color.web("#FFCC33")  //Clyde
    };
    private final Color[] DEBUG_RENDER_COLOR_ITEM = {
            Color.GRAY,             //Food
            Color.web("#ff5836"),   //Power up
            Color.web("#90103c")    //Bonus item
    };

    public DebugRenderer(GraphicsContext gc, DynamicVariables dyn, Font gameFont) {
        super(gc, dyn);
        this.gameFont = gameFont;
    }

    @Override
    public void renderStatics(TileMap map, boolean flicker) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, dyn.getGameAreaWidth(), dyn.getGameAndUiResolutionHeight());
        int x = 0, y = 0;
        for(StaticObjects[] vert : map.getMapObjectData()) {
            for(StaticObjects horz : vert) {
                gc.setFill(DEBUG_RENDERER_COLOR[horz.ordinal()]);
                gc.fillRect(x, y, dyn.getBlockWidth(), dyn.getBlockHeight());
                x += dyn.getBlockWidth();
            }
            x = 0;
            y += dyn.getBlockHeight();
        }
        for(int yc = dyn.getBlockAmountVertical() / map.getChunkCoordinator().getDimension(); yc >= 0; yc--) {
            for(int xc = dyn.getBlockAmountHorizontal() / map.getChunkCoordinator().getDimension(); xc >= 0; xc--) {
                map.getChunkCoordinator().getRegion(xc, yc).values().forEach(v -> {
                    v.values().forEach(item -> {
                        switch(item.getBlockType()) {
                            case FOOD: gc.setFill(DEBUG_RENDER_COLOR_ITEM[0]); break;
                            case POWERUP: gc.setFill(DEBUG_RENDER_COLOR_ITEM[1]); break;
                            default: break;
                        }
                        gc.fillRect(item.getX(), item.getY(), item.getWidth(), item.getHeight());
                    });
                });
            }
        }
        if(map.bonusItemSpawned()) {
            gc.setFill(DEBUG_RENDER_COLOR_ITEM[2]);
            gc.fillRect(map.getBonusItem().getX(), map.getBonusItem().getY(), map.getBonusItem().getWidth(), map.getBonusItem().getHeight());
        }
    }

    @Override
    public void renderEntities(Entities entities, List<GhostWall> ghostWalls, boolean isFrightened) {
        gc.setFill(DEBUG_RENDER_COLOR_ENTITY[0]);
        gc.fillRect(entities.getPlayer().getX(), entities.getPlayer().getY(), entities.getPlayer().getWidth(), entities.getPlayer().getHeight());
        for(AbstractGhost ghost : entities.getGhosts()) {
            if(Constants.DEBUG_GHOST_CURRENT_CELL) {
                gc.setFill(DEBUG_GHOST_CURRENT_CELL_COLOR);
                gc.fillRect(ghost.clampCellX()*dyn.getBlockWidth(), ghost.clampCellY()*dyn.getBlockHeight(), dyn.getBlockWidth(), dyn.getBlockHeight());
            }
            switch(ghost.getBehaviorState()) {
            case DEAD:
                gc.setFill(DEBUG_RENDER_COLOR_GHOST_DEAD);
                break;
            case FRIGHTENED:
                tick = ++tick % dyn.getFpsCap();
                gc.setFill(ghost.isFrighteningFlashOver() && tick < dyn.getFpsCap() / 2 ? Color.LIGHTGRAY : DEBUG_RENDER_COLOR_GHOST_FRIGHT);
                break;
            default:
                gc.setFill(DEBUG_RENDER_COLOR_ENTITY[ghost.getEntityType().ordinal()]);
                break;
        }

            gc.fillRect(ghost.getX(), ghost.getY(), ghost.getWidth(), ghost.getHeight());
        }
        if(Constants.DEBUG_GHOST_CURRENT_TARGET_TILE) {
            for(AbstractGhost ghost : entities.getGhosts()) {
                MapCell c = ghost.getCurrentTargetTile(entities.getPlayer());
                if(c != null) {
                    gc.setFill(DEBUG_RENDER_COLOR_ENTITY[ghost.getEntityType().ordinal()]);
                    drawTargetTileArrow(ghost, entities.getPlayer(), c);
               }
            }
        }
        gc.setTextAlign(TextAlignment.CENTER);
        if(Constants.DEBUG_DRAW_CELL_COORDINATES) {
            gc.setFont(DEBUG_FONT_SMALL);
            for(int y = 0; y < dyn.getBlockAmountVertical(); y++) {
                for(int x = 0; x < dyn.getBlockAmountHorizontal(); x++) {
                    gc.setFont(DEBUG_FONT);
                    gc.setFill(Color.AQUA);
                    gc.fillText(String.format("%02d %02d", x, y), x*dyn.getBlockWidth()+(dyn.getBlockWidth()/2), y*dyn.getBlockHeight()+dyn.getBlockHeight()-(dyn.getBlockHeight()/6));
                }
            }
        }
        gc.setFill(Color.BLACK);
        gc.setFont(gameFont);
    }

    private void drawTargetTileArrow(AbstractGhost ghost, Player p, MapCell c) {
        double x1 = ghost.getX() + ghost.getWidth() / 2.0;
        double x2 = c.getCellX()*dyn.getBlockWidth() + dyn.getBlockWidth() / 2.0;
        double y1 = ghost.getY() + ghost.getHeight() / 2.0;
        double y2 = c.getCellY()*dyn.getBlockHeight() + dyn.getBlockHeight() / 2.0;
        if(ghost.getMovementState() == GhostMovementStates.CHASE) {
            x2 = p.getX()+((c.getCellX()-p.clampCellX())*dyn.getBlockWidth()) + dyn.getBlockWidth() / 2.0;
            y2 = p.getY()+((c.getCellY()-p.clampCellY())*dyn.getBlockHeight()) + dyn.getBlockHeight() / 2.0;
            if(ghost.getEntityType() == EntityObjects.CLYDE && Helper.euclideanDistance(ghost.clampCellX(), ghost.clampCellY(), p.clampCellX(), p.clampCellY()) > 9.0) {
                gc.setStroke(DEBUG_RENDER_COLOR_ENTITY[4]);
                gc.strokeOval(ghost.getX()-9*dyn.getBlockWidth(), ghost.getY()-9*dyn.getBlockWidth(), 18*dyn.getBlockWidth(), 18*dyn.getBlockHeight());
            }
        }
        Affine def = new Affine(gc.getTransform());
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        Transform transform = Transform.translate(x1, y1);
        transform = transform.createConcatenation(Transform.rotate(Math.toDegrees(angle), 0, 0));
        gc.setTransform(new Affine(transform));
        gc.fillRect(0, 0, len-4, 4);
        gc.fillPolygon(new double[]{len, len-10, len-10, len}, new double[]{0, -10, 10, 0}, 4);
        gc.setTransform(def);
    }

    @Override
    public void sharedStaticImage(TileMap map, boolean flickerMap, Entities entities, boolean renderEntities) {
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, dyn.getGameAreaWidth(), dyn.getGameAndUiResolutionHeight());
        gc.setFill(Color.WHITE);
        gc.fillText("DEBUG ACTIVE!", dyn.getGameAreaWidth()/2, dyn.getGameAreaHeight()/2);
    }

}
