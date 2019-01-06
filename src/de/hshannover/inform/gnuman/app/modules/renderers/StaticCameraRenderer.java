package de.hshannover.inform.gnuman.app.modules.renderers;

import java.util.List;
import de.hshannover.inform.gnuman.app.abstracts.RenderInstruction;
import de.hshannover.inform.gnuman.app.enums.gameobjects.ObjectTypes;
import de.hshannover.inform.gnuman.app.enums.gameobjects.OtherObjects;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.model.AbstractGhost;
import de.hshannover.inform.gnuman.app.model.GhostWall;
import de.hshannover.inform.gnuman.app.model.animation.AnimationToggle;
import de.hshannover.inform.gnuman.app.model.animation.EntityAnimation;
import de.hshannover.inform.gnuman.app.model.chunks.ChunkCoordinator;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.modules.Entities;
import de.hshannover.inform.gnuman.app.modules.Textures;
import de.hshannover.inform.gnuman.app.modules.TileMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Render implementation for a static camera.
 * @author Marc Herschel
 */
public class StaticCameraRenderer extends RenderInstruction {
    private Textures textures;
    private AnimationToggle powerups, background;
    private EntityAnimation animation;
    private int spriteHitboxHeightOffset, spriteHitboxWidthOffset;

    public StaticCameraRenderer(GraphicsContext gc, DynamicVariables dyn, Textures textures, EntityAnimation animation) {
        super(gc, dyn);
        this.textures = textures;
        this.animation = animation;
        this.spriteHitboxHeightOffset = (dyn.getEntityHitboxHeight() - dyn.getEntitySpriteHeight()) / 2;
        this.spriteHitboxWidthOffset = (dyn.getEntityHitboxWidth() - dyn.getEntitySpriteWidth()) / 2;
        powerups = new AnimationToggle((int) (dyn.getFpsCap()*0.5));
        background = new AnimationToggle(dyn.getFpsCap()*2);
    }

    @Override
    public void renderStatics(TileMap map, boolean flicker) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, dyn.getGameAreaWidth(), dyn.getGameAndUiResolutionHeight());
        gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.MAZE), 0, 0, dyn.getGameAreaWidth(), dyn.getGameAreaHeight());
        renderItems(map.getChunkCoordinator());
        if(map.bonusItemSpawned()) {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.BONUS_ITEM), map.getBonusItem().getX()+spriteHitboxWidthOffset-2, map.getBonusItem().getY()+spriteHitboxHeightOffset-2);
        }
    }

    @Override
    public void renderEntities(Entities entities, List<GhostWall> ghostWalls, boolean isFrightened) {
        animation.update(isFrightened);
        gc.drawImage(animation.forPlayer(entities.getPlayer()), entities.getPlayer().getX()+spriteHitboxWidthOffset, entities.getPlayer().getY()+spriteHitboxHeightOffset);
        for(AbstractGhost ghost : entities.getGhosts()) {
            switch(ghost.getBehaviorState()) {
                case DEAD:
                    gc.drawImage(animation.forGhostDead(ghost), ghost.getX()+spriteHitboxWidthOffset, ghost.getY()+spriteHitboxHeightOffset);
                    break;
                case FRIGHTENED:
                    gc.drawImage(animation.forGhostFrightened(ghost), ghost.getX()+spriteHitboxWidthOffset, ghost.getY()+spriteHitboxHeightOffset);
                    break;
                default:
                    gc.drawImage(animation.forGhost(ghost), ghost.getX()+spriteHitboxWidthOffset, ghost.getY()+spriteHitboxHeightOffset);
                    break;
            }
        }
        for(GhostWall wall : ghostWalls) {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.GHOST_WALL), wall.getX(), wall.getY());
        }
    }

    @Override
    public void sharedStaticImage(TileMap map, boolean flickerMap, Entities entities, boolean renderEntities) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, dyn.getGameAreaWidth(), dyn.getGameAndUiResolutionHeight());
        if(flickerMap) {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, background.updateAndIsDisplay() ? OtherObjects.MAZE_FLICKER : OtherObjects.MAZE), 0, 0, dyn.getGameAreaWidth(), dyn.getGameAreaHeight());
            if(background.updateAndIsDisplay()) {
                renderItems(map.getChunkCoordinator());
            }
        } else {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.MAZE), 0, 0, dyn.getGameAreaWidth(), dyn.getGameAreaHeight());
            renderItems(map.getChunkCoordinator());
        }
        if(map.bonusItemSpawned()) {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.BONUS_ITEM), map.getBonusItem().getX()+spriteHitboxWidthOffset-2, map.getBonusItem().getY()+spriteHitboxHeightOffset-2);
        }
        for(GhostWall wall : map.getGhostWalls()) {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.GHOST_WALL), wall.getX(), wall.getY());
        }
        if(renderEntities) { renderEntities(entities, map.getGhostWalls(), false); }
    }

    private void renderItems(ChunkCoordinator c) {
        boolean renderPowerUps = powerups.updateAndIsDisplay();
        for(int yc = dyn.getBlockAmountVertical() / c.getDimension(); yc >= 0; yc--) {
            for(int xc = dyn.getBlockAmountHorizontal() / c.getDimension(); xc >= 0; xc--) {
                c.getRegion(xc, yc).values().forEach(v -> {
                    v.values().forEach(item -> {
                        if(item.getBlockType() == StaticObjects.POWERUP) {
                            if(renderPowerUps) { gc.drawImage(textures.getTexture(ObjectTypes.STATIC, item.getBlockType()), item.getX(), item.getY()); }
                        } else {
                            gc.drawImage(textures.getTexture(ObjectTypes.STATIC, item.getBlockType()), item.getX(), item.getY());
                        }
                    });
                });
            }
        }
    }
}
