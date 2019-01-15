package de.hshannover.inform.gnuman.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.app.enums.UIStates;

/**
 * Controlling the OptionAudio screen.
 * @author Marc Herschel
 */

public class OptionsAudioController extends CommonController {
    @FXML Label baseLevelString;
    @FXML Button soundButton, musicButton;
    @FXML Slider audioBaseLevelSlider;

    private void updateMusicButton() {
        musicButton.setId(gameOptions.isMusicOn() ? Constants.OPTIONS_CSS_ENABLED :  Constants.OPTIONS_CSS_DISABLED);
        musicButton.setText(gameOptions.isMusicOn() ? "On" : "Off");
    }

    private void updateSoundButton() {
        soundButton.setId(gameOptions.isSoundOn() ? Constants.OPTIONS_CSS_ENABLED :  Constants.OPTIONS_CSS_DISABLED);
        soundButton.setText(gameOptions.isSoundOn() ? "On" : "Off");
    }

    @FXML
    private void initialize() {
        soundButton.setId(Constants.OPTIONS_CSS_ENABLED);
        musicButton.setId(Constants.OPTIONS_CSS_ENABLED);
        audioBaseLevelSlider.setValue(100);
        audioBaseLevelSlider.valueProperty().addListener(e -> {
            audioBaseLevelSlider.setValue(Math.round(audioBaseLevelSlider.getValue()));
            gameOptions.setBaseLevelMultiplicator((float) audioBaseLevelSlider.getValue()/100);
            baseLevelString.setText(String.format("Audio Base Level @ %d%%", (int) audioBaseLevelSlider.getValue()));
            GameLauncher.am().adjustAll(gameOptions.getBaseLevelMultiplicator());
        });
    }

    @FXML
    private void toggleMusic() {
        gameOptions.toggleMusic();
        updateMusicButton();
        GameLauncher.am().toggleMainTheme();
    }

    @FXML
    private void toggleSound() {
        gameOptions.toggleSound();
        updateSoundButton();
    }

    @FXML
    private void goBackToOptions() {
        manager.switchScene(UIStates.OPTIONS);
    }
}
