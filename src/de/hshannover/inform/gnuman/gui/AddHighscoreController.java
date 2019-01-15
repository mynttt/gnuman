package de.hshannover.inform.gnuman.gui;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.app.enums.Difficulty;
import de.hshannover.inform.gnuman.app.enums.UIStates;
import de.hshannover.inform.gnuman.gui.components.Highscore;

/**
 * Controlling the AddHighscore screen.
 * @author Marc Herschel
 */

public class AddHighscoreController extends CommonController {
    @FXML TextField highscoreNameField;
    @FXML Label rank, points, difficulty;
    Highscore highscore;
    Difficulty gameDifficulty;
    int score;

    /**
     * Update the GUI fields with data collected by the game tracker.
     * @param gameDifficulty Difficulty
     * @param score Score reached
     */
    public void setScoreData(Difficulty gameDifficulty, int score) {
        this.gameDifficulty = gameDifficulty;
        this.score = score;
        rank.setText(Integer.toString(highscore.getHighscoreRank(gameDifficulty, score)));
        points.setText(Integer.toString(score));
        difficulty.setText(gameDifficulty.toString());
    }

    /**
     * Retrieve the highscore object from the HighscoreWindowController, set bind for entering scores with enter.
     */
    public void initializeAddHighscore() {
        highscore = ((HighscoreWindowController) manager.getController(UIStates.HIGHSCORE)).getHighscore();
        manager.getScene(UIStates.ADD_HIGHSCORE).getRoot().setOnKeyPressed(e -> { if(e.getCode() == KeyCode.ENTER) { executeSaveHighscore(); } });
        manager.getScene(UIStates.ADD_HIGHSCORE).getRoot().requestFocus();
    }

    @FXML
    private void initialize() {
        highscoreNameField.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, Event::consume);
        highscoreNameField.textProperty().addListener(e -> {
            if(highscoreNameField.getText().length() > Constants.HIGHSCORE_NAME_MAX_LENGTH) {
                String s = highscoreNameField.getText().substring(0, Constants.HIGHSCORE_NAME_MAX_LENGTH);
                highscoreNameField.setText(s);
            }
        });
    }

    @FXML
    private void saveHighscore() {
        executeSaveHighscore();
    }

    private void executeSaveHighscore() {
        String name = highscoreNameField.getText().trim();
        if(name.length() == 0) { name = Constants.HIGHSCORE_UNKNOWN_PLAYER; }
        highscore.addToHighscore(gameDifficulty, score, name);
        highscore.saveHighscore();
        highscoreNameField.clear();
        GameLauncher.am().playSound("ADD_HIGHSCORE");
        ((HighscoreWindowController) manager.getController(UIStates.HIGHSCORE)).resetToFirstValue();
        manager.switchScene(UIStates.MAIN_MENU);
        gameDifficulty = null;
    }
}
