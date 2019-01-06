package de.hshannover.inform.gnuman.gui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.app.AudioManager;
import de.hshannover.inform.gnuman.app.enums.UIStates;

/**
 * Controlling the pause screen.
 * @author Marc Herschel
 */

public class PauseWindowController extends CommonController {
    @FXML AnchorPane rootPane;

    /**
     * Call this after the scene manager finished loading the scenes.<br>
     * This will set the onKeyPressed event to the scene root.
     */
    public void setKeyBinds() {
        manager.getScene(UIStates.PAUSED).getRoot().setOnKeyPressed(e -> { if(e.getCode() == KeyCode.ESCAPE ) { continueGame(); } });
        manager.getScene(UIStates.PAUSED).getRoot().requestFocus();
    }

    /**
     * Calculate dimensions for the pause window (this depends on the game resolution).
     * The magic numbers are percentage values representing the scale @ 25,25 blocksize.
     * @param width width of game.
     * @param height height including UI elements.
     * @param heightNoUi height without UI elements (game height).
     */
    protected void recalculateDimensions(int width, int height, int heightNoUi) {
        rootPane.setPrefSize(width, height);
        VBox buttons = (VBox) rootPane.getChildren().get(0);
        AnchorPane overlay = (AnchorPane) rootPane.getChildren().get(1);
        overlay.setLayoutX(width*0.16);
        overlay.setLayoutY(heightNoUi*0.15);
        overlay.setPrefSize(width*0.714, heightNoUi*0.232);
        buttons.setLayoutX(width*0.285);
        buttons.setLayoutY(heightNoUi*0.5);
        buttons.setPrefSize(width*0.428, heightNoUi*0.29);
        int padding1 = (int) Math.round(0.24*heightNoUi*0.052);
        int padding2 = (int) Math.round(0.6*heightNoUi*0.052);
        String cssPadding = "-fx-padding: " + padding1 + " " + padding2 + " " + padding1 + " " + padding2 + ";";
        String cssFont = "-fx-font-size: " + (int) Math.ceil(0.5*0.052*heightNoUi) + "px;";
        Insets margin = new Insets((int) Math.round(0.47*heightNoUi*0.052), 0, 0, 0);
        for(Node n : buttons.getChildren()) {
             VBox.setMargin(n, margin);
             ((Button) n).setPrefSize(width*0.428, heightNoUi*0.052);
             ((Button) n).setStyle(cssPadding + cssFont);
        }
    }

    @FXML
    private void continueGame() {
        ((GameWindowController) manager.getController(UIStates.GAME_WINDOW)).resumeFromPauseMenu();
        manager.switchScene(UIStates.GAME_WINDOW);
    }

    @FXML
    private void restartGame() {
        ((GameWindowController) manager.getController(UIStates.GAME_WINDOW)).resetGameFromPauseMenu();
        manager.switchScene(UIStates.GAME_WINDOW);
    }

    @FXML
    private void backToMenu() {
        manager.switchScene(UIStates.MAIN_MENU);
        GameLauncher.centerStage();
        AudioManager.startUiMusic();
        ((GameWindowController) manager.getController(UIStates.GAME_WINDOW)).shutdownGame();
    }
}
