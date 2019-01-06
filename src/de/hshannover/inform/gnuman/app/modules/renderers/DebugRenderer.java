package de.hshannover.inform.gnuman.app.modules.renderers;

import java.util.List;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.app.abstracts.RenderInstruction;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.model.AbstractGhost;
import de.hshannover.inform.gnuman.app.model.GhostWall;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.modules.Entities;
import de.hshannover.inform.gnuman.app.modules.TileMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

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

    public DebugRenderer(GraphicsContext gc, DynamicVariables dyn) {
        super(gc, dyn);
    }

    @Override
    public void renderStatics(TileMap map, boolean flicker) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, dyn.getGameAreaWidth(), dyn.getGameAndUiResolutionHeight());
        int x = 0, y = 0;
        for(StaticObjects[] vert : map.getMapObjectData()) {
            for(StaticObjects horz : vert) {
                gc.setFill(Constants.DEBUG_RENDERER_COLOR[horz.ordinal()]);
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
                            case FOOD: gc.setFill(Constants.DEBUG_RENDER_COLOR_ITEM[0]); break;
                            case POWERUP: gc.setFill(Constants.DEBUG_RENDER_COLOR_ITEM[1]); break;
                            default: break;
                        }
                        gc.fillRect(item.getX(), item.getY(), item.getWidth(), item.getHeight());
                    });
                });
            }
        }
        if(map.bonusItemSpawned()) {
            gc.setFill(Constants.DEBUG_RENDER_COLOR_ITEM[2]);
            gc.fillRect(map.getBonusItem().getX(), map.getBonusItem().getY(), map.getBonusItem().getWidth(), map.getBonusItem().getHeight());
        }
    }

    @Override
    public void renderEntities(Entities entities, List<GhostWall> ghostWalls, boolean isFrightened) {
        gc.setFill(Constants.DEBUG_RENDER_COLOR_ENTITY[0]);
        gc.fillRect(entities.getPlayer().getX(), entities.getPlayer().getY(), entities.getPlayer().getWidth(), entities.getPlayer().getHeight());
        for(AbstractGhost ghost : entities.getGhosts()) {
            if(Constants.DEBUG_GHOST_CURRENT_CELL) {
                gc.setFill(Constants.DEBUG_GHOST_CURRENT_CELL_COLOR);
                gc.fillRect(ghost.clampCellX()*dyn.getBlockWidth(), ghost.clampCellY()*dyn.getBlockHeight(), dyn.getBlockWidth(), dyn.getBlockHeight());
            }
            gc.setFill(Constants.DEBUG_RENDER_COLOR_ENTITY[ghost.getEntityType().ordinal()]);
            gc.fillRect(ghost.getX(), ghost.getY(), ghost.getWidth(), ghost.getHeight());
        }
        if(Constants.DEBUG_GHOST_CURRENT_TARGET_TILE) {
            for(AbstractGhost ghost : entities.getGhosts()) {
                if(ghost.getCurrentTargetTile() != null) {
                    gc.setFill(Constants.DEBUG_RENDER_COLOR_ENTITY[ghost.getEntityType().ordinal()]);
                    gc.fillOval(ghost.getCurrentTargetTile().getCellX()*dyn.getBlockWidth()+(dyn.getBlockWidth()/2), ghost.getCurrentTargetTile().getCellY()*dyn.getBlockHeight()+(dyn.getBlockHeight()/2), dyn.getBlockWidth()/4, dyn.getBlockHeight()/4);
               }
            }
        }
        gc.setTextAlign(TextAlignment.CENTER);
        if(Constants.DEBUG_DRAW_CELL_COORDINATES) {
            gc.setFont(Constants.DEBUG_FONT_SMALL);
            for(int y = 0; y < dyn.getBlockAmountVertical(); y++) {
                for(int x = 0; x < dyn.getBlockAmountHorizontal(); x++) {
                    gc.setFill(Color.AQUA);
                    gc.fillText(String.format("%02d %02d", x, y), x*dyn.getBlockWidth()+(dyn.getBlockWidth()/2), y*dyn.getBlockHeight()+dyn.getBlockHeight()-(dyn.getBlockHeight()/6));
                }
            }
        }
        gc.setFont(Constants.DEBUG_FONT);
        gc.fillText("DEBUG RENDERER ACTIVE!", dyn.getGameAreaWidth() / 2 , dyn.getGameAreaHeight()+dyn.getUiHeight()/2);
    }

    @Override
    public void sharedStaticImage(TileMap map, boolean flickerMap, Entities entities, boolean renderEntities) {
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, dyn.getGameAreaWidth(), dyn.getGameAndUiResolutionHeight());
        gc.setFill(Color.WHITE);
        gc.fillText("DEBUG ACTIVE!", dyn.getGameAreaWidth()/2, dyn.getGameAreaHeight()/2);
    }

}
