package de.hshannover.inform.gnuman.app.modules;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.TextAlignment;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.abstracts.HighscoreAnimation;
import de.hshannover.inform.gnuman.app.abstracts.RenderInstruction;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.GameStates;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.enums.gameobjects.ObjectTypes;
import de.hshannover.inform.gnuman.app.enums.gameobjects.OtherObjects;
import de.hshannover.inform.gnuman.app.model.Player;
import de.hshannover.inform.gnuman.app.model.animation.AnimationToggle;
import de.hshannover.inform.gnuman.app.model.animation.EntityAnimation;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.GameVariableTracker;
import de.hshannover.inform.gnuman.app.modules.renderers.DebugRenderer;
import de.hshannover.inform.gnuman.app.modules.renderers.DynamicCameraRenderer;
import de.hshannover.inform.gnuman.app.modules.renderers.StaticCameraRenderer;
import de.hshannover.inform.gnuman.app.util.Helper;

/**
 * Render game on a canvas.
 * @author Marc Herschel
 */

public class Renderer {
    private DynamicVariables dyn;
    private GraphicsContext gc;
    private Textures textures;
    private Font gameFont, smallFont, largeFont, mediumFont;
    private RenderInstruction instruction;
    private AnimationToggle text;
    private HighscoreAnimation highscoreFailed, highscoreMade;
    private EntityAnimation animation;

    /**
     * Construct a rendered and decide if to use the normal/debug renderer.
     * @param gc context of canvas.
     * @param textures texture manager.
     * @param dyn Dynamic user options depending values.
     * @param dynamicCamera if we use a dynamic camera
     * @param player for the dynamic camera (can be null if no dynamic camera)
     */
    public Renderer(GraphicsContext gc, Textures textures, DynamicVariables dyn, boolean dynamicCamera, Player player) {
        this.dyn = dyn;
        this.gc = gc;
        this.textures = textures;
        gc.setFontSmoothingType(FontSmoothingType.GRAY);
        gc.setFont(gameFont);

        try {
            animation = Textures.createEntityAnimation(dyn.getFpsCap(), dyn.getEntitySpriteWidth(), dyn.getEntitySpriteHeight());
        } catch(Exception e) {
            Log.critical(getClass().getSimpleName(), "Failed to initiate animations:" + Helper.stackTraceToString(e));
            Helper.exitOnCritical();
        }

        if(Constants.DEBUG_RENDERER) {
            instruction = new DebugRenderer(gc, dyn);
        } else {
            instruction = dynamicCamera ? new DynamicCameraRenderer(gc, dyn, textures, animation, player) : new StaticCameraRenderer(gc, dyn, textures, animation);
        }

        highscoreMade = new HighscoreAnimation() {

            @Override
            public void renderNextFrame() {
                double speed = dyn.getBaseSpeed()*0.75;
                animation.update(false);
                if(reset) { gplX = -dyn.getEntitySpriteWidth()*7; playerX = -dyn.getEntitySpriteWidth()*6; ghostX = -dyn.getEntitySpriteWidth(); reset = false; }
                gplX+=speed; playerX+=speed; ghostX+=speed;

                for(EntityObjects o : EntityObjects.ghosts()) {
                    gc.drawImage(animation.forAnimationWithoutObject(o, Directions.RIGHT), ghostX, (7+o.ordinal()*2)*dyn.getEntitySpriteHeight());
                }
                gc.drawImage(animation.forAnimationWithoutObject(EntityObjects.PLAYER, Directions.RIGHT), playerX, 14*dyn.getEntitySpriteHeight()-dyn.getEntitySpriteHeight()*0.25, dyn.getEntitySpriteWidth()*1.25, dyn.getEntitySpriteHeight()*1.25);
                gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.GPL), gplX, 10*dyn.getEntitySpriteHeight());

                if(gplX > dyn.getGameAreaWidth()+dyn.getEntitySpriteWidth()) { reset = true; }
            }
        };

        highscoreFailed = new HighscoreAnimation() {

            @Override
            public void renderNextFrame() {
                double speed = dyn.getBaseSpeed()*0.75;
                animation.update(false);
                if(reset) { gplX = -2*dyn.getHighscoreEntitySize(); playerX = -dyn.getHighscoreEntitySize(); ghostX = -dyn.getHighscoreEntitySize()*6; reset = false; }
                gplX+=speed; playerX+=speed; ghostX+=speed;

                for(EntityObjects o : EntityObjects.ghosts()) {
                    gc.drawImage(animation.forAnimationWithoutObject(o, Directions.RIGHT), ghostX, (9+o.ordinal()*2)*dyn.getHighscoreEntitySize());
                }
                gc.drawImage(animation.forAnimationWithoutObject(EntityObjects.PLAYER, Directions.RIGHT), playerX, 16*dyn.getHighscoreEntitySize()-dyn.getHighscoreEntitySize()*0.25, dyn.getHighscoreEntitySize()*1.25, dyn.getHighscoreEntitySize()*1.25);
                gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.GPL), gplX, 12*dyn.getHighscoreEntitySize());

                if(ghostX > dyn.getGameAreaResolutionHeight()+dyn.getHighscoreEntitySize()) { reset = true; }
            }
        };


        text = new AnimationToggle(dyn.getFpsCap());
        gameFont = Font.loadFont(getClass().getResourceAsStream("/de/hshannover/inform/gnuman/resources/css/fonts/game.ttf"), dyn.getUiFontSize());
        mediumFont = Font.loadFont(getClass().getResourceAsStream("/de/hshannover/inform/gnuman/resources/css/fonts/game.ttf"), dyn.getUiFontSize()*1.25);
        largeFont = Font.loadFont(getClass().getResourceAsStream("/de/hshannover/inform/gnuman/resources/css/fonts/game.ttf"), dyn.getUiFontSize()*1.5);
        smallFont = Font.loadFont(getClass().getResourceAsStream("/de/hshannover/inform/gnuman/resources/css/fonts/game.ttf"), dyn.getUiFontSize()*0.5);
        highscoreFailed.reset(); highscoreMade.reset();
    }

    /**
     * Execute rendering
     * @param map to render
     * @param entities to render
     * @param tracker game variables
     */
    public void render(TileMap map, Entities entities, GameVariableTracker tracker) {
        instruction.renderStatics(map, true);
        instruction.renderEntities(entities, map.getGhostWalls(), tracker.isFrightened());
        renderUI(tracker);
    }

    /**
     * Render the wait for next level screen.
     * @param map to render
     * @param entities to render
     * @param nextLevel magnitude of level
     */
    public void renderWaitForNextLevel(TileMap map, Entities entities, int nextLevel) {
        instruction.sharedStaticImage(map, true, entities, true);
        renderFlickerBottomText("SPACE FOR LEVEL " + nextLevel + "!");
        seperateUi();
    }

    /**
     * Render the wait for player screen.
     * @param map to render
     * @param entities to render
     * @param renderEntitiesAt timestamp of when to show the entities to the player
     */
    public void renderWaitForPlayer(TileMap map, Entities entities, long renderEntitiesAt) {
        instruction.sharedStaticImage(map, false, entities, System.currentTimeMillis() > renderEntitiesAt);
        renderFlickerBottomText("PRESS SPACE TO START THE GAME!");
        seperateUi();
    }

    /**
     * Render the you lost a life screen.
     * @param map to render
     * @param entities to render
     */
    public void renderLifeLost(TileMap map, Entities entities) {
        instruction.sharedStaticImage(map, false, entities, true);
        renderFlickerBottomText("LIFE LOST! SPACE TO RESUME!");
        seperateUi();
    }

    /**
     * Failed highscore message and story.
     * @param state of game.
     * @param tracker to get data.
     */
    public void failedHighscore(GameStates state, GameVariableTracker tracker) {
        highscoreRenderInit();
        renderFlickerBottomText("PRESS SPACE TO CONTINUE!", "PRESS ENTER TO PLAY AGAIN!");
        highscoreHeadline("NO HIGHSCORE!");
        highscoreScoreData(tracker);
        gc.setFill(Color.WHITE);
        gc.setFont(smallFont);
        if(state == GameStates.NO_HIGHSCORE_YOU_SUCK) {
            gc.fillText(String.format("It appears that you're simply not good enough :/", tracker.getScore()), dyn.getGameAreaResolutionWidth() / 2, 10*dyn.getHighscoreBlockSize());
            gc.fillText(String.format("But why don't you just try again?", tracker.getScore()), dyn.getGameAreaResolutionWidth() / 2, 11*dyn.getHighscoreBlockSize());
            gc.fillText(String.format("One day you might break the highscore :D", tracker.getScore()), dyn.getGameAreaResolutionWidth() / 2, 12*dyn.getHighscoreBlockSize());
            highscoreFailed.renderNextFrame();
            drawHighscoreFrame();
        } else {
            gc.fillText(String.format("Custom maps do not support highscore tracking!", tracker.getScore()), dyn.getGameAreaResolutionWidth() / 2, 10*dyn.getHighscoreBlockSize());
            gc.fillText(String.format("If you really want to break the highscore,", tracker.getScore()), dyn.getGameAreaResolutionWidth() / 2, 11*dyn.getHighscoreBlockSize());
            gc.fillText(String.format("simply play the default map!", tracker.getScore()), dyn.getGameAreaResolutionWidth() / 2, 12*dyn.getHighscoreBlockSize());
        }
    }

    /**
     * Made highscore screen and story.
     * @param tracker to get data.
     */
    public void madeHighscore(GameVariableTracker tracker) {
        highscoreRenderInit();
        renderFlickerBottomText("PRESS SPACE TO CONTINUE!");
        highscoreHeadline("HIGHSCORE MADE!");
        highscoreScoreData(tracker);
        highscoreMade.renderNextFrame();
        drawHighscoreFrame();
    }

    /**
     * Reset the frightening flash state so the frightening animation always starts at the same point.
     */
    public void resetFrighteningFlashState() {
        animation.resetFrighteningFlashState();
    }

    /**
     * Prepare screen before rendering highscore data
     */
    private void highscoreRenderInit() {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, dyn.getGameAreaWidth(), dyn.getGameAndUiResolutionHeight());
        gc.setFill(Constants.HIGHSCORE_CANVAS_FILL_COLOR);
    }

    /**
     * Draw the frame for the highscore screen
     */
    private void drawHighscoreFrame() {
        gc.setFill(Constants.HIGHSCORE_CANVAS_FILL_COLOR);
        gc.fillRect(2*dyn.getHighscoreBlockSize(), dyn.getHighscoreBlockSize()*2, dyn.getGameAreaResolutionWidth()-(4*dyn.getHighscoreBlockSize()), dyn.getHighscoreBlockSize()/2);
        gc.fillRect(2*dyn.getHighscoreBlockSize(), dyn.getGameAndUiResolutionHeight()-(2*dyn.getHighscoreBlockSize())-dyn.getHighscoreBlockSize()/2, dyn.getGameAreaResolutionWidth()-(4*dyn.getHighscoreBlockSize()), dyn.getHighscoreBlockSize()/2);
        gc.fillRect(dyn.getHighscoreBlockSize()*2, dyn.getHighscoreBlockSize()*2, dyn.getHighscoreBlockSize()/2, dyn.getGameAndUiResolutionHeight()-(4*dyn.getHighscoreBlockSize()));
        gc.fillRect(dyn.getHighscoreBlockSize()*2+dyn.getGameAreaResolutionWidth()-(4*dyn.getHighscoreBlockSize())-dyn.getHighscoreBlockSize()/2, dyn.getHighscoreBlockSize()*2, dyn.getHighscoreBlockSize()/2, dyn.getGameAndUiResolutionHeight()-(4*dyn.getHighscoreBlockSize()));
    }

    /**
     * Render a flickering bottom text.
     * @param s string to render at the bottom
     */
    private void renderFlickerBottomText(String s) {
        gc.setFont(gameFont);
        if(text.updateAndIsDisplay()) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFill(Color.WHITE);
            gc.fillText(s, dyn.getGameAreaResolutionWidth() / 2, dyn.getTextMarginVertical());
        }
    }

    /**
     * Render a flickering bottom text.
     * @param s string to render at the bottom
     * @param ss string to render at the bottom once the first one is not showing
     */
    private void renderFlickerBottomText(String s, String ss) {
        gc.setFont(gameFont);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(Color.WHITE);
        gc.fillText(text.updateAndIsDisplay() ? s : ss, dyn.getGameAreaResolutionWidth() / 2, dyn.getTextMarginVertical());
    }

    /**
     * Render a highscore headline
     * @param s headline
     */
    private void highscoreHeadline(String s) {
        gc.setFont(largeFont);
        gc.fillText(s, dyn.getGameAreaResolutionWidth() / 2, 4*dyn.getHighscoreBlockSize());
        gc.setFont(gameFont);
    }

    /**
     * Render score data
     * @param tracker to get data from
     */
    private void highscoreScoreData(GameVariableTracker tracker) {
        gc.setFill(Color.WHITE);
        gc.setFont(mediumFont);
        gc.fillText(String.format("Level %d @ %s", tracker.getLevel(), tracker.getDifficulty()), dyn.getGameAreaResolutionWidth() / 2, 6*dyn.getHighscoreBlockSize());
        gc.setFill(text.isInvertedDisplay() ? Constants.HIGHSCORE_CANVAS_FILL_COLOR : Color.WHITE);
        gc.fillText(String.format("%08d", tracker.getScore()), dyn.getGameAreaResolutionWidth() / 2, 8*dyn.getHighscoreBlockSize());
    }

    /**
     * Render UI
     * @param tracker game variables
     */
    private void renderUI(GameVariableTracker tracker) {
        gc.fillRect(0, dyn.getGameAreaResolutionHeight(), dyn.getGameAreaResolutionWidth(), dyn.getUiHeight());
        seperateUi();
        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.LEFT);
        for(int i = 0; i < tracker.getLifes(); i++) {
            gc.drawImage(textures.getTexture(ObjectTypes.OTHER, OtherObjects.LIFE), i*dyn.getUiLifeDimension() + dyn.getBlockWidth(), dyn.getUiLifeVerticalPosition());
        }
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText(String.format("%08d", tracker.getScore()), dyn.getTextMarginHorizontal(), dyn.getTextMarginVertical());
    }

    /**
     * Draws the ui seperator.
     */
    private void seperateUi() {
        gc.setFill(Constants.UI_BORDER);
        gc.fillRect(0, dyn.getGameAreaResolutionHeight()+1, dyn.getGameAreaResolutionWidth(), 2);
    }
}
