package de.hshannover.inform.gnuman.app.mapeditor;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import de.hshannover.inform.gnuman.GameLauncher;
import de.hshannover.inform.gnuman.app.AudioManager;
import de.hshannover.inform.gnuman.app.enums.gameobjects.EntityObjects;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.mapeditor.Enums.BackgroundRenderStates;
import de.hshannover.inform.gnuman.app.mapeditor.Enums.CreationStates;
import de.hshannover.inform.gnuman.app.mapeditor.Enums.TwoPointsSelectionStates;
import de.hshannover.inform.gnuman.app.mapeditor.Enums.ProgressStates;
import de.hshannover.inform.gnuman.app.mapeditor.Enums.Tools;
import de.hshannover.inform.gnuman.app.model.storage.MapCell;
import de.hshannover.inform.gnuman.app.model.storage.MapData;
import de.hshannover.inform.gnuman.app.model.storage.PathNode;
import de.hshannover.inform.gnuman.app.modules.Textures;
import de.hshannover.inform.gnuman.app.util.AStarPathfinding;
import de.hshannover.inform.gnuman.app.util.MapParser;
import de.hshannover.inform.gnuman.app.util.ParsingReply;
import de.hshannover.inform.gnuman.app.util.ParsingStatus;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * THERE IS NO DOCUMENTATION FOR THE MAP EDITOR GUI AND THERE WILL NEVER BE.
 * IT'S A HACKED AND UGLY IMPLEMENTATION WITH LOTS OF COPY PASTE. THIS IS NOT GOOD CODE BUT THERE IS NO NEED
 * FOR THE EDITOR TO BE OF GOOD CODE QUALITY AS LONG AS IT "JUST WORKS(TM)" AND TAKES LOW EFFORT TO BUILD!
 * ABSTRACTING ANYTHING HERE MAKES NO SENSE BECAUSE IT WILL NEVER BE REUSED BY ANOTHER PART OF THE APPLICATION.
 *
 * Also, this is the definition of a god class.
 *
 * @author Marc Herschel
 */

public class EditorController {
    @FXML BorderPane root;
    @FXML Canvas canvas;
    @FXML Pane canvasPane;
    @FXML ChoiceBox<StaticObjects> mapBlocks;
    @FXML Label selectedOperation, lastStatus;
    @FXML MenuItem redoField, undoField, save, saveAs;
    @FXML CheckMenuItem toggleDisplayGrid, toggleDisplayProperties, toggleRenderAsItems, automaticFit;
    @FXML RadioMenuItem toggleRenderAsBlocks, toggleRenderAsBackground, toggleRenderAsFlashmap,
                        toggleBackgroundDefault, toggleBackgroundOneColor, toggleBackgroundCustom,
                        toggleFlashmapDefault, toggleFlashmapCustom;
    @FXML Slider scaleSlider;

    Stage stage;
    State state;
    Image[] textures;
    GraphicsContext g;
    OperationStack redo, undo;
    StaticObjects selectedBlock;
    ToggleGroup toggleRenderAs, toggleBackground, toggleFlashmap;
    MapData currentMap;
    ViewPort viewPort;
    List<PathNode> pathList;
    Function<Double, Integer> cellX, cellY;
    boolean keyUp, keyDown, keyLeft, keyRight;
    int scale;

    void setStage(Stage stage) {
        stage.setOnCloseRequest(e -> exitAskSave());
        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if((e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN || e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT) && !state.automaticFit) {
                if(e.getCode() == KeyCode.UP) { keyUp = true; }
                if(e.getCode() == KeyCode.DOWN) { keyDown = true; }
                if(e.getCode() == KeyCode.LEFT) { keyLeft = true; }
                if(e.getCode() == KeyCode.RIGHT) { keyRight = true; }
                freelook(); e.consume();
            }
        });
        stage.getScene().addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if(e.getCode() == KeyCode.UP) { keyUp = false; }
            if(e.getCode() == KeyCode.DOWN) { keyDown = false; }
            if(e.getCode() == KeyCode.LEFT) { keyLeft = false; }
            if(e.getCode() == KeyCode.RIGHT) { keyRight = false; }
            if(!state.automaticFit) e.consume();
        });
        stage.getScene().addEventFilter(ScrollEvent.SCROLL, e -> {
            if(!state.automaticFit) {
                scaleSlider.setValue(scaleSlider.getValue() + (e.getDeltaY() > 0 ? 5 : -5));
                if(currentMap.getWidthInBlocks()*scale<=viewPort.x) { viewPort.x-=viewPort.x-currentMap.getWidthInBlocks()*scale+scale; }
                if(currentMap.getHeightInBlocks()*scale<=viewPort.y) { viewPort.y-=viewPort.y-currentMap.getHeightInBlocks()*scale+scale; }
                redraw();
                e.consume();
            }
        });
        this.stage = stage;
    }

    @FXML
    private void initialize() {
    //Fields
        state = new State();
        g = canvas.getGraphicsContext2D();
        viewPort = new ViewPort();
        redo = new OperationStack(() -> redoField.setDisable(redo.size() == 0)); redo.clear();
        undo = new OperationStack(() -> undoField.setDisable(undo.size() == 0)); undo.clear();
        textures = new Image[] { Textures.loadStaticImage("game/food.png"), Textures.loadStaticImage("game/powerup.png"), Textures.loadStaticImage("game/ghostwall.png")};
    //Data
        mapBlocks.getItems().addAll(StaticObjects.EMPTY, StaticObjects.WALL, StaticObjects.FOOD, StaticObjects.POWERUP, StaticObjects.PLAYER_SPAWN, StaticObjects.BONUS_ITEM);
        mapBlocks.setValue(mapBlocks.getItems().get(0));
        selectedBlock = mapBlocks.getValue();
        toggleRenderAs = new ToggleGroup(); toggleRenderAsBlocks.setToggleGroup(toggleRenderAs); toggleRenderAsBackground.setToggleGroup(toggleRenderAs); toggleRenderAsFlashmap.setToggleGroup(toggleRenderAs);
        toggleBackground = new ToggleGroup(); toggleBackgroundDefault.setToggleGroup(toggleBackground); toggleBackgroundOneColor.setToggleGroup(toggleBackground); toggleBackgroundCustom.setToggleGroup(toggleBackground);
        toggleFlashmap = new ToggleGroup(); toggleFlashmapCustom.setToggleGroup(toggleFlashmap); toggleFlashmapDefault.setToggleGroup(toggleFlashmap);
    //Listeners
        canvasPane.heightProperty().addListener(e -> scaleCanvas());
        canvasPane.widthProperty().addListener(e -> scaleCanvas());
        toggleRenderAs.selectedToggleProperty().addListener(e -> checkBackgroundImageSetting((RadioMenuItem) toggleRenderAs.getSelectedToggle()));
        mapBlocks.valueProperty().addListener(e -> {
            selectedBlock = mapBlocks.getValue();
            updateSelectedTools();
        });
        toggleDisplayGrid.selectedProperty().addListener(e -> {
            state.renderSettings.drawGrid = toggleDisplayGrid.isSelected();
            redraw();
        });
        toggleRenderAsItems.selectedProperty().addListener(e -> {
            state.renderSettings.withItems = toggleRenderAsItems.isSelected();
            redraw();
        });
        automaticFit.selectedProperty().addListener(e -> {
            state.automaticFit = automaticFit.isSelected();
            if(automaticFit.isSelected()) { disableZoom(); scaleSlider.setDisable(true); } else { scaleSlider.setDisable(false); scaleSlider.setValue(scale); }
            scaleCanvas();
        });
        scaleSlider.valueProperty().addListener(e -> {
            scaleSlider.setValue(Math.round(scaleSlider.getValue()));
            if(scaleSlider.getValue() == 0) {
                scaleSlider.setValue(2); }
            scaleCanvas();
        });
        toggleDisplayProperties.selectedProperty().addListener(e -> {
            state.renderSettings.withModifiers = toggleDisplayProperties.isSelected();
            redraw();
        });
        toggleBackground.selectedToggleProperty().addListener(e -> {
            RadioMenuItem i = (RadioMenuItem) toggleBackground.getSelectedToggle();
            if(i == toggleBackgroundDefault) { state.spriteSheetPath = Constants.MAP_TILE_SET; }
            if(i == toggleBackgroundOneColor) { state.spriteSheetPath = Constants.MAP_TILE_SET_ONE_COLOR; }
            if(i == toggleBackgroundCustom) {
                File f = getImageLocation("Select a background spritesheet");
                if(f == null) {
                    toggleBackground.selectToggle(toggleBackgroundDefault);
                    state.spriteSheetPath = Constants.MAP_TILE_SET;
                    return;
                }
                state.spriteSheetPath = f.getAbsolutePath();
            }
        });
        toggleFlashmap.selectedToggleProperty().addListener(e -> {
            RadioMenuItem i = (RadioMenuItem) toggleFlashmap.getSelectedToggle();
            if(i == toggleFlashmapDefault) { state.flashSheetPath = Constants.MAP_TILE_SET_FLASH; }
            if(i == toggleFlashmapCustom) {
                File f = getImageLocation("Select a flashmap spritesheet");
                if(f == null) {
                    toggleFlashmap.selectToggle(toggleFlashmapDefault);
                    state.flashSheetPath = Constants.MAP_TILE_SET_FLASH;
                    return;
                }
                state.flashSheetPath = f.getAbsolutePath();
            }
        });
        canvasPane.setOnMouseMoved(e -> {
            state.renderSettings.arrowX = e.getX(); state.renderSettings.arrowY = e.getY();
            if(state.tool == Tools.LINE && state.line.state == TwoPointsSelectionStates.FIRST_SET) { redraw(); }
        });
        canvasPane.setOnMouseClicked(e -> {
            if(scale == 0 || state.creation == CreationStates.UNDEFINED || e.getX() < 0 || e.getY() < 0 || cellX.apply(e.getX())  >= currentMap.getWidthInBlocks() || cellY.apply(e.getY()) >= currentMap.getHeightInBlocks()) { return; }
            int x = cellX.apply(e.getX()), y = cellY.apply(e.getY());
            if(state.tool == Tools.INSPECTOR) { inspector(x, y); return; }
            if(state.tool == Tools.A_STAR_ALGORITHM) { aStarTool(x, y); return; }
            if(state.tool == Tools.MODIFIER) { modifier(x, y); return; }
            if(!(state.tool == Tools.DRAW || state.tool == Tools.ERASE || state.tool == Tools.LINE)) { return; }
            if(inGhostHouseArea(x, y)) { Dialogs.dialog(AlertType.INFORMATION, "Unsupported Operation", "Ghost house area is protected.\n\nModification is not possible!"); return; }
            if(state.renderSettings.backgroundState != BackgroundRenderStates.BLOCKS_COLOR) { state.renderSettings.backgroundState = BackgroundRenderStates.BLOCKS_COLOR; toggleRenderAs.selectToggle(toggleRenderAsBlocks); state.renderSettings.withItems = false; toggleRenderAsItems.setSelected(false); redraw(); }
            if(state.tool == Tools.LINE) { lineTool(x, y, e.getButton() == MouseButton.SECONDARY); } else { drawOrRemove(x, y); }
        });
        canvasPane.setOnMouseDragged(e -> {
            if(scale == 0 || state.creation == CreationStates.UNDEFINED || e.getX() < 0 || e.getY() < 0 || cellX.apply(e.getX())  >= currentMap.getWidthInBlocks() || cellY.apply(e.getY()) >= currentMap.getHeightInBlocks()) { return; }
            int x = cellX.apply(e.getX()), y = cellY.apply(e.getY());
            if(!(state.tool == Tools.DRAW || state.tool == Tools.ERASE)) { return; }
            if(inGhostHouseArea(x, y)) { Dialogs.dialog(AlertType.INFORMATION, "Unsupported Operation", "Ghost house area is protected.\n\nModification is not possible!"); return; }
            if(state.renderSettings.backgroundState != BackgroundRenderStates.BLOCKS_COLOR) { state.renderSettings.backgroundState = BackgroundRenderStates.BLOCKS_COLOR; toggleRenderAs.selectToggle(toggleRenderAsBlocks); state.renderSettings.withItems = false; toggleRenderAsItems.setSelected(false); redraw(); }
            drawOrRemove(x, y);
        });
    //Init
        cellX = (posX) -> viewPort.interpolatedCellX(posX, scale);
        cellY = (posY) -> viewPort.interpolatedCellY(posY, scale);
        root.getCenter().setManaged(true);
        lastStatus.setText("Load/Create a new map to begin.");
        updateCreationState(CreationStates.UNDEFINED);
        updateSelectedTools();
        scaleCanvas();
    }

    @FXML
    private void selectDrawTool() {
        state.tool = Tools.DRAW;
        suspendLineTool();
        updateSelectedTools();
    }

    @FXML
    private void selectLineTool() {
        state.tool = Tools.LINE;
        updateSelectedTools();
    }

    @FXML
    private void selectEraseTool() {
        state.tool = Tools.ERASE;
        suspendLineTool();
        updateSelectedTools();
    }

    @FXML
    private void selectModifierTool() {
        state.tool = Tools.MODIFIER;
        suspendLineTool();
        updateSelectedTools();
    }

    @FXML
    private void selectValidationTool() {
        suspendLineTool();
        ParsingStatus p = validateMap();
        if(p != null) {
            lastStatus.setText(p.isSuccess() ? "Validation succeeded." : "Validation failed -> " + p.statusMessage());
            Dialogs.dialog(p.isSuccess() ? AlertType.INFORMATION : AlertType.WARNING, p.isSuccess() ? "Validation succeeded." : "Validation failed.", p.statusMessage());
        } else {
            Dialogs.dialog(AlertType.ERROR, "Illegal Operation", "Its impossible to validate without having a map loaded.");
        }
    }

    @FXML
    private void selectInspectorTool() {
        state.tool = Tools.INSPECTOR;
        suspendLineTool();
        updateSelectedTools();
    }

    @FXML
    private void selectAStarAlgorithmTool() {
        state.tool = Tools.A_STAR_ALGORITHM;
        suspendLineTool();
        updateSelectedTools();
    }

    @FXML
    private void newMap() {
        if(state.creation != CreationStates.UNDEFINED || state.progress == ProgressStates.UNSAVED) {
            if(Dialogs.yesOrNoDialog("Unsaved Progress.", "Do you want to save your unsaved progress before creating a new map?")) {
                sharedSaving(false);
            }
        }
        NewMapDTO settings = Dialogs.getNewMapSettings();
        if(settings != null) {
            updateCreationState(CreationStates.NEW);
            state.progress = ProgressStates.SAVED;
            state.path = null;
            state.resetAStarTool();
            state.resetLineTool();
            state.renderSettings.withAStar = false;
            currentMap = new MapData(false);
            currentMap.setAuthor(settings.author);
            currentMap.setName(settings.name);
            currentMap.setCreationDate();
            currentMap.setWidthInBlocks(settings.width);
            currentMap.setHeightInBlocks(settings.height);
            setTitle();
            lastStatus.setText("Created new map!");
            buildMapFoundation();
            scaleCanvas();
            automaticFit.setDisable(false);
            disableZoom();
        }
    }

    @FXML
    private void openMap() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open GNUMAN map");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Gnuman Map", "*.gnuman"), new ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if(selectedFile == null) { return; }
        state.path = selectedFile.getAbsolutePath();
        try {
            ParsingReply r = MapParser.parseMap(state.path, false);
            if(r.status().isSuccess()) {
                currentMap = r.data();
                setTitle();
                lastStatus.setText("Loaded map: " + selectedFile.getName());
                redo.clear(); undo.clear();
                state.tool = Tools.DRAW;
                state.progress = ProgressStates.SAVED;
                state.resetLineTool();
                state.resetAStarTool();
                state.renderSettings.withAStar = false;
                updateSelectedTools();
                updateCreationState(CreationStates.LOADED);
                automaticFit.setDisable(false);
                disableZoom();
            } else {
                unloadMap();
                lastStatus.setText("Failed to load map duo the map being corrupt: " + selectedFile.getName());
                Dialogs.dialog(AlertType.ERROR, "Failed to load and parse map.", "Failed to load map: " + r.status().statusMessage());
            }
        } catch (Exception e) {
            unloadMap();
            lastStatus.setText("Failed to load map: " + selectedFile.getName());
            Dialogs.exceptionDialog(e, "Garbage in, Garbage out.", "In computer science, garbage in, garbage out (GIGO) describes the concept that flawed, or nonsense input data produces nonsense output or \"garbage\". \n\nTo make it short: Is this a valid GNUMAN map?");
        }
        scaleCanvas();
    }

    @FXML
    private void saveMap() { sharedSaving(false); }

    @FXML
    private void saveMapAs() { sharedSaving(true); }

    @FXML
    private void resetMap() {
        if(state.isLoaded()) {
            undo.clear(); redo.clear();
            state.resetLineTool();
            state.resetAStarTool();
            buildMapFoundation();
            currentMap.getScatterPoints().clear();
            currentMap.getBlocked().clear();
            state.progress = ProgressStates.UNSAVED;
            state.renderSettings.withAStar = false;
            Dialogs.dialog(AlertType.INFORMATION, "Reset map", "Your map has been reset to its initial state.");
            lastStatus.setText("Map has been reset.");
            scaleCanvas();
        } else {
            Dialogs.dialog(AlertType.ERROR, "Illegal Operation", "Can only reset a loaded map.");
        }
    }

    @FXML
    private void exitAskSave() {
        if(state.canExitWithoutAsking()) { backToGnuman(); return; }
        if(Dialogs.yesOrNoDialog("Unsaved Progress.", "Do you want to save your unsaved progress before leaving the editor?")) { sharedSaving(true); }
        backToGnuman();
    }

    @FXML
    private void redo () {
        Operation o = redo.pop();
        o.execute(currentMap);
        undo.push(o);
        redraw();
    }

    @FXML
    private void undo () {
        Operation o = undo.pop();
        o.execute(currentMap);
        redo.push(o);
        redraw();
    }

    @FXML
    private void modifyMetaData () {
        if(state.creation == CreationStates.UNDEFINED) { Dialogs.dialog(AlertType.ERROR, "Illegal Operation", "Load a map to change metadata."); return; }
        Pair<String, String> result = Dialogs.changeMetadata(currentMap);
        currentMap.setAuthor(result.getKey().trim().length() == 0 ? "Unknown" : result.getKey());
        currentMap.setName(result.getValue().trim().length() == 0 ? "Unknown" : result.getValue());
        setTitle();
        lastStatus.setText("Updated metadata.");
    }

    @FXML
    private void modifyScatterData () {
        if(state.creation == CreationStates.UNDEFINED) { Dialogs.dialog(AlertType.ERROR, "Illegal Operation", "Load a map to modify scatter data."); return; }
        HashMap<EntityObjects, MapCell> scatter = Dialogs.changeScatter(currentMap, lastStatus);
        currentMap.setScatterData(scatter);
    }

    @FXML
    private void defaultScatterData () {
        if(state.creation == CreationStates.UNDEFINED) { Dialogs.dialog(AlertType.ERROR, "Illegal Operation", "Load a map to reset scatter data."); return; }
        currentMap.getScatterPoints().clear();
        currentMap.addScatterPoint(EntityObjects.BLINKY, currentMap.getWidthInBlocks()-3, -1);
        currentMap.addScatterPoint(EntityObjects.PINKY, 2, -1);
        currentMap.addScatterPoint(EntityObjects.INKY, currentMap.getWidthInBlocks()-1, currentMap.getHeightInBlocks());
        currentMap.addScatterPoint(EntityObjects.CLYDE, 0, currentMap.getHeightInBlocks());
        lastStatus.setText("Set default Pacman scatter data.");
        Dialogs.dialog(AlertType.INFORMATION, "Operation succeeded.", "You have successfully set the default Pacman scatter data.");
    }

    @FXML
    private void fixByConversion() { teleporterFix(false); }

    @FXML
    private void fixByRemoval() { teleporterFix(true); }

    @FXML
    private void fillEmptyWithFood() {
        if(state.creation == CreationStates.UNDEFINED) { Dialogs.dialog(AlertType.ERROR, "Illegal Operation", "Load a map to fill with food."); return; }
        int filled = 0;
        ArrayList<Point> cells = new ArrayList<>();
        ArrayList<StaticObjects> blocks = new ArrayList<>();
        for(int y = 1; y < currentMap.getHeightInBlocks()-1; y++) {
            for(int x = 1; x < currentMap.getWidthInBlocks()-1; x++) {
                if(!inGhostHouseArea(x, y) && currentMap.getData()[y][x] == StaticObjects.EMPTY) {
                    cells.add(new Point(x, y));
                    blocks.add(currentMap.getData()[y][x]);
                    currentMap.setMapData(StaticObjects.FOOD, x, y);
                    filled++;
                }
            }
        }
        if(filled > 0) { undo.push(new BatchBlockModificationOperation(blocks, cells)); }
        Dialogs.dialog(AlertType.INFORMATION, "Operation completed", "Replaced " + filled + " block(s).");
        redraw();
    }

    @FXML
    private void cleanUselessProperties() {
        if(state.creation == CreationStates.UNDEFINED) { Dialogs.dialog(AlertType.ERROR, "Illegal Operation", "Load a map to clean useless properties."); return; }
        int beforeCleanup = currentMap.getBlocked().values().stream().mapToInt(l -> l.size()).sum();
        currentMap.getBlocked().forEach((k, v) -> v.removeIf(c -> currentMap.getData()[c.getCellY()][c.getCellX()] == StaticObjects.WALL));
        int afterCleanup = beforeCleanup - currentMap.getBlocked().values().stream().mapToInt(l -> l.size()).sum();
        for(int i = 0; i < currentMap.getHeightInBlocks(); i++) { for(int j = 0; j < currentMap.getWidthInBlocks(); j++) { if(currentMap.getData()[i][j] == StaticObjects.WALL && currentMap.getSlowdown()[j][i]) { currentMap.getSlowdown()[j][i] = false; afterCleanup++; }}}
        Dialogs.dialog(AlertType.INFORMATION, "Operation completed", "Removed " + afterCleanup + " useless properties.");
        redraw();
    }

    @FXML
    private void resetToFitWindow() {
        canvas.setWidth(canvasPane.getWidth());
        canvas.setHeight(canvasPane.getHeight());
        windowDependentScale();
        viewPort.reset();
        redraw();
    }

    @FXML
    private void generateBackground() {
        if(state.creation == CreationStates.UNDEFINED) { Dialogs.dialog(AlertType.ERROR, "Invalid operation.", "Load a map to generate a background."); return; }
        if(toggleBackground.getSelectedToggle() == toggleBackgroundCustom && !Files.exists(Paths.get(state.spriteSheetPath))) {
            Dialogs.dialog(AlertType.ERROR, "Could not find custom background sheet", "Failed to resolve path: "+state.spriteSheetPath+"\nPlease select a new custom background sheet or use one of the default options.");
            lastStatus.setText("Failed to resolve path for custom background.");
            return;
        }
        try {
            boolean external = ((RadioMenuItem) toggleBackground.getSelectedToggle()) == toggleBackgroundCustom;
            MapToImageConverter m = new MapToImageConverter(currentMap.getData(), state.spriteSheetPath, 25, external);
            currentMap.setBackground(m.getAsFxImage());
            lastStatus.setText("Generated background.");
            state.renderSettings.backgroundState = BackgroundRenderStates.BACKGROUND;
            toggleRenderAs.selectToggle(toggleRenderAsBackground);
            state.progress = ProgressStates.UNSAVED;
            redraw();
        } catch (Exception e) {
            Dialogs.exceptionDialog(e, "Failed to generate background.", "Something went wrong, very wrong. The stacktrace might help you.");
            lastStatus.setText("Background generation failed.");
        }
    }

    @FXML
    private void generateFlashmap() {
        if(state.creation == CreationStates.UNDEFINED) { Dialogs.dialog(AlertType.ERROR, "Invalid operation.", "Load a map to generate a flashmap."); return; }
        if(toggleFlashmap.getSelectedToggle() == toggleFlashmapCustom && !Files.exists(Paths.get(state.flashSheetPath))) {
            Dialogs.dialog(AlertType.ERROR, "Could not find custom flashmap sheet", "Failed to resolve path: "+state.flashSheetPath+"\nPlease select a new custom flash sheet or use the default option.");
            lastStatus.setText("Failed to resolve path for custom flashmap.");
            return;
        }
        try {
            boolean external = ((RadioMenuItem) toggleFlashmap.getSelectedToggle()) == toggleFlashmapCustom;
            MapToImageConverter m = new MapToImageConverter(currentMap.getData(), state.flashSheetPath, 25, external);
            currentMap.setBackgroundFlash(m.getAsFxImage());
            lastStatus.setText("Generated flashmap.");
            state.renderSettings.backgroundState = BackgroundRenderStates.FLASHMAP;
            toggleRenderAs.selectToggle(toggleRenderAsFlashmap);
            state.progress = ProgressStates.UNSAVED;
            redraw();
        } catch (Exception e) {
            Dialogs.exceptionDialog(e, "Failed to generate flashmap.", "Something went wrong, very wrong. The stacktrace might help you.");
            lastStatus.setText("Flashmap generation failed");
        }
    }

    @FXML
    private void loadBackgroundImage() {
        Image i = sharedBgImage("Select a background image");
        if(i != null) {
            currentMap.setBackground(i);
            lastStatus.setText("Loaded Background Image. Preview -> Show with Background to see.");
            Dialogs.dialog(AlertType.INFORMATION, "Operation succeeded", "Loaded Background Image. Preview -> Show with Background to see.");
        }
    }

    @FXML
    private void loadFlashmapImage() {
        Image i = sharedBgImage("Select a flashmap image");
        if(i != null) {
            currentMap.setBackgroundFlash(i);
            lastStatus.setText("Loaded Flashmap Image. Preview -> Show with Flashmap to see.");
            Dialogs.dialog(AlertType.INFORMATION, "Operation succeeded", "Loaded Flashmap Image. Preview -> Show with Flashmap to see.");
        }
    }

    @FXML
    private void showHelp() {
        Dialogs.help();
    }

    @FXML
    private void showCredits() {
        Dialogs.showCredits();
    }

    @FXML
    private void about() {
        Dialogs.dialog(AlertType.INFORMATION, "__HackForDialog", "GNUMAN Editor 1.0 by Marc Herschel");
    }

    private void updateCreationState(CreationStates state) {
        if (state == CreationStates.UNDEFINED) {
            save.setDisable(true);
            saveAs.setDisable(true);
        } else {
            save.setDisable(false);
            saveAs.setDisable(false);
        }
        this.state.creation = state;
    }

    private void updateSelectedTools() {
        selectedOperation.setText("Selected: " + state.tool + (state.tool == Tools.LINE || state.tool == Tools.DRAW ? " -> " + selectedBlock : ""));
        state.resetAStarTool();
        state.renderSettings.withAStar = false;
    }

    private void suspendLineTool() { redraw(); }

    private ParsingStatus validateMap() {
        if(state.isLoaded()) { return MapParser.validate(currentMap); }
        return null;
    }

    private void scaleCanvas() {
        canvas.setWidth(canvasPane.getWidth());
        canvas.setHeight(canvasPane.getHeight());
        if(state.automaticFit || currentMap == null || state.creation == CreationStates.UNDEFINED) {
            windowDependentScale();
            redraw(); return;
        }
        if(!state.automaticFit && currentMap != null && state.creation != CreationStates.UNDEFINED) {
            scale = (int) scaleSlider.getValue();
            redraw(); return;
        }
    }

    private void windowDependentScale() {
        scale = (currentMap == null) ? 0 : Integer.min((int) Math.floor((int) canvas.getWidth()/currentMap.getWidthInBlocks()), (int) Math.floor((int)canvas.getHeight()/currentMap.getHeightInBlocks()));
        if(scale > 0) { scaleSlider.setValue(scale); }
    }

    private void redraw() {
        Helper.redraw(currentMap, canvas, canvasPane, viewPort, state, textures, scale, pathList);
    }

    private void sharedSaving(boolean saveAs) {
        ParsingStatus p = validateMap();
        if(!p.isSuccess()) { Dialogs.dialog(AlertType.ERROR, "Illegal Operation", "Errorous maps are unable to be saved.\nPlease fix your map first.\n\n"+p.statusMessage()); return; }
        if(state.creation == CreationStates.LOADED && !saveAs) {
            if(Dialogs.yesOrNoDialog("Overriding map.", "Do you wish to override the map stored at:\n" + state.path + "?")) {
                saveProcess(state.path);
            }
            return;
        }
        saveProcess(savePicker());
    }

    private void saveProcess(String path) {
        try {
            MapParser.dumpMap(currentMap, path);
            Dialogs.dialog(AlertType.INFORMATION, "Saving completed.", "Map has been saved successfully!");
            lastStatus.setText("Saved map under: " + path);
            state.progress = ProgressStates.SAVED;
            state.creation = CreationStates.LOADED;
            state.path = path;
        } catch (Exception e) {
            Dialogs.exceptionDialog(e, "Saving failed.", "We encountered a critical error while attempting to save your map!");
            lastStatus.setText("Saving of the map failed: " + e.getClass().getSimpleName());
        }
    }

    private void saveProcess(File file) {
        if(file == null) { return; }
        saveProcess(file.getAbsolutePath());
    }

    private File savePicker() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save GNUMAN map");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Gnuman Map", "*.gnuman"));
        return fileChooser.showSaveDialog(stage);
    }

    private void unloadMap() {
        redo.clear(); undo.clear();
        state.resetLineTool();
        state.resetAStarTool();
        state.renderSettings.withAStar = false;
        stage.setTitle("GNUMAN Editor :: Ready");
        lastStatus.setText("Load/Create a new map to begin.");
        updateCreationState(CreationStates.UNDEFINED);
        state.progress = ProgressStates.SAVED;
        state.path = null;
        currentMap = null;
        scale = 0;
        disableZoom();
        automaticFit.setDisable(true);
    }

    private Image sharedBgImage(String title) {
        if(state.creation == CreationStates.UNDEFINED) { Dialogs.dialog(AlertType.ERROR, "Illegal operation", "Load a map to set a background."); return null; }
        File f = getImageLocation(title);
        if(f == null) { return null; }
        try {
            return new Image(new FileInputStream(f));
        } catch (Exception e) {
            Dialogs.exceptionDialog(e, "Failed to load image.", "Something went wrong while loading your image.");
            return null;
        }
    }

    private File getImageLocation(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new ExtensionFilter("PNG Image", "*.png"));
        return fileChooser.showOpenDialog(stage);
    }

    private void checkBackgroundImageSetting(RadioMenuItem selectedToggle) {
        if(selectedToggle == toggleRenderAsBlocks) { state.renderSettings.backgroundState = BackgroundRenderStates.BLOCKS_COLOR; }
        if(selectedToggle == toggleRenderAsBackground) {
            if(currentMap != null && currentMap.getBackground() != null) {
                state.renderSettings.backgroundState = BackgroundRenderStates.BACKGROUND;
            } else {
                Dialogs.dialog(AlertType.ERROR, "Illegal Operation", "You must load a map with a valid background image.");
                toggleRenderAs.selectToggle(toggleRenderAsBlocks);
            }
        }
        if(selectedToggle == toggleRenderAsFlashmap) {
            if(currentMap != null && currentMap.getBackgroundFlash() != null) {
                state.renderSettings.backgroundState = BackgroundRenderStates.FLASHMAP;
            } else {
                Dialogs.dialog(AlertType.ERROR, "Illegal Operation", "You must load a map with a valid flashmap image.");
                toggleRenderAs.selectToggle(toggleRenderAsBlocks);
            }
        }
        scaleCanvas();
    }

    private void backToGnuman() {
        stage.close();
        GameLauncher.show();
        AudioManager.startUiMusic();
    }

    private void disableZoom() {
        if(!state.automaticFit) { automaticFit.setSelected(true); state.automaticFit = true; scaleSlider.setDisable(true); }
        viewPort.reset();
    }

    private void buildMapFoundation() {
        for(int y = 0; y < currentMap.getHeightInBlocks(); y++) {
            for(int x = 0; x < currentMap.getWidthInBlocks(); x++) {
                currentMap.setMapData(StaticObjects.EMPTY, x, y);
            }
        }
        for(int hy = 0; hy < Constants.GHOST_HOUSE.length; hy++) {
            for(int hx=0; hx < Constants.GHOST_HOUSE[hy].length; hx++) {
                currentMap.setMapData(StaticObjects.values()[Constants.GHOST_HOUSE[hy][hx]], hx+ghostHouseXOffset(), hy+ghostHouseYOffset()+1);
            }
        }
        currentMap.initiateEmptySlowdown();
    }

    private int ghostHouseXOffset() { return (currentMap.getWidthInBlocks()-8)/2; }
    private int ghostHouseYOffset() { return -1+(currentMap.getHeightInBlocks()-8)/2; }
    private boolean inGhostHouseArea(int x, int y) { return x >= ghostHouseXOffset() && x < ghostHouseXOffset()+8 && y >= ghostHouseYOffset()+1 && y < ghostHouseYOffset()+7; }
    private boolean inGhostHouseAreaForModifier(int x, int y) { return x >= ghostHouseXOffset() && x < ghostHouseXOffset()+8 && y > ghostHouseYOffset()+1 && y < ghostHouseYOffset()+7; }

    private void modifier(int x, int y) {
        if(!inGhostHouseAreaForModifier(x, y) && currentMap.getData()[y][x] != StaticObjects.GHOST_SPAWN && currentMap.getData()[y][x] != StaticObjects.WALL) {
            Dialogs.modifierDialog(x, y, currentMap, lastStatus);
            redraw();
        } else {
            Dialogs.dialog(AlertType.INFORMATION, "Modifier not applicable here", "This block is not applicable for a modifier. Reasons might be:\n\n- Block is a wall/ghost spawn\n- Block is inside the protected area of the ghost house");
        }
    }

    private void inspector(int x, int y) {
        boolean walkable = currentMap.getData()[y][x] != StaticObjects.WALL;
        StringBuilder sb = new StringBuilder();
        if(walkable) {
            sb.append("\nGhost Movement Decision Properties:\n");
            ArrayList<String> tmp = new ArrayList<>(4);
            currentMap.getBlocked().forEach((k, v) -> tmp.add(String.format("%s %s", k, v.contains(new MapCell(x, y)) ? "is blocked." : "is possible.")));
            Collections.sort(tmp);
            tmp.forEach(s -> sb.append(s+"\n"));
        }
        String movementRestrictionProperties = sb.toString();
        Dialogs.dialog(AlertType.INFORMATION, "Inspector", String.format( "[X=%d, Y=%d]\n\nBlocktype is %s.\n%s\n%s\n%s", x, y, currentMap.getData()[y][x], (walkable && currentMap.getData()[y][x] == StaticObjects.INVISIBLE_PLAYER_WALL) ? "This block is only walkable by ghosts." : (walkable ? "This block is walkable." : "This block is not walkable."), walkable ? movementRestrictionProperties : "", walkable ? (currentMap.getSlowdown()[x][y] ? "This block slows down ghosts." : "Ghosts move at their normal speed.") : ""));
    }

    private void lineTool(int x, int y, boolean cancel) {
        state.line.addPoint(x, y);
        redraw();
        if(state.line.state != TwoPointsSelectionStates.BOTH_SET) { redraw(); return; }
        if(cancel) { state.resetLineTool(); redraw(); return; }
        Point p1 = state.line.points[0], p2 = state.line.points[1];
        boolean rectangle = !p1.equals(p2) && !(p1.x == p2.x ^ p1.y == p2.y);
        undo.push(rectangle ? rectangleOperation(p1, p2) : lineOperation(p1, p2));
        state.progress = ProgressStates.UNSAVED;
        state.line.reset();
        redraw();
    }

    private BatchBlockModificationOperation lineOperation(Point p1, Point p2) {
        boolean horizontal = p1.x != p2.x;
        int min = horizontal ? Math.min(p1.x, p2.x) : Math.min(p1.y, p2.y);
        int max = horizontal ? Math.max(p1.x, p2.x) : Math.max(p1.y, p2.y);
        ArrayList<Point> cells = new ArrayList<>(max-min);
        ArrayList<StaticObjects> blocks = new ArrayList<>(max-min);
        for(int m = min; m <= max; m++) {
            int mx = horizontal ? m : p1.x, my = horizontal ? p1.y : m;
            if(!inGhostHouseArea(mx, my)) {
                blocks.add(currentMap.getData()[my][mx]);
                cells.add(new Point(mx, my));
                currentMap.setMapData(selectedBlock, mx, my);
            }
        }
        return new BatchBlockModificationOperation(blocks, cells);
    }

    private BatchBlockModificationOperation rectangleOperation(Point p1, Point p2) {
        int minX = Math.min(p1.x, p2.x), maxX = Math.max(p1.x, p2.x), minY = Math.min(p1.y, p2.y), maxY = Math.max(p1.y, p2.y);
        ArrayList<Point> cells = new ArrayList<>();
        ArrayList<StaticObjects> blocks = new ArrayList<>();
        for(int y = minY; y <= maxY; y++) {
            for(int x = minX; x <= maxX; x++) {
                if(!inGhostHouseArea(x, y)) {
                    blocks.add(currentMap.getData()[y][x]);
                    cells.add(new Point(x, y));
                    currentMap.setMapData(selectedBlock, x, y);
                }
            }
        }
        return new BatchBlockModificationOperation(blocks, cells);
    }

    private void drawOrRemove(int x, int y) {
        if(state.tool == Tools.DRAW) {
            if(currentMap.getData()[y][x] == selectedBlock) { return; }
            int s = 0; for(StaticObjects[] o : currentMap.getData()) for(StaticObjects k : o) { if(k == StaticObjects.PLAYER_SPAWN) s++; } if(s >= 1 && selectedBlock == StaticObjects.PLAYER_SPAWN) { return; }
        }
        if(state.tool == Tools.ERASE ) {
            currentMap.getSlowdown()[x][y] = false;
            if(currentMap.getData()[y][x] == StaticObjects.EMPTY) { redraw(); return; }
        }
        undo.push(new BlockModificationOperation(currentMap.getData()[y][x], new Point(x, y)));
        currentMap.setMapData(state.tool == Tools.DRAW ? selectedBlock : StaticObjects.EMPTY, x, y);
        state.progress = ProgressStates.UNSAVED;
        redraw();
    }

    /*
     * The +1, -1 operations here are because the A Star creates a dummy grid for teleportations.
     */
    private void aStarTool(int x, int y) {
        if(state.renderSettings.withAStar) { state.renderSettings.withAStar = false; }
        state.aStar.addPoint(x, y);
        redraw();
        if(state.aStar.state != TwoPointsSelectionStates.BOTH_SET) { redraw(); return; }
        PathNode[][] nodes = new PathNode[currentMap.getHeightInBlocks()+2][currentMap.getWidthInBlocks()+2];
        Helper.preparePathNodesWithTeleportation(nodes, currentMap.getPlayerNavigationData());
        AStarPathfinding p = new AStarPathfinding(nodes);
        long before = System.nanoTime();
        pathList = p.findPath(state.aStar.points[0].x+1, state.aStar.points[0].y+1, state.aStar.points[1].x+1, state.aStar.points[1].y+1);
        long after = System.nanoTime();
        if(pathList.size() == 0) {
            boolean same = state.aStar.points[0].equals(state.aStar.points[1]);
            Dialogs.dialog(AlertType.INFORMATION, same ? "Same point, no path." : "No way found.", same ? "You selected the same point as start/target." : "There is no possible way for players to reach the target.");
            state.resetAStarTool();
        } else {
            String trace = pathList.stream()
                    .map(n -> String.format("(X: %d | Y: %d)", n.getX()-1, n.getY()-1))
                    .collect(Collectors.joining("\n"));
            Dialogs.aStarDialog("Found a path.", String.format("Calculated in %.8f seconds.\n\nFinal moves are %d.\n\n", (after - before)*1e-9, pathList.size()), String.format("From: (X: %d | Y: %d)\n\n", state.aStar.points[0].x, state.aStar.points[0].y) + trace);
            state.renderSettings.withAStar = true;
            state.resetAStarTool();
        }
        redraw();
    }

    private void teleporterFix(boolean remove) {
        if(state.creation == CreationStates.UNDEFINED) { Dialogs.dialog(AlertType.ERROR, "Illegal Operation", "Load a map to fix teleporters by removal."); return; }
        ArrayList<Point> cells = new ArrayList<>();
        ArrayList<StaticObjects> blocks = new ArrayList<>();
        int fixed = 0;
        for(int y = 0; y < currentMap.getHeightInBlocks(); y++) {
            if(currentMap.getData()[y][0] == StaticObjects.WALL ^ currentMap.getData()[y][currentMap.getWidthInBlocks()-1] == StaticObjects.WALL) {
                boolean isFirst = currentMap.getData()[y][0] == StaticObjects.WALL;
                int xHigh = currentMap.getWidthInBlocks()-1;
                if(remove) {
                    cells.add(new Point(isFirst ? 0 : xHigh, y));
                    blocks.add(currentMap.getData()[y][isFirst ? 0 : xHigh]);
                    currentMap.setMapData(StaticObjects.EMPTY, isFirst ? 0 : xHigh, y);
                } else {
                    cells.add(new Point(isFirst ? xHigh : 0, y));
                    blocks.add(currentMap.getData()[y][isFirst ? xHigh : 0]);
                    currentMap.setMapData(StaticObjects.WALL, isFirst ? xHigh : 0, y);
                }
                fixed++;
            }
        }
        for(int x = 0; x < currentMap.getWidthInBlocks(); x++) {
            if(currentMap.getData()[0][x] == StaticObjects.WALL ^ currentMap.getData()[currentMap.getHeightInBlocks()-1][x] == StaticObjects.WALL) {
                boolean isFirst = currentMap.getData()[0][x] == StaticObjects.WALL;
                int yHigh = currentMap.getHeightInBlocks()-1;
                if(remove) {
                    cells.add(new Point(x, isFirst ? 0 : yHigh));
                    blocks.add(currentMap.getData()[isFirst ? 0 : yHigh][x]);
                    currentMap.setMapData(StaticObjects.EMPTY, x, isFirst ? 0 : yHigh);
                } else {
                    cells.add(new Point(x, isFirst ? yHigh : 0));
                    blocks.add(currentMap.getData()[isFirst ? yHigh : 0][x]);
                    currentMap.setMapData(StaticObjects.WALL, isFirst ? yHigh : 0, x);
                }
                fixed++;
            }
        }
        if(fixed == 0) {
            Dialogs.dialog(AlertType.INFORMATION, "Operation complete", "There are no illegal teleporters to fix.");
            return;
        }
        Dialogs.dialog(AlertType.INFORMATION, "Operation complete", "Fixed " + fixed + " teleporter(s).");
        undo.push(new BatchBlockModificationOperation(blocks, cells));
        redraw();
    }

    private void setTitle() {
        stage.setTitle(String.format("GNUMAN Editor :: %s by %s [%dx%d] %s",
                currentMap.getName(),
                currentMap.getAuthor(),
                currentMap.getWidthInBlocks(),
                currentMap.getHeightInBlocks(),
                state.path == null ? "" : " -> "+state.path));
    }

    private void freelook() {
        double speed = ((visibleVerticalBlocks() + visibleHorizontalBlocks()) / 2)*0.1*scale;
        if (keyUp) { viewPort.y -= (viewPort.y-speed >= 0) ? speed : viewPort.y; }
        if (keyDown) { viewPort.y += (viewPort.y+speed < scale*currentMap.getHeightInBlocks()) ? speed : 0; }
        if (keyLeft) { viewPort.x -= (viewPort.x-speed >= 0) ? speed : viewPort.x; }
        if (keyRight) { viewPort.x += (viewPort.x+speed < scale*currentMap.getWidthInBlocks()) ? speed : 0; }
        redraw();
    }

    private int visibleVerticalBlocks() {
        int vY = viewPort.cellY(scale);
        int dataYTo = (int) Math.ceil(canvasPane.getHeight()/scale)+vY <= currentMap.getHeightInBlocks() ? (int) Math.ceil(canvasPane.getHeight()/scale)+vY : currentMap.getHeightInBlocks();
        if(dataYTo+1 < currentMap.getHeightInBlocks()) { dataYTo++; }
        return dataYTo-vY;
    }

    private int visibleHorizontalBlocks() {
        int vX = viewPort.cellX(scale);
        int dataXTo = (int) Math.ceil(canvasPane.getWidth()/scale)+vX <= currentMap.getWidthInBlocks() ? (int) Math.ceil(canvasPane.getWidth()/scale)+vX : currentMap.getWidthInBlocks();
        if(dataXTo+1 < currentMap.getWidthInBlocks()) { dataXTo++; }
        return dataXTo-vX;
    }
}
