package de.hshannover.inform.gnuman.gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.app.enums.Difficulty;
import de.hshannover.inform.gnuman.app.enums.UIStates;
import de.hshannover.inform.gnuman.gui.components.Highscore;

/**
 * Controlling the HighscoreWindow screen.
 * @author Marc Herschel
 */

public class HighscoreWindowController extends CommonController {
    @FXML HBox highscoreBox;
    @FXML Button showNextLevel;
    Highscore highscore;
    int categoryCounter;

    public Highscore getHighscore() {
        return highscore;
    }

    /**
     * Reset highscore to first difficulty.
     */
    public void resetToFirstValue() {
        highscore.setState(Difficulty.values()[0]);
        categoryCounter = 0;
        showNextLevel.setText("SHOW " + Difficulty.values()[1].toString());
    }

    @FXML
    private void initialize() {
        highscore = new Highscore(Constants.HIGHSCORE_MAX_PLAYERS_TO_DISPLAY, Difficulty.SLOW);
        showNextLevel.setText("SHOW " + Difficulty.values()[categoryCounter+1%Difficulty.values().length].toString());
        highscoreBox.setAlignment(Pos.CENTER);
        highscoreBox.getChildren().add(highscore.getBox());
        if(!highscore.isEmpty()) { highscore.saveHighscore(); }
    }

    @FXML
    private void showNextDifficultyLevel() {
        categoryCounter++;
        showNextLevel.setText("SHOW " + Difficulty.values()[(categoryCounter+1)%Difficulty.values().length]);
        highscore.setState(Difficulty.values()[categoryCounter%Difficulty.values().length]);
    }

    @FXML
    private void goToMenu() {
        manager.switchScene(UIStates.MAIN_MENU);
    }
}
