package de.hshannover.inform.gnuman.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.AudioManager;
import de.hshannover.inform.gnuman.app.enums.Difficulty;
import de.hshannover.inform.gnuman.app.enums.UIStates;
import de.hshannover.inform.gnuman.app.mapeditor.Dialogs;
import de.hshannover.inform.gnuman.app.model.storage.MapData;
import de.hshannover.inform.gnuman.app.util.Helper;
import de.hshannover.inform.gnuman.app.util.MapParser;
import de.hshannover.inform.gnuman.app.util.ParsingReply;

/**
 * Controlling the NewGame screen.
 * @author Marc Herschel
 */

public class NewGameWindowController extends CommonController {
    @FXML Button easy, medium, hard;
    ArrayList<Button> buttons;
    Difficulty difficulty;

    @FXML
    private void initialize() {
        difficulty = Difficulty.NORMAL;
        medium.setId("difficulty-selected");
        buttons = new ArrayList<>(); buttons.add(easy); buttons.add(medium); buttons.add(hard);
    }

    @FXML
    private void setEasy() { setDifficulty(easy, Difficulty.SLOW); }
    @FXML
    private void setMedium() { setDifficulty(medium, Difficulty.NORMAL); }
    @FXML
    private void setHard() { setDifficulty(hard, Difficulty.FAST); }
    @FXML
    private void switchToMenu() {  manager.switchScene(UIStates.MAIN_MENU); }

    @FXML
    private void startCustom() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open GNUMAN map");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Gnuman Map", "*.gnuman"), new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(GameLauncher.getStage());
        if(selectedFile != null) {
            MapData map = null;
            try {
                Log.info("Helper", "Parsing external map: " + selectedFile.getAbsolutePath());
                ParsingReply r = MapParser.parseMap(selectedFile.getAbsolutePath(), false);
                if(r.status().isSuccess()) {
                    map = r.data();
                    Log.info("Helper", "Parsed and loaded external map: " + map);
                } else {
                    Log.warning("Helper", "Fatal error while parsing external map: Corrupt Map -> " + r.status().statusMessage());
                    Dialogs.dialog(AlertType.ERROR, "Corrupt map.", "This GNUMAN map is corrupt.\n\n" + r.status().statusMessage());
                    Log.info("Helper", "Gnuman recovered after corrupt map.");
                    return;
                }
            } catch (Exception e) {
                Log.warning("Helper", "Fatal error while parsing external map: Wrong Format -> " + Helper.stackTraceToString(e));
                Dialogs.exceptionDialog(e, "Garbage in, Garbage out.", "In computer science, garbage in, garbage out (GIGO) describes the concept that flawed, or nonsense input data produces nonsense output or \"garbage\". \n\nTo make it short: Is this a valid GNUMAN map?");
                Log.info("Helper", "Gnuman recovered after garbage data.");
                return;
            }
            if(!gameOptions.dynCamera()) {
                int width = Toolkit.getDefaultToolkit().getScreenSize().width;
                int height = Toolkit.getDefaultToolkit().getScreenSize().height;
                int maxDimension = Helper.calculateMaxBlockDimensionsForCustomMap(width, height, map.getWidthInBlocks(), map.getHeightInBlocks());
                if(gameOptions.getBlockDimension() > maxDimension) { gameOptions.setBlockDimension(maxDimension); }
            }
            AudioManager.stopUiMusic();
            ((GameWindowController) manager.getController(UIStates.GAME_WINDOW)).startCustomMapSession(map, difficulty, gameOptions);
            manager.switchScene(UIStates.GAME_WINDOW);
            GameLauncher.centerStage();
        }
    }

    @FXML
    private void startDefault() {
        if(!gameOptions.dynCamera()) {
            int width = Toolkit.getDefaultToolkit().getScreenSize().width;
            int height = Toolkit.getDefaultToolkit().getScreenSize().height;
            int maxDimension = Helper.calculateMaxBlockDimensionForDefaultMap(width, height);
            if(gameOptions.getBlockDimension() > maxDimension) { gameOptions.setBlockDimension(maxDimension); }
        }
        AudioManager.stopUiMusic();
        ((GameWindowController) manager.getController(UIStates.GAME_WINDOW)).startGameSession(difficulty, gameOptions);
        manager.switchScene(UIStates.GAME_WINDOW);
        GameLauncher.centerStage();
    }

    private void setDifficulty(Button button, Difficulty difficulty) {
        buttons.forEach(b -> b.setId("button-menu"));
        button.setId("difficulty-selected");
        this.difficulty = difficulty;
    }

}
