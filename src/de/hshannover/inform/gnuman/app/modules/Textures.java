package de.hshannover.inform.gnuman.app.modules;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javafx.scene.image.Image;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.enums.gameobjects.ObjectTypes;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.enums.gameobjects.OtherObjects;
import de.hshannover.inform.gnuman.app.model.animation.EntityAnimation;
import de.hshannover.inform.gnuman.app.model.animation.EntityAnimation.FrightenedStates;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.util.Helper;

/**
 * Texture manager
 * @author Marc Herschel
 */

public class Textures {
    private DynamicVariables dyn;
    private Image errorImage;
    private HashMap<ObjectTypes, HashMap<Enum<?>, Image>> textures;

    /**
     * Constructs a texture manager.
     * @param dyn Dynamic user options depending values.
     */
    public Textures(DynamicVariables dyn) {
        this.dyn = dyn;
        this.errorImage = new Image(getClass().getResourceAsStream(Constants.TEXTURE_PATH_PREFIX + Constants.TEXTURE_LOAD_ERROR));
        this.textures = new HashMap<>();

        if(errorImage == null || errorImage.isError()) {
            Log.critical(getClass().getSimpleName(), "Failed to load error texture properly! Exiting now.");
            Helper.exitOnCritical();
        }
    }

    /**
     * Hardcoded because we don't write a game engine here. I have nothing from a dynamic texture manager that loads textures from a text file.
     */
    public void loadBaseTextures() {
        try {
        //Statics
            loadTexture(ObjectTypes.STATIC, StaticObjects.FOOD, new Image(path("game/food.png"), dyn.getItemFoodWidth(), dyn.getItemFoodHeight(), true, false));
            loadTexture(ObjectTypes.OTHER, OtherObjects.GHOST_WALL, new Image(path("game/ghostwall.png"), dyn.getBlockWidth(), dyn.getBlockHeight(), true, false));
            loadTexture(ObjectTypes.STATIC, StaticObjects.POWERUP, new Image(path("game/powerup.png"), dyn.getItemWidth()-1, dyn.getBlockHeight()-1, true, false));
            loadTexture(ObjectTypes.OTHER, OtherObjects.BONUS_ITEM, new Image(path("game/bonus.png"), dyn.getEntitySpriteWidth()-2, dyn.getEntitySpriteHeight()-2, true, false));
        //UI
            loadTexture(ObjectTypes.OTHER, OtherObjects.LIFE, new Image(path("game/life.png"), dyn.getUiLifeDimension(), dyn.getUiLifeDimension(), true, false));
            loadTexture(ObjectTypes.OTHER, OtherObjects.GPL, new Image(path("game/gplv3.png"), dyn.getBlockWidth()*5, dyn.getBlockHeight()*3, true, false));
        } catch(Exception e) {
            Log.critical(getClass().getSimpleName(), "Failed to initiate all base textures correctly.");
            Log.critical(getClass().getSimpleName(), "Unknown Error." + Helper.stackTraceToString(e));
            Helper.exitOnCritical();
        }
        Log.info(getClass().getSimpleName(), "Initiated all base textures successfully.");
    }

    /**
     * Unload all textures.
     */
    public void unloadAll() {
        for(ObjectTypes t : ObjectTypes.values()) {
            if(textures.containsKey(t)) {
                textures.get(t).clear();
            }
        }
        Log.info(getClass().getSimpleName(), "Unloaded all textures!");
    }

    /**
     * Retrieve a texture.
     * @param objectType type of the texture.
     * @param textureId id of the texture
     * @return Texture if it exists, else error placeholder.
     */
    public Image getTexture(ObjectTypes objectType, Enum<?> textureId) {
        if(textures.containsKey(objectType) && textures.get(objectType).containsKey(textureId)) {
            return textures.get(objectType).get(textureId);
        }
        return errorImage;
    }

    /**
     * Load a texture into the manager.
     * @param objectType type of the texture.
     * @param textureId id of the texture
     * @param image to load.
     * @param width of the texture.
     * @param height of the texture.
     * @return true if it succeeded
     */
    public boolean loadTexture(ObjectTypes objectType, Enum<?> textureId, Image image, int width, int height) {
        boolean error = false;
        Image loadedImage;

        if(image == null || image.isError()) {
            loadedImage = errorImage;
            Log.warning(getClass().getSimpleName(), objectType + " -> " + textureId + " failed. Fallback to error image!");
            error = true;
        } else {
            loadedImage = image;
        }

        if(!textures.containsKey(objectType)) { textures.put(objectType, new HashMap<>()); }

        if(!textures.get(objectType).containsKey(textureId)) {
            textures.get(objectType).put(textureId, loadedImage);
            if(!error) {
                Log.info(getClass().getSimpleName(), objectType + " -> " + textureId + " loaded. " + "[W: " + loadedImage.getWidth() + " H: " + loadedImage.getHeight() +"]" );
            }
        } else {
            error = true;
            Log.warning(getClass().getSimpleName(),objectType + " -> " + textureId + " is already loaded. Use unloadTexture() to free resources first!");
        }

        return !error;
    }

    /**
     * Load a texture into the manager. (Using default block size as dimension).
     * @param objectType type of the texture.
     * @param textureId id of the texture
     * @param image to load.
     * @return true if it succeeded
     */
    public boolean loadTexture(ObjectTypes objectType, Enum<?> textureId, Image image) {
        return loadTexture(objectType, textureId, image, dyn.getBlockWidth(), dyn.getBlockHeight());
    }

    /**
     * Unloads a texture.
     * @param objectType type of the texture.
     * @param textureId id of the texture
     * @return true if it succeeded
     */
    public boolean unloadTexture(ObjectTypes objectType, Enum<?> textureId) {
        if(textures.containsKey(objectType)) {
            Log.info(getClass().getSimpleName(), objectType + " -> " + textureId + " unloaded.");
            return textures.get(objectType).remove(textureId) != null;
        }
        return false;
    }

    /**
     * @return error texture.
     */
    public Image getErrorTexture() {
        return errorImage;
    }

    /**
     * Static factory method for Entity Animation.
     * @param fpsCap max fps
     * @param spriteWidth width in px
     * @param spriteHeight height in px
     * @return animation object
     * @throws Exception whenever something goes wrong here
     */
    public static EntityAnimation createEntityAnimation(int fpsCap, int spriteWidth, int spriteHeight) throws Exception {
        EntityAnimation e = new EntityAnimation(fpsCap);

        int x = 0, y = 0, m = Constants.TEXTURE_SPRITE_SHEET_DIMENSION;

        java.awt.Image sheet = ImageIO.read(Textures.class.getResourceAsStream(path("game/" + (Constants.TEXTURE_USE_CLASSIC_SPRITES_FOR_GHOSTS ? Constants.TEXTURE_GHOST_SPRITES_CLASSIC : Constants.TEXTURE_GHOST_SPRITES_CORPORATIONS))));
        BufferedImage bimage = new BufferedImage(112, 70, BufferedImage.TYPE_INT_ARGB);

        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(sheet, 0, 0, 112, 70, null);
        bGr.dispose();

        for(EntityObjects ghost : EntityObjects.ghosts()) {
            for(Directions d : Directions.loadOrder()) {
                Image[] resizedSprites = new Image[2];
                for(int i = 0; i < 2; i++) {
                    resizedSprites[i] = cutSprite(bimage, x, y, m, m, spriteWidth, spriteHeight);
                    x+=m;
                }
                e.registerAnimation(ghost, d, resizedSprites);
            }
            x = 0;
            y+=m;
        }

    //Special animations and images
        x = 0;
        Image[] blueImages = new Image[2];
        blueImages[0] = cutSprite(bimage, x, y, m, m, spriteWidth, spriteHeight);
        x+=m;
        blueImages[1] = cutSprite(bimage, x, y, m, m, spriteWidth, spriteHeight);
        Image[] whiteImages = new Image[2];
        x+=m;
        whiteImages[0] = cutSprite(bimage, x, y, m, m, spriteWidth, spriteHeight);
        x+=m;
        whiteImages[1] = cutSprite(bimage, x, y, m, m, spriteWidth, spriteHeight);
        e.registerFrightenedAnimation(FrightenedStates.BLUE, blueImages);
        e.registerFrightenedAnimation(FrightenedStates.WHITE, whiteImages);

        for(Directions d : Directions.loadOrder()) {
            x+=m;
            e.registerDeadAnimation(d, cutSprite(bimage, x, y, m, m, spriteHeight, spriteHeight));
        }

    //Player
        sheet = ImageIO.read(Textures.class.getResourceAsStream(path("game/rms.png")));
        bimage = new BufferedImage(256, 32, BufferedImage.TYPE_INT_ARGB);
        bGr = bimage.createGraphics();
        bGr.drawImage(sheet, 0, 0, 256, 32, null);
        bGr.dispose();

        y=0; x=0;

        for(Directions d : Directions.loadOrder()) {
            Image[] resizedSprites = new Image[2];
            for(int i = 0; i < 2; i++) {
                resizedSprites[i] = cutSprite(bimage, x, y, 32, 32, spriteWidth, spriteHeight);
                x+=32;
            }
            e.registerAnimation(EntityObjects.PLAYER, d, resizedSprites);
        }

        return e;
    }

    /**
     * Static factory method for the map editor.
     * @param location location within texture folder
     * @return image of texture.
     */
    public static Image loadStaticImage(String location) { return new Image(path(location)); }

    /**
     * Shortcut
     * @param s relative path from /textures
     * @return full path
     */
    private static String path(String s) {
        return Constants.TEXTURE_PATH_PREFIX + s;
    }

    /*
     * Cut sprites from a buffered Image
     */
    private static Image cutSprite(BufferedImage sheet, int x, int y, int width, int height, int requestedWidth, int requestedHeight) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(sheet.getSubimage(x, y, width, height), "png", os);
        Image sprite = new Image(new ByteArrayInputStream(os.toByteArray()), requestedWidth, requestedHeight, true, false);
        os.close();
        return sprite;
    }
}
