package de.hshannover.inform.gnuman.app.mapeditor;

import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.NoSuchElementException;
import de.hshannover.inform.gnuman.app.enums.Directions;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.model.storage.MapCell;
import de.hshannover.inform.gnuman.app.model.storage.MapData;
import de.hshannover.inform.gnuman.app.util.Helper;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebView;
import javafx.util.Pair;

/**
 * Heavy copy paste and abuse of the dialog API to not having to deal with more FXML files.
 * @author Marc Herschel
 */

public class Dialogs {

    public static void exceptionDialog(Exception e, String title, String notification) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setGraphic(new ImageView(Dialogs.class.getResource("resources/error.png").toExternalForm()));
        alert.setTitle("Oh Noooo! " + e.getClass().getSimpleName() + " :(");
        alert.setHeaderText(title);
        alert.setContentText(notification);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();
        Label label = new Label("The exception stacktrace was:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public static void dialog(AlertType type, String header, String notification) {
        Alert alert = new Alert(type);
        switch(type) {
            case INFORMATION:
                alert.setGraphic(new ImageView(Dialogs.class.getResource(header.equals("__HackForDialog") ? "resources/editor.png" : "resources/info.png").toExternalForm()));
                if(header.equals("__HackForDialog")) { ((ImageView) alert.getGraphic()).setFitHeight(50); ((ImageView) alert.getGraphic()).setFitWidth(50); }
                break;
            case ERROR:
                alert.setGraphic(new ImageView(Dialogs.class.getResource("resources/error.png").toExternalForm()));
                break;
            case WARNING:
                alert.setGraphic(new ImageView(Dialogs.class.getResource("resources/warning.png").toExternalForm()));
                break;
            default: break;
        }
        alert.setTitle(header.equals("__HackForDialog") ? "About GNUMAN Editor" : type.toString());
        alert.setHeaderText(header.equals("__HackForDialog") ? "" : header);
        alert.setContentText(notification);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public static boolean yesOrNoDialog(String header, String notification) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setGraphic(new ImageView(Dialogs.class.getResource("resources/question.png").toExternalForm()));
        alert.setTitle("CONFIRM ACTION");
        alert.setHeaderText(header);
        alert.setContentText(notification);
        ButtonType okButton = new ButtonType("Yes", ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonData.NO);
        alert.getButtonTypes().setAll(okButton, noButton);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
        return alert.getResult().getButtonData() == ButtonData.YES;
    }

    static void help() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setGraphic(new ImageView(Dialogs.class.getResource("resources/question.png").toExternalForm()));
        alert.setTitle("Help.");
        alert.setHeaderText("GNUMAN Help");
        ButtonType okButton = new ButtonType("Ok", ButtonData.YES);
        alert.getButtonTypes().setAll(okButton);
        BorderPane p = new BorderPane();
        WebView v = new WebView();
        p.setCenter(v);
        v.getEngine().load(Helper.class.getResource("/de/hshannover/inform/gnuman/app/mapeditor/resources/html/index.html").toExternalForm());
        alert.getDialogPane().setContent(p);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.setResizable(true);
        alert.showAndWait();
    }

    static void modifierDialog(int x, int y, MapData currentMap, Label currentStatus) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit modifiers for [X: " + x + " | Y:" + y + "]");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10); gridPane.setVgap(10);
        CheckBox upwards = new CheckBox(),
                 downwards = new CheckBox(),
                 leftwards = new CheckBox(),
                 rightwards = new CheckBox(),
                 slowdown = new CheckBox();

        slowdown.setSelected(currentMap.getSlowdown()[x][y]);
        currentMap.getBlocked().forEach((k, v) -> {
            v.forEach(c -> {
                if(c.getCellX() == x && c.getCellY() == y) {
                    switch(k) {
                    case DOWN:
                        downwards.setSelected(true);
                        break;
                    case LEFT:
                        leftwards.setSelected(true);
                        break;
                    case RIGHT:
                        rightwards.setSelected(true);
                        break;
                    case UP:
                        upwards.setSelected(true);
                        break;
                    default:
                        break;
                    }
                }
            });
        });

        gridPane.add(new Label("Block UP for ghosts: "), 0, 0);
        gridPane.add(upwards, 1, 0);
        gridPane.add(new Label("Block DOWN for ghosts: "), 0, 1);
        gridPane.add(downwards, 1, 1);
        gridPane.add(new Label("Block LEFT for ghosts: "), 0, 2);
        gridPane.add(leftwards, 1, 2);
        gridPane.add(new Label("Block RIGHT for ghosts: "), 0, 3);
        gridPane.add(rightwards, 1, 3);
        gridPane.add(new Label("Ghost slowdown: "), 0, 4);
        gridPane.add(slowdown, 1, 4);
        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(upwards::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if(dialogButton == ButtonType.OK) {
                currentMap.getSlowdown()[x][y] = slowdown.isSelected();
                removeOrAddBlocked(upwards.isSelected(), x, y, currentMap, Directions.UP);
                removeOrAddBlocked(downwards.isSelected(), x, y, currentMap, Directions.DOWN);
                removeOrAddBlocked(leftwards.isSelected(), x, y, currentMap, Directions.LEFT);
                removeOrAddBlocked(rightwards.isSelected(), x, y, currentMap, Directions.RIGHT);
                currentStatus.setText("Modifier changed for X: " + x + " Y: " + y);
            }
            return true;
        });

        dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        dialog.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        dialog.showAndWait();
    }

    private static void removeOrAddBlocked(boolean add, int x, int y, MapData m, Directions d) {
        if(add) {
            m.getBlocked().get(d).add(new MapCell(x, y));
        } else {
            m.getBlocked().get(d).removeIf(c -> c.equals(new MapCell(x,  y)));
        }
    }

    static Pair<String, String> changeMetadata(MapData currentMap) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Edit metadata");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10); gridPane.setVgap(10);

        TextField author = new TextField(currentMap.getAuthor()), name = new TextField(currentMap.getName());
        name.setPromptText("Name"); author.setPromptText("Author");

        gridPane.add(new Label("Author:"), 0, 0);
        gridPane.add(author, 1, 0);
        gridPane.add(new Label("Name:"), 2, 0);
        gridPane.add(name, 3, 0);
        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(author::requestFocus);

        dialog.setResultConverter(dialogButton -> dialogButton == ButtonType.OK ? new Pair<>(Helper.onlyAscii(author.getText()), Helper.onlyAscii(name.getText()))
                                             : new Pair<>(currentMap.getAuthor(), currentMap.getName()));

        dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        dialog.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        return dialog.showAndWait().get();
    }

    static HashMap<EntityObjects, MapCell> changeScatter(MapData currentMap, Label lastStatus) {
        Dialog<HashMap<EntityObjects, MapCell>> dialog = new Dialog<>();
        dialog.setTitle("Change scatter data");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10); gridPane.setVgap(10);

        HashMap<EntityObjects, MapCell> tmp = currentMap.getScatterPoints();
        HashMap<EntityObjects, MapCell> fallback = new HashMap<>();
        HashMap<Node, Boolean> fields = new HashMap<>();

        TextField blinky_x = new TextField(getX(EntityObjects.BLINKY, tmp, fallback)),
                 blinky_y = new TextField(getY(EntityObjects.BLINKY, tmp, fallback)),
                 pinky_x = new TextField(getX(EntityObjects.PINKY, tmp, fallback)),
                 pinky_y = new TextField(getY(EntityObjects.PINKY, tmp, fallback)),
                 inky_x = new TextField(getX(EntityObjects.INKY, tmp, fallback)),
                 inky_y = new TextField(getY(EntityObjects.INKY, tmp, fallback)),
                 clyde_x = new TextField(getX(EntityObjects.CLYDE, tmp, fallback)),
                 clyde_y = new TextField(getY(EntityObjects.CLYDE, tmp, fallback));

        gridPane.add(new Label("Ghost:"), 0, 0);
        gridPane.add(new Label("X:"), 1, 0);
        gridPane.add(new Label("Y:"), 2, 0);
        gridPane.add(new Label("Blinky: "), 0, 1);
        gridPane.add(blinky_x, 1, 1);
        gridPane.add(blinky_y, 2, 1);
        gridPane.add(new Label("Pinky: "), 0, 2);
        gridPane.add(pinky_x, 1, 2);
        gridPane.add(pinky_y, 2, 2);
        gridPane.add(new Label("Inky: "), 0, 3);
        gridPane.add(inky_x, 1, 3);
        gridPane.add(inky_y, 2, 3);
        gridPane.add(new Label("Clyde: "), 0, 4);
        gridPane.add(clyde_x, 1, 4);
        gridPane.add(clyde_y, 2, 4);
        dialog.getDialogPane().setContent(gridPane);

        gridPane.getChildren().forEach(child -> {
            if(child instanceof TextField) {
                fields.put(child, true);
                ((TextField) child).textProperty().addListener(e -> {
                    boolean ok = true;
                    fields.put(child, de.hshannover.inform.gnuman.app.util.Helper.isInteger(((TextField) child).getText()));
                    for(boolean b : fields.values()) { ok &= b; }
                    dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(!ok);
                });
            }
        });

        Platform.runLater(blinky_x::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            HashMap<EntityObjects, MapCell> ret = new HashMap<>();
            if(dialogButton == ButtonType.OK) {
                ret.put(EntityObjects.BLINKY, addScatter(blinky_x.getText(), blinky_y.getText()));
                ret.put(EntityObjects.PINKY, addScatter(pinky_x.getText(), pinky_y.getText()));
                ret.put(EntityObjects.CLYDE, addScatter(clyde_x.getText(), clyde_y.getText()));
                ret.put(EntityObjects.INKY, addScatter(inky_x.getText(), inky_y.getText()));
                lastStatus.setText("Updated scatter data.");
                return ret;
            } else {
                lastStatus.setText("Aborted scatter data update.");
                return fallback;
            }
        });

        dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        dialog.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        return dialog.showAndWait().get();
    }

    static void aStarDialog(String title, String notification, String trace) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setGraphic(new ImageView(Dialogs.class.getResource("resources/info.png").toExternalForm()));
        alert.setTitle(alert.getAlertType().toString());
        alert.setHeaderText(title);
        alert.setContentText(notification);
        Label label = new Label("The path node trace is:");
        TextArea textArea = new TextArea(trace);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    static NewMapDTO getNewMapSettings() {
        Dialog<NewMapDTO> dialog = new Dialog<>();
        dialog.setTitle("Create a new map!");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10); gridPane.setVgap(10);

        TextField author = new TextField(), name = new TextField(), height = new TextField(), width = new TextField();
        author.setPromptText("Author"); name.setPromptText("Name"); height.setPromptText("min. 15, max. 125"); width.setPromptText("min. 15, max. 125");
        height.textProperty().addListener(e -> dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(!(isValidDimension(width.getText()) != -1 && isValidDimension(height.getText()) != -1)));
        width.textProperty().addListener(e -> dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(!(isValidDimension(width.getText()) != -1 && isValidDimension(height.getText()) != -1)));

        gridPane.add(new Label("Author:"), 0, 0);
        gridPane.add(author, 1, 0);
        gridPane.add(new Label("Name:"), 2, 0);
        gridPane.add(name, 3, 0);
        gridPane.add(new Label("Width (Blocks):"), 0, 1);
        gridPane.add(width, 1, 1);
        gridPane.add(new Label("Height (Blocks):"), 2, 1);
        gridPane.add(height, 3, 1);
        dialog.getDialogPane().setContent(gridPane);

        Platform.runLater(author::requestFocus);

        dialog.setResultConverter(dialogButton -> dialogButton != ButtonType.OK ? null : new NewMapDTO(Integer.parseInt(height.getText()), Integer.parseInt(width.getText()), name.getText(), author.getText()));

        dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        dialog.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        try {
            return dialog.showAndWait().get();
        } catch(NoSuchElementException e) {
            return null;
        }
    }

    static void showCredits() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setGraphic(new ImageView(Dialogs.class.getResource("resources/creative-commons-logo.png").toExternalForm()));
        alert.setTitle("Credits");
        alert.setHeaderText("Credits for used assets!");
        GridPane p = new GridPane(); p.setVgap(8);
        p.add(new Label("Icons for the Map Editor: (Creative Commons BY 3.0)"), 0, 0);
        Hyperlink h1 = new Hyperlink("Freepik");
        h1.setOnAction(e -> openUrl("https://www.freepik.com/"));
        p.add(h1, 0, 1);
        Hyperlink h2 = new Hyperlink("Roundicons");
        h2.setOnAction(e -> openUrl("https://www.flaticon.com/authors/roundicons"));
        p.add(h2, 0, 2);
        Hyperlink h3 = new Hyperlink("Smashicons");
        h3.setOnAction(e -> openUrl("https://www.flaticon.com/authors/smashicons"));
        p.add(h3, 0, 3);
        Hyperlink h4 = new Hyperlink("Nhor Phai");
        h4.setOnAction(e -> openUrl("https://www.flaticon.com/authors/nhor-phai"));
        p.add(h4, 0, 4);
        Hyperlink h5 = new Hyperlink("Daniel Bruce");
        h5.setOnAction(e -> openUrl("https://www.flaticon.com/authors/daniel-bruce"));
        p.add(h5, 0, 5);
        Hyperlink h6 = new Hyperlink("Darius Dan");
        h6.setOnAction(e -> openUrl("https://www.flaticon.com/authors/darius-dan"));
        p.add(h6, 0, 6);
        alert.getDialogPane().setContent(p);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    public static void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (IOException | URISyntaxException e) {
            dialog(AlertType.ERROR, "Failed to open credit URL.", "URL was: " + url);
        }
    }

    private static int isValidDimension(String text) {
        if(!Helper.isInteger(text)) { return -1; }
        int i = Integer.parseInt(text);
        return (i >= 15 && i <= 125) ? i : -1;
    }

    private static String getY(EntityObjects entity, HashMap<EntityObjects, MapCell> m, HashMap<EntityObjects, MapCell> f) {
        int y = m.containsKey(entity) ? m.get(entity).getCellY() : 0;
        f.put(entity, new MapCell(f.get(entity).getCellX(), y));
        return Integer.toString(y);
    }

    private static String getX(EntityObjects entity, HashMap<EntityObjects, MapCell> m, HashMap<EntityObjects, MapCell> f) {
        int x = m.containsKey(entity) ? m.get(entity).getCellX() : 0;
        f.put(entity, new MapCell(x, 0));
        return Integer.toString(x);
    }

    private static MapCell addScatter(String x, String y) { return new MapCell(Integer.parseInt(x), Integer.parseInt(y)); }
}
