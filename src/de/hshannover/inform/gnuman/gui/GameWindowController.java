package de.hshannover.inform.gnuman.gui;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.GameSupervisor;
import de.hshannover.inform.gnuman.app.enums.Difficulty;
import de.hshannover.inform.gnuman.app.enums.UIStates;
import de.hshannover.inform.gnuman.app.model.storage.GameSettings;
import de.hshannover.inform.gnuman.app.model.storage.MapData;
import de.hshannover.inform.gnuman.app.util.Helper;
import de.hshannover.inform.gnuman.app.util.MapParser;
import de.hshannover.inform.gnuman.app.util.ParsingReply;

/**
 * Controlling the GameWindow screen.
 * @author Marc Herschel
 */

public class GameWindowController extends CommonController {
    @FXML BorderPane rootPane;
    GameSupervisor supervisor;
    MapData defaultMap;

    /**
     * Call this after the scene manager finished loading the scenes to set up game and cache the default map.
     */
    public void initializeGame() {
        supervisor = new GameSupervisor(this, rootPane);
        Log.info("Helper", "Parsing internal map: " + Constants.FILE_MAP);
        try {
            ParsingReply r = MapParser.parseMap(Constants.FILE_MAP, true);
            defaultMap = r.data();
            if(!r.status().isSuccess()) { throw new RuntimeException(r.status().statusMessage()); }
        } catch(Exception e) {
            Log.critical("Helper", "Fatal error while parsing. Aborting execution now." + Helper.stackTraceToString(e));
            Helper.exitOnCritical();
        }
        Log.info("Helper", "Parsed and loaded map: " + defaultMap);
        manager.getScene(UIStates.GAME_WINDOW).getRoot().setOnKeyPressed(e -> {
            switch(e.getCode()) {
                case ESCAPE: switchToPauseMenu(); break;
                case SPACE: supervisor.playerIsReady(); break;
                case ENTER: supervisor.replayGame();
                default: supervisor.updatePlayerInputBuffer(e.getCode()); break;
            }
        });
        manager.getScene(UIStates.GAME_WINDOW).getRoot().requestFocus();
    }

    /**
     * If we want to load a custom map.
     * @param map map to load.
     * @param difficulty of the game.
     * @param options to take into consideration.
     */
    public void startCustomMapSession(MapData map, Difficulty difficulty, GameSettings options) {
        Log.info(getClass().getSimpleName(), "Starting custom map: " + map.getName() + " by " + map.getAuthor());
        supervisor.startGame(map, rootPane, difficulty, options);
        adjustPauseWindow();
    }

    /**
     * Start game with the default map.
     * @param difficulty of the game.
     * @param options to take into consideration.
     */
    public void startGameSession(Difficulty difficulty, GameSettings options) {
        supervisor.startGame(defaultMap, rootPane, difficulty, options);
        adjustPauseWindow();
    }

    /**
     * Check if we made highscore and set data in case we made it.
     * @param difficulty of the game
     * @param score final score
     * @return true if in highscore
     */
    public boolean madeHighscore(Difficulty difficulty, int score) {
        ((AddHighscoreController) manager.getController(UIStates.ADD_HIGHSCORE)).setScoreData(difficulty, score);
        return score != 0 && ((HighscoreWindowController) manager.getController(UIStates.HIGHSCORE)).getHighscore().madeHighscore(difficulty, score);
    }

    /**
     * Switch to highscore if highscore was made
     * @param madeHighscore true if highscore was made
     */
    public void manageHighscoreEvent(boolean madeHighscore) {
        if(madeHighscore) {
            GameLauncher.am().playSound("CLICK");
            manager.switchScene(UIStates.ADD_HIGHSCORE);
        } else {
            GameLauncher.am().playSound("ADD_HIGHSCORE");
            manager.switchScene(UIStates.MAIN_MENU);
        }
        GameLauncher.centerStage();
        shutdownGame();
    }

    protected void shutdownGame() {
        supervisor.shutdownGame();
    }

    protected void resumeFromPauseMenu() {
        supervisor.resumeGame();
    }

    protected void resetGameFromPauseMenu() {
        supervisor.resetGame();
    }

    private void switchToPauseMenu() {
        if(supervisor.pauseGame()) {
            GameLauncher.am().stopEntireAudio();
            GameLauncher.am().playSound("CLICK");
            manager.switchScene(UIStates.PAUSED);
        }
    }

    private void adjustPauseWindow() {
        ((PauseWindowController) manager.getController(UIStates.PAUSED)).recalculateDimensions(
                supervisor.getDynamic().getGameAreaResolutionWidth(),
                (int) supervisor.getDynamic().getGameAndUiResolutionHeight(),
                supervisor.getDynamic().getGameAreaResolutionHeight());
    }
}
