package de.hshannover.inform.gnuman.app.model.storage;

import java.awt.Toolkit;

import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.app.enums.Difficulty;

/**
 * Dynamically generated values that are important in some of the games internal workings.<br>
 * These will be set once with the newSession() function and can then be used everywhere the object is referenced.
 * @author Marc Herschel
 */

public class DynamicVariables {
    /* WIZARD STUFF DON'T TOUCH */
    private static final double WIDTH_FONT_MULTIPLIER = 0.03285714285;

    private int blockWidth;
    private int blockAmountHorizontal;
    private int blockHeight;
    private int blockAmountVertical;
    private int uiLifeDimension;
    private int uiLifeVerticalPosition;
    private int entityHitboxWidth;
    private int entityHitboxHeight;
    private int entitySpriteHeight;
    private int entitySpriteWidth;
    private int itemFoodWidth;
    private int itemFoodHeight;
    private int itemWidth;
    private int itemHeight;
    private int gameAreaWidth;
    private int gameAreaHeight;
    private int gameResolutionWidth;
    private int gameResolutionHeight;
    private int textMarginHorizontal;
    private int targetedFps;
    private int highscoreAnimationBlock;
    private int highscoreAnimationEntityBlock;
    private double resolutionHeight;
    private double textMarginVertical;
    private double uiHeight;
    private double uiFontSize;
    private double gameBaseSpeed;
    private Difficulty difficulty;

    /**
     * Update values for a new game session.
     * @param blocksX amount of blocks x
     * @param blocksY amount of blocks y
     * @param blockWidth width in px
     * @param blockHeight height in px
     * @param targetedFps currently targeted upper fps limit
     * @param difficulty targeted difficulty
     * @param dynamicCamera if we shall use a dynamic camera
     */
    public void newSession(int blocksX, int blocksY, int blockWidth, int blockHeight, int targetedFps, Difficulty difficulty, boolean dynamicCamera) {
        this.blockAmountHorizontal = blocksX;
        this.blockAmountVertical = blocksY;
        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;
        this.targetedFps = targetedFps;
        this.difficulty = difficulty;
        calculateOnce(dynamicCamera);
    }

    /**
     * Calculation once a session started.
     * @param dynamicCamera if a dynamic camera shall be used
     */
    private void calculateOnce(boolean dynamicCamera) {
        int blockDimension = (int) Math.round(((blockWidth + blockHeight) / 2));
        entityHitboxWidth = blockWidth - 2;
        entityHitboxHeight = blockHeight - 2;
        entitySpriteWidth = blockWidth + (blockWidth / 2);
        entitySpriteHeight = blockHeight + (blockHeight / 2);
        itemFoodWidth = (int) Math.round(blockWidth * 0.28);
        itemFoodHeight = (int) Math.round(blockHeight * 0.28);
        itemWidth = (int) Math.round(blockWidth * 0.76);
        itemHeight = (int) Math.round(blockHeight * 0.76);
        gameAreaWidth = blockAmountHorizontal * blockWidth;
        gameAreaHeight = blockAmountVertical * blockHeight;
        uiHeight =  blockDimension * 2.5;
        uiLifeDimension = blockDimension * 2;
        if(dynamicCamera) {
            gameResolutionHeight = 775;
            gameResolutionWidth = 700;
            while(gameResolutionHeight + uiHeight > Toolkit.getDefaultToolkit().getScreenSize().getHeight()) { gameResolutionHeight-=25; }
            while(gameResolutionWidth > Toolkit.getDefaultToolkit().getScreenSize().getWidth()) { gameResolutionWidth-=25; }
            uiFontSize = Math.round(25 * 0.92);
            highscoreAnimationBlock = 25;
            highscoreAnimationEntityBlock = 37;
        } else {
            gameResolutionHeight = blockAmountVertical*blockHeight;
            gameResolutionWidth = blockAmountHorizontal*blockWidth;
            uiFontSize = (blockDimension * (blockAmountHorizontal*WIDTH_FONT_MULTIPLIER) < uiHeight) ? blockDimension * (blockAmountHorizontal*WIDTH_FONT_MULTIPLIER) : uiHeight - 0.001;
            highscoreAnimationBlock = blockDimension;
            highscoreAnimationEntityBlock = entitySpriteHeight;
        }
        resolutionHeight = gameResolutionHeight + uiHeight;
        uiLifeVerticalPosition = gameResolutionHeight + blockDimension - (blockDimension/2);
        textMarginHorizontal = gameResolutionWidth - (gameResolutionWidth / 30);
        textMarginVertical = gameResolutionHeight + 1.5*blockDimension;
        calculateBaseSpeed();
    }

    /**
     * Base speed regarding debug modifiers.
     */
    private void calculateBaseSpeed() {
        if(Constants.DEBUG_DISABLE_SPEED_ADJUSTMENT) {
            gameBaseSpeed = difficulty.getSpeedScale()* Constants.GAME_MOVEMENT_SPEED_TO_BLOCK_RATIO * ((blockHeight + blockWidth) / 2.0);
        } else {
            gameBaseSpeed = difficulty.getSpeedScale() * Constants.GAME_MOVEMENT_SPEED_TO_BLOCK_RATIO  * ((blockHeight + blockWidth) / 2.0) * 60.0/targetedFps;
        }
    }

    public int getGameAreaResolutionWidth() { return gameResolutionWidth; }
    public int getGameAreaResolutionHeight() { return gameResolutionHeight; }
    public double getGameAndUiResolutionHeight() { return resolutionHeight; }

    public int getGameAreaWidth() { return gameAreaWidth; }
    public int getGameAreaHeight() { return gameAreaHeight; }

    public int getBlockWidth() { return blockWidth; }
    public int getBlockHeight() { return blockHeight; }
    public int getBlockAmountHorizontal() { return blockAmountHorizontal; }
    public int getBlockAmountVertical() { return blockAmountVertical; }

    public int getHighscoreBlockSize() { return highscoreAnimationBlock; }
    public int getHighscoreEntitySize() { return highscoreAnimationEntityBlock; }
    public int getUiLifeDimension() { return uiLifeDimension; }
    public int getUiLifeVerticalPosition() { return uiLifeVerticalPosition; }
    public double getUiHeight() { return uiHeight; }

    public int getEntityHitboxWidth() { return entityHitboxWidth; }
    public int getEntityHitboxHeight() { return entityHitboxHeight; }
    public int getEntitySpriteHeight() { return entitySpriteHeight; }
    public int getEntitySpriteWidth() { return entitySpriteWidth; }

    public int getItemFoodWidth() { return itemFoodWidth; }
    public int getItemFoodHeight() { return itemFoodHeight; }
    public int getItemWidth() { return itemWidth; }
    public int getItemHeight() { return itemHeight; }

    public int getTextMarginHorizontal() { return textMarginHorizontal; }
    public double getTextMarginVertical() { return textMarginVertical; }
    public double getUiFontSize() { return uiFontSize; }

    public int getFpsCap() { return targetedFps;}
    public double getBaseSpeed() { return gameBaseSpeed; }

    public Difficulty getDifficulty() { return difficulty; }
}
