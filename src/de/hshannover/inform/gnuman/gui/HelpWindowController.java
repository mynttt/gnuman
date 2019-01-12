package de.hshannover.inform.gnuman.gui;

import de.hshannover.inform.gnuman.Log;
import de.hshannover.inform.gnuman.app.abstracts.LoopInstruction;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.UIStates;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.model.animation.EntityAnimation;
import de.hshannover.inform.gnuman.app.modules.GameLoop;
import de.hshannover.inform.gnuman.app.modules.Textures;
import de.hshannover.inform.gnuman.app.util.Helper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;


/**
 * Controlling the extras screen.
 * @author Marc Herschel
 */

public class HelpWindowController extends CommonController {
    @FXML AnchorPane togglePane;
    @FXML Button toggle;
    @FXML ImageView inky, pinky, clyde, blinky;
    EntityAnimation animation;
    GameLoop animationLoop;
    int toggleIndex, ticks;

    @FXML
    private void initialize() {
        toggle.setText((toggleIndex+1)+"/"+togglePane.getChildren().size() + " Pages");
        try {
            animation = Textures.createEntityAnimation(60, 264, 264);
        } catch(Exception e) {
            Log.critical(getClass().getSimpleName(), "Failed to initiate animations for help:" + Helper.stackTraceToString(e));
            Helper.exitOnCritical();
        }
        animationLoop = new GameLoop(new LoopInstruction() {

            @Override
            public void handle(long now) {
                animation.update(true);
                Directions d = ticks < 300 ? Directions.RIGHT : ticks < 600 ? Directions.DOWN : ticks < 900 ? Directions.LEFT : Directions.DOWN;
                pinky.setImage(animation.forAnimationWithoutObject(EntityObjects.PINKY, d));
                clyde.setImage(animation.forAnimationWithoutObject(EntityObjects.CLYDE, d));
                blinky.setImage(animation.forAnimationWithoutObject(EntityObjects.BLINKY, d));
                inky.setImage(animation.forAnimationWithoutObject(EntityObjects.INKY, d));
                ticks = ++ticks % 1200;
            }

        }, 60, false);

    }

    @FXML
    void startAnimation() {
        animationLoop.start();
    }

    @FXML
    private void backToExtras() {
        manager.switchScene(UIStates.EXTRAS);
        animationLoop.stop();
        togglePane.getChildren().forEach(n -> n.setVisible(false));
        toggleIndex = 0;
        toggle.setText((toggleIndex+1)+"/"+togglePane.getChildren().size() + " Pages");
        togglePane.getChildren().get(0).setVisible(true);
    }

    @FXML
    private void togglePage() {
        int disable = toggleIndex%togglePane.getChildren().size();
        toggleIndex = ++toggleIndex%togglePane.getChildren().size();
        togglePane.getChildren().get(disable).setVisible(false);
        togglePane.getChildren().get(toggleIndex).setVisible(true);
        toggle.setText((toggleIndex+1)+"/"+togglePane.getChildren().size() + " Pages");
    }
}
