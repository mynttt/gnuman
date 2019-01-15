package de.hshannover.inform.gnuman.app;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.abstracts.LoopInstruction;
import de.hshannover.inform.gnuman.app.enums.Difficulty;
import de.hshannover.inform.gnuman.app.enums.GameStates;
import de.hshannover.inform.gnuman.app.model.storage.DynamicVariables;
import de.hshannover.inform.gnuman.app.model.storage.GameSettings;
import de.hshannover.inform.gnuman.app.model.storage.MapData;
import de.hshannover.inform.gnuman.app.modules.GameLoop;
import de.hshannover.inform.gnuman.gui.GameWindowController;

/**
 * Guarding the game instance.
 * @author Marc Herschel
 */

public class GameSupervisor {
    private GameWindowController controller;
    private Canvas gameCanvas;
    private GameInstance gameInstance;
    private GameLoop gameLoop;
    private DynamicVariables dyn;
    private GameStates state, stateBeforePause;
    private boolean recordFps;
    private long pausedAt, renderEntitiesAt;

    /**
     * Construct the supervisor.
     * @param controller Controller of the game window.
     * @param rootPane BorderPane to initiate the game in.
     */
    public GameSupervisor(GameWindowController controller, BorderPane rootPane) {
        this.controller = controller;
        this.gameCanvas = new Canvas(0, 0);
        this.state = GameStates.WAIT_FOR_PLAYER;
        this.dyn = new DynamicVariables();
        this.gameInstance = new GameInstance(gameCanvas.getGraphicsContext2D(), dyn);
        this.recordFps = false;
        rootPane.setCenter(gameCanvas);
        initiateGameLoop();
        Log.info(getClass().getSimpleName(), "Game initiated successfully.");
    }

    /**
     * Creates the game loop with 60fps and no FPS recording and passes a loop instruction handling the game events.
     */
    private void initiateGameLoop() {
        gameLoop = new GameLoop(new LoopInstruction() {

            @Override
            public void handle(long now) {
                if(state != GameStates.RUNNING) { gameInstance.renderSpecial(state, renderEntitiesAt); return; }
                gameInstance.updateGame();
                gameInstance.render();
                if(recordFps) { GameLauncher.setFPS(gameLoop.getFramerate()); }
                if(gameInstance.isFinished()) {
                    GameLauncher.am().stopGameMusic();
                    switch(gameInstance.endedState()) {
                    case GAME_LOST:
                        GameLauncher.am().playSound("DIE");
                        GameLauncher.am().startUiMusic();
                        if(!gameInstance.trackHighscore()) { state = GameStates.NO_HIGHSCORE_CUSTOM_MAP; return; }
                        state = (madeHighscore()) ? GameStates.MADE_HIGHSCORE : GameStates.NO_HIGHSCORE_YOU_SUCK;
                        break;
                    case NEXT_LEVEL:
                        GameLauncher.am().playSound("LEVEL_FINISHED");
                        state = GameStates.WAIT_NEXT_LEVEL;
                        break;
                    case LIFE_LOST:
                        GameLauncher.am().playSound("DIE");
                        state = GameStates.LIFE_LOST;
                        break;
                    default:
                        state = GameStates.UNDEFINED;
                        break;
                    }
                }
            }

        }, 60, false);
        Log.info(getClass().getSimpleName(), "Gameloop initiated successfully.");
    }

    /**
     * Manage switching to the pause menu.
     * @return true if pause possible
     */
    public boolean pauseGame() {
        if(state == GameStates.WAIT_FOR_PLAYER || state == GameStates.WAIT_NEXT_LEVEL || state == GameStates.LIFE_LOST) return true;
        if(state == GameStates.RUNNING) {
            gameLoop.stop();
            stateBeforePause = state;
            state = GameStates.PAUSED;
            pausedAt = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Manage resumption from the pause menu.
     */
    public void resumeGame() {
        if(state == GameStates.PAUSED) {
            gameInstance.evaluateGameMusic();
            gameInstance.adjustDeltaTime(pausedAt);
            renderEntitiesAt +=System.currentTimeMillis()-pausedAt;
            state = stateBeforePause;
            gameLoop.start();
        }
    }

    /**
     * Manage a total reset from the pause menu.
     */
    public void resetGame() {
        gameLoop.stop();
        gameInstance.resetGame();
        state = GameStates.WAIT_FOR_PLAYER;
        gameLoop.start();
        renderEntitiesAt = System.currentTimeMillis() + 4000;
    }

    /**
     * Start a new game with a given map, difficulty and options.
     * @param mapData data to load.
     * @param rootPane pane to resize if needed.
     * @param difficulty of the game.
     * @param options to take into consideration.
     */
    public void startGame(MapData mapData, BorderPane rootPane, Difficulty difficulty, GameSettings options) {
        Log.info(getClass().getSimpleName(), "Refreshing block dependent values and loading map.");
        recordFps = options.trackFps();
        gameLoop.changeSettings(options.getFps(), recordFps);
        state = GameStates.WAIT_FOR_PLAYER;
        dyn.newSession(mapData.getWidthInBlocks(), mapData.getHeightInBlocks(), options.getBlockDimension(), options.getBlockDimension(), options.getFps(), difficulty, options.dynCamera());
        gameCanvas.setWidth(dyn.getGameAreaResolutionWidth());
        gameCanvas.setHeight(dyn.getGameAndUiResolutionHeight());
        rootPane.setPrefWidth(dyn.getGameAreaResolutionWidth());
        rootPane.setPrefHeight(dyn.getGameAndUiResolutionHeight());
        gameInstance.startGame(mapData, difficulty, options);
        renderEntitiesAt = System.currentTimeMillis() + 4000;
        gameLoop.start();
    }

    /**
     * Unload map and free the resources.
     */
    public void shutdownGame() {
        state = GameStates.UNDEFINED;
        gameLoop.stop();
        gameInstance.shutdownGame();
    }

    /**
     * Update player input.
     * @param code to check for.
     */
    public void updatePlayerInputBuffer(KeyCode code) {
        if(state != GameStates.RUNNING) { return; }
        gameInstance.updatePlayerInputBuffer(code);
    }

    /**
     * Tell the game the player has instructed a ready command.
     */
    public void playerIsReady() {
        if(state == GameStates.MADE_HIGHSCORE || state == GameStates.NO_HIGHSCORE_CUSTOM_MAP || state == GameStates.NO_HIGHSCORE_YOU_SUCK) {
            executeHighscoreSwitch();
            return;
        }
        if(state == GameStates.LIFE_LOST) { gameInstance.prepareLevelAfterLifeLoss(); state = GameStates.RUNNING; }
        if(state == GameStates.WAIT_NEXT_LEVEL) { gameInstance.prepareNextLevel(); state = GameStates.RUNNING; }
        if(state == GameStates.WAIT_FOR_PLAYER) {
            state = GameStates.RUNNING;
            GameLauncher.am().stopMusic("STARTING_MUSIC");
            GameLauncher.am().decideMusic(false, false);
        }
    }

    /**
     * Evaluate if we made a new record or get the "you tried XD" screen.
     * @return true if made highscore
     */
    public boolean madeHighscore() {
        return controller.madeHighscore(gameInstance.getDifficulty(), gameInstance.getScore());
    }

    /**
     * Switch screen accordingly.
     */
    public void executeHighscoreSwitch() {
        controller.manageHighscoreEvent(state == GameStates.MADE_HIGHSCORE);
    }

    /**
     * Trigger a reset from the highscore screen.
     */
    public void replayGame() {
        if(state == GameStates.NO_HIGHSCORE_YOU_SUCK || state == GameStates.NO_HIGHSCORE_CUSTOM_MAP) {
            gameInstance.resetGame();
            state = GameStates.WAIT_FOR_PLAYER;
            GameLauncher.am().stopUiMusic();
        }
    }

    /**
     * @return Dynamic blocksize object for pause menu.
     */
    public DynamicVariables getDynamic() {
        return dyn;
    }

}
