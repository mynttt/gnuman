package de.hshannover.inform.gnuman.app.mapeditor;

import de.hshannover.inform.gnuman.app.mapeditor.Enums.BackgroundRenderStates;
import de.hshannover.inform.gnuman.app.mapeditor.Enums.CreationStates;
import de.hshannover.inform.gnuman.app.mapeditor.Enums.ProgressStates;
import de.hshannover.inform.gnuman.app.mapeditor.Enums.Tools;

/**
 * State of the editor.
 * @author Marc Herschel
 */

class State {
    CreationStates creation;
    ProgressStates progress;
    Tools tool;
    String path, spriteSheetPath, flashSheetPath;
    RenderSettings renderSettings;
    TwoPointInput line, aStar;
    boolean automaticFit;

    class RenderSettings {
        boolean drawGrid, withItems, withModifiers, withAStar;
        BackgroundRenderStates backgroundState;
        double arrowY, arrowX;

        RenderSettings() {
            drawGrid = withModifiers = true;
            backgroundState = BackgroundRenderStates.BLOCKS_COLOR;
        }
    }

    State() {
        creation = CreationStates.UNDEFINED;
        progress = ProgressStates.SAVED;
        tool = Tools.DRAW;
        path = null;
        automaticFit = true;
        spriteSheetPath = Constants.MAP_TILE_SET;
        flashSheetPath = Constants.MAP_TILE_SET_FLASH;
        renderSettings = new RenderSettings();
        line = new TwoPointInput();
        aStar = new TwoPointInput();
    }

    boolean isLoaded() {
        return creation != CreationStates.UNDEFINED;
    }

    boolean canExitWithoutAsking() {
        return !isLoaded() || (isLoaded() && progress == ProgressStates.SAVED);
    }

    void resetLineTool() { line.reset(); }
    void resetAStarTool() { aStar.reset(); }
}
