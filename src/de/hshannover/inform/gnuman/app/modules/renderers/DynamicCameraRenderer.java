package de.hshannover.inform.gnuman.app.modules.renderers;

import java.util.List;
import de.hshannover.inform.gnuman.app.abstracts.RenderInstruction;
import de.hshannover.inform.gnuman.app.enums.gameobjects.ObjectTypes;
import de.hshannover.inform.gnuman.app.enums.gameobjects.OtherObjects;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.model.AbstractGhost;
import de.hshannover.inform.gnuman.app.model.GhostWall;
import de.hshannover.inform.gnuman.app.model.Player;
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
 * Render implementation for a dynamic camera (the player).
 * @author Marc Herschel
 */
public class DynamicCameraRenderer extends RenderInstruction {
    private Textures textures;
    private AnimationToggle powerups, background;
    private EntityAnimation animation;
    private Player player;
    private int spriteHitboxHeightOffset, spriteHitboxWidthOffset;
    private double tX, tY;

    public DynamicCameraRenderer(GraphicsContext gc, DynamicVariables dyn, Textures textures, EntityAnimation animation, Player player) {
        super(gc, dyn);
        this.textures = textures;
        this.animation = animation;
        this.spriteHitboxHeightOffset = (dyn.getEntityHitboxHeight() - dyn.getEntitySpriteHeight()) / 2;
        this.spriteHitboxWidthOffset = (dyn.getEntityHitboxWidth() - dyn.getEntitySpriteWidth()) / 2;
        this.player = player;
        powerups = new AnimationToggle((int) (dyn.getFpsCap()*0.5));
        background = new AnimationToggle(dyn.getFpsCap()*2);
        transform();
    }

    @Override
    public void renderStatics(TileMap map, boolean flicker) {
        transform();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, dyn.getGameAreaResolutionWidth(), dyn.getGameAndUiResolutionHeight());
        gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.MAZE),
                player.getX()-dyn.getGameAreaResolutionWidth()/2.0,
                player.getY()-dyn.getGameAreaResolutionHeight()/2.0,
                dyn.getGameAreaResolutionWidth(),
                dyn.getGameAreaResolutionHeight(),
                0,
                0,
                dyn.getGameAreaResolutionWidth(),
                dyn.getGameAreaResolutionHeight());
        renderItems(map.getChunkCoordinator());
        if(map.bonusItemSpawned()) {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.BONUS_ITEM),
                   tX+map.getBonusItem().getX()+spriteHitboxWidthOffset-2,
                   tY+map.getBonusItem().getY()+spriteHitboxHeightOffset-2);
        }
    }

    @Override
    public void renderEntities(Entities entities, List<GhostWall> ghostWalls, boolean isFrightened) {
        animation.update(isFrightened);
        gc.drawImage(animation.forPlayer(entities.getPlayer()),
                dyn.getGameAreaResolutionWidth()/2+spriteHitboxWidthOffset,
                dyn.getGameAreaResolutionHeight()/2+spriteHitboxHeightOffset);
        for(AbstractGhost ghost : entities.getGhosts()) {
            switch(ghost.getBehaviorState()) {
                case DEAD:
                    gc.drawImage(animation.forGhostDead(ghost),
                            tX+ghost.getX()+spriteHitboxWidthOffset,
                            tY+ghost.getY()+spriteHitboxHeightOffset);
                    break;
                case FRIGHTENED:
                    gc.drawImage(animation.forGhostFrightened(ghost),
                            tX+ghost.getX()+spriteHitboxWidthOffset,
                            tY+ghost.getY()+spriteHitboxHeightOffset);
                    break;
                default:
                    gc.drawImage(animation.forGhost(ghost),
                            tX+ghost.getX()+spriteHitboxWidthOffset,
                            tY+ghost.getY()+spriteHitboxHeightOffset);
                    break;
            }
        }
        for(GhostWall wall : ghostWalls) {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.GHOST_WALL),
                    tX+wall.getX(),
                    tY+wall.getY());
        }
    }

    @Override
    public void sharedStaticImage(TileMap map, boolean flickerMap, Entities entities, boolean renderEntities) {
        transform();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, dyn.getGameAreaResolutionWidth(), dyn.getGameAndUiResolutionHeight());
        if(flickerMap) {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, background.updateAndIsDisplay() ? OtherObjects.MAZE_FLICKER : OtherObjects.MAZE),
                    player.getX()-dyn.getGameAreaResolutionWidth()/2.0,
                    player.getY()-dyn.getGameAreaResolutionHeight()/2.0,
                    dyn.getGameAreaResolutionWidth(),
                    dyn.getGameAreaResolutionHeight(),
                    0,
                    0,
                    dyn.getGameAreaResolutionWidth(),
                    dyn.getGameAreaResolutionHeight());
            if(background.updateAndIsDisplay()) renderItems(map.getChunkCoordinator());
        } else {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.MAZE),
                    player.getX()-dyn.getGameAreaResolutionWidth()/2.0,
                    player.getY()-dyn.getGameAreaResolutionHeight()/2.0,
                    dyn.getGameAreaResolutionWidth(),
                    dyn.getGameAreaResolutionHeight(),
                    0,
                    0,
                    dyn.getGameAreaResolutionWidth(),
                    dyn.getGameAreaResolutionHeight());
            renderItems(map.getChunkCoordinator());
        }
        if(map.bonusItemSpawned()) {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.BONUS_ITEM),
                    tX+map.getBonusItem().getX()+spriteHitboxWidthOffset-2,
                    tY+map.getBonusItem().getY()+spriteHitboxHeightOffset-2);
        }
        for(GhostWall wall : map.getGhostWalls()) {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.GHOST_WALL), tX+wall.getX(), tY+wall.getY());
        }
        if(renderEntities) { renderEntities(entities, map.getGhostWalls(), false); }
        gc.fillRect(0, dyn.getGameAreaResolutionHeight(), dyn.getGameAreaResolutionWidth(), dyn.getUiHeight());
    }

    private void transform() {
        tX = dyn.getGameAreaResolutionWidth()/2.0-player.getX();
        tY = dyn.getGameAreaResolutionHeight()/2.0-player.getY();
    }


    private void renderItems(ChunkCoordinator c) {
        boolean renderPowerUps = powerups.updateAndIsDisplay();
        int yLow = (int) (player.getY() - dyn.getGameAreaResolutionHeight()/2) / dyn.getBlockHeight() / c.getDimension();
        int yHigh = (int) (player.getY() + dyn.getGameAreaResolutionHeight()/2) / dyn.getBlockHeight() / c.getDimension();
        int xLow = (int) (player.getX() - dyn.getGameAreaResolutionWidth() / 2) / dyn.getBlockWidth() / c.getDimension();
        int xHigh = (int) (player.getX() + dyn.getGameAreaResolutionWidth() / 2) / dyn.getBlockWidth() / c.getDimension();
        for(int yc = yHigh; yc >= yLow; yc--) {
            for(int xc = xHigh; xc >= xLow; xc--) {
                c.getRegion(xc, yc).values().forEach(v -> {
                    v.values().forEach(item -> {
                        if(item.getBlockType() == StaticObjects.POWERUP) {
                            if(renderPowerUps) { gc.drawImage(textures.getTexture(ObjectTypes.STATIC, item.getBlockType()), tX+item.getX(), tY+item.getY()); }
                        } else {
                            gc.drawImage(textures.getTexture(ObjectTypes.STATIC, item.getBlockType()), tX+item.getX(), tY+item.getY());
                        }
                    });
                });
            }
        }
    }

}
