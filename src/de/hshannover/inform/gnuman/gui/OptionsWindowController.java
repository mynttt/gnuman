package de.hshannover.inform.gnuman.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.app.enums.UIStates;

/**
 * Controlling the OptionWindow screen.
 * @author Marc Herschel
 */

public class OptionsWindowController extends CommonController {
    @FXML Button controlButton;

    private void updateControlButton() {
        controlButton.setId(gameOptions.isUseArrows() ? Constants.OPTIONS_CSS_DISABLED : Constants.OPTIONS_CSS_ENABLED);
        controlButton.setText(gameOptions.isUseArrows() ? "Arrows" : "WASD");
    }

    @FXML
    private void initialize() {
        controlButton.setId(Constants.OPTIONS_CSS_DISABLED);
    }

    @FXML
    private void toggleControls() {
        gameOptions.toggleControls();
        updateControlButton();
    }

    @FXML
    private void goToGraphicSettings() {
        manager.switchScene(UIStates.OPTIONS_GRAPHIC);
    }

    @FXML
    private void goToAudioSettings() {
        manager.switchScene(UIStates.OPTIONS_AUDIO);
    }

    @FXML
    private void goBackToMenu() {
        manager.switchScene(UIStates.MAIN_MENU);
    }
}
