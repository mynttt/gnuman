package de.hshannover.inform.gnuman.app.mapeditor;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;

/**
 * Bootstrap the editor.
 * @author Marc Herschel
 */

public class StartEditor {
    private Scene editor;
    private EditorController controller;

    public void start() throws IOException {
        Stage stage = new Stage();
        FXMLLoader l = new FXMLLoader(getClass().getResource("resources/Editor.fxml"));
        editor = new Scene(l.load());
        controller = l.getController();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("resources/editor.png")));
        stage.setMinWidth(990);
        stage.setMinHeight(440);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setScene(editor);
        stage.setTitle("GNUMAN Editor :: Ready");
        stage.show();
        editor.getStylesheets().add(getClass().getResource("resources/style.css").toExternalForm());
        editor.getRoot().applyCss();
        controller.setStage(stage);
    }

}
