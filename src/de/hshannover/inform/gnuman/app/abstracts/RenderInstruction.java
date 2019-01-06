package de.hshannover.inform.gnuman.app.abstracts;

import java.util.List;
import de.hshannover.inform.gnuman.app.model.GhostWall;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.modules.Entities;
import de.hshannover.inform.gnuman.app.modules.TileMap;
import javafx.scene.canvas.GraphicsContext;

/**
 * Instructions for the renderer.
 * @author Marc Herschel
 */

public abstract class RenderInstruction {
    protected GraphicsContext gc;
    protected DynamicVariables dyn;

    public RenderInstruction(GraphicsContext gc, DynamicVariables dyn) {
        this.gc = gc;
        this.dyn = dyn;
    }

    /**
     * How to render statics and the background.
     * @param map map to render.
     * @param flicker if the renderer should switch between normal map and flicker layout frequently.
     */
    public abstract void renderStatics(TileMap map, boolean flicker);

    /**
     * How to render entities.
     * @param entities entities to render
     * @param ghostWalls ghostwalls to render
     * @param isFrightened if frightening is enabled
     */
    public abstract void renderEntities(Entities entities, List<GhostWall> ghostWalls, boolean isFrightened);

    /**
     * Render the map for waiting screens.
     * @param map to render
     * @param flickerMap if flicker
     * @param entities to render
     * @param renderEntities true if render entites
     */
    public abstract void sharedStaticImage(TileMap map, boolean flickerMap, Entities entities, boolean renderEntities);
}
