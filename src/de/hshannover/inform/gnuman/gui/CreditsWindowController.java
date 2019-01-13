package de.hshannover.inform.gnuman.gui;

import de.hshannover.inform.gnuman.app.enums.UIStates;
import de.hshannover.inform.gnuman.app.mapeditor.Dialogs;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

/**
 * Controlling the Credits screen.
 * @author Marc Herschel
 */

public class CreditsWindowController extends CommonController {
    @FXML AnchorPane trigger;

    @FXML
    private void initialize() {
        trigger.setOnMouseClicked(e -> {
            manager.switchScene(UIStates.LECTURE);
            ((LectureWindowController) manager.getController(UIStates.LECTURE)).interject();
        });
    }

    @FXML private void backToExtras() { manager.switchScene(UIStates.EXTRAS); }
    @FXML private void gitlab() { Dialogs.openUrl("https://gitlab.com/mynttt/gnuman"); }
    @FXML private void github() { Dialogs.openUrl("https://github.com/mynttt/gnuman"); }

}
