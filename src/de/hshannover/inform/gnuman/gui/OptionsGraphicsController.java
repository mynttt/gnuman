package de.hshannover.inform.gnuman.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import de.hshannover.inform.gnuman.Constants;
import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.app.enums.UIStates;

/**
 * Controlling the OptionGraphics screen.
 * @author Marc Herschel
 */

public class OptionsGraphicsController extends CommonController {
    @FXML Button fpsTrackingButton, fpsButton, dynCameraButton;
    @FXML Slider blockDimensionSlider;
    @FXML Label resolutionInfo;

    @FXML
    private void initialize() {
        fpsTrackingButton.setId(Constants.OPTIONS_CSS_DISABLED);
        fpsButton.setId(Constants.OPTIONS_CSS_ENABLED);
        dynCameraButton.setId(Constants.OPTIONS_CSS_DISABLED);
        blockDimensionSlider.setValue(Constants.DEFAULT_BLOCK_DIMENSIONS);
        blockDimensionSlider.valueProperty().addListener(e -> {
            blockDimensionSlider.setValue(Math.round(blockDimensionSlider.getValue()));
            triggerDimensionUpdate((int) blockDimensionSlider.getValue());
        });
    }

    /**
     * Update slider text.
     * @param dimension to validate.
     */
    public void triggerDimensionUpdate(int dimension) {
        gameOptions.setBlockDimension(dimension);
        resolutionInfo.setText("Blocksize set to " + gameOptions.getBlockDimension() + "px");
    }

    /**
     * Update every time we visit the menu. The game might have changed the size to render everything properly.
     */
    public void updateOnVisit() {
        triggerDimensionUpdate(gameOptions.getBlockDimension());
        blockDimensionSlider.setValue(gameOptions.getBlockDimension());
    }

    @FXML
    private void toggleDynCamera() {
        gameOptions.toggleDynamicCamera();
        dynCameraButton.setId(gameOptions.dynCamera() ? Constants.OPTIONS_CSS_ENABLED : Constants.OPTIONS_CSS_DISABLED);
        dynCameraButton.setText(gameOptions.dynCamera() ? "YES" : "NO");
    }

    @FXML
    private void toggleFpsTracking() {
        if(gameOptions.trackFps()) { GameLauncher.disableFPSTracking(); }
        gameOptions.toggleFpsTracking();
        fpsTrackingButton.setId(gameOptions.trackFps() ? Constants.OPTIONS_CSS_ENABLED : Constants.OPTIONS_CSS_DISABLED);
        fpsTrackingButton.setText(gameOptions.trackFps() ? "YES" : "NO");
    }

    @FXML
    private void cycleFps() {
        gameOptions.cycleFps();
        fpsButton.setText(Integer.toString(gameOptions.getFps()));
    }

    @FXML
    private void goBackToOptions() {
        manager.switchScene(UIStates.OPTIONS);
    }
}
