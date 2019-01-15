package de.hshannover.inform.gnuman;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import de.hshannover.inform.gnuman.app.AudioManager;
import de.hshannover.inform.gnuman.app.abstracts.LoopInstruction;
import de.hshannover.inform.gnuman.app.enums.UIStates;
import de.hshannover.inform.gnuman.app.model.storage.GameSettings;
import de.hshannover.inform.gnuman.app.modules.GameLoop;
import de.hshannover.inform.gnuman.app.util.Helper;

/**
 * Launch the game
 * @author Marc Herschel
 */

public class GameLauncher extends Application {
    private static AudioManager audioManager;
    private static Stage primaryStage = null;
    private boolean skipped;
    private GameLoop g;

    /*
     * Hack to fix tooltip time being to long in JavaFX 8. This will affect all tooltips at runtime.
     * https://stackoverflow.com/a/43291239/7648364
     */
    static {
        try {
            Tooltip obj = new Tooltip();
            Class<?> clazz = obj.getClass().getDeclaredClasses()[0];
            Constructor<?> constructor = clazz.getDeclaredConstructor(
                    Duration.class,
                    Duration.class,
                    Duration.class,
                    boolean.class);
            constructor.setAccessible(true);
            Object tooltipBehavior = constructor.newInstance(
                    new Duration(150),  //open
                    new Duration(5000), //visible
                    new Duration(200),  //close
                    false);
            Field fieldBehavior = obj.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            fieldBehavior.set(obj, tooltipBehavior);
        }
        catch (Exception e) {}
    }

    @Override
    public void start(Stage primaryStage) {
        GameLauncher.primaryStage = primaryStage;

        if(Constants.DEBUG_LOG) {
            if(Log.hasBeenBootstrapped()) {
                Log.logger().reset();
            } else {
                Log.bootstrapLogger(Constants.DEBUG_LOG_FILE, Constants.DEBUG_LEVEL_MIN, Constants.DEBUG_LOG_TIME_FORMAT);
            }
            Log.info(getClass().getSimpleName(), "Starting GNUMAN...");
        }

        primaryStage.getIcons().clear();
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream(Constants.GAME_ICON)));
        primaryStage.setTitle("GNUMAN");

        Canvas c = new Canvas(875, 600);
        Scene disclaimer = new Scene(new AnchorPane(c));
        c.getGraphicsContext2D().setFill(Color.BLACK);
        c.getGraphicsContext2D().drawImage(new Image(getClass().getResourceAsStream("/de/hshannover/inform/gnuman/resources/data/disclaimer.png")), 0, 0);

        disclaimer.getRoot().setOnKeyPressed(e -> {
            if(skipped) { return; } mainMenu(); skipped = true;
        });
        disclaimer.getRoot().setOnMouseClicked(e -> {
            if(skipped) { return; } mainMenu(); skipped = true;
        });
        disclaimer.getRoot().requestFocus();

        g = new GameLoop(new LoopInstruction() {
            int loop;

            @Override
            public void handle(long now) {
                if(!skipped && ++loop>5) { skipped = true; mainMenu(); }
            }

        }, 1, false);
        g.start();
        primaryStage.setScene(disclaimer);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private void mainMenu() {
        g.stop();
        SceneManager m = new SceneManager(primaryStage);
        m.callInitialScene(UIStates.MAIN_MENU);
        audioManager.startUiMusic();
        if(Constants.DEBUG_AUDIO_TEST) { audioManager.testAudioFiles(); }
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch(Exception e) {
            e.printStackTrace();
            if(Log.hasBeenBootstrapped()) {
                Log.critical(GameLauncher.class.getSimpleName(), "Critical Error has been detected. It is not possible to recover from this.\n" + Helper.stackTraceToString(e));
            }
            System.exit(1);
        }
    }

    public static AudioManager am() { return audioManager; }
    public static void setFPS(int fps) { primaryStage.setTitle("FPS: " + fps); }
    public static void disableFPSTracking() { primaryStage.setTitle("GNUMAN"); }
    public static void hide() { primaryStage.hide(); }
    public static void show() { primaryStage.show(); primaryStage.sizeToScene(); }
    public static void centerStage() { primaryStage.centerOnScreen(); }
    public static Stage getStage() { return primaryStage; }
    public static void createAudioManager(GameSettings settings) { if(audioManager == null) { audioManager = AudioManager.createAudioManager(settings); }}
}
