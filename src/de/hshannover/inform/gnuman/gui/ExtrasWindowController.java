package de.hshannover.inform.gnuman.gui;

import java.io.IOException;
import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.app.enums.UIStates;
import de.hshannover.inform.gnuman.app.mapeditor.StartEditor;
import javafx.fxml.FXML;

/**
 * Controlling the Help screen.
 * @author Marc Herschel
 */

public class ExtrasWindowController extends CommonController {

    @FXML
    private void backToMenu() {
        manager.switchScene(UIStates.MAIN_MENU);
    }

    @FXML
    private void credits() {
        manager.switchScene(UIStates.CREDITS);
    }

    @FXML
    private void mapEditor() {
        try {
            GameLauncher.hide();
            GameLauncher.am().stopEntireAudio();
            StartEditor s = new StartEditor();
            s.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHelp() {
        ((HelpWindowController) manager.getController(UIStates.HELP)).startAnimation();
        manager.switchScene(UIStates.HELP);
    }
}
