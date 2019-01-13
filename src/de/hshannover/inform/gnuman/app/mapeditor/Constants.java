package de.hshannover.inform.gnuman.app.mapeditor;

import javafx.scene.paint.Color;

/**
 * Constants for the map editor.
 * @author Marc Herschel
 */

final class Constants {

    final static String MAP_TILE_SET = de.hshannover.inform.gnuman.Constants.TEXTURE_PATH_PREFIX + "editor/mapSpriteSheet.png";
    final static String MAP_TILE_SET_ONE_COLOR = de.hshannover.inform.gnuman.Constants.TEXTURE_PATH_PREFIX + "editor/mapSpriteSheet_oneColor.png";
    final static String MAP_TILE_SET_FLASH = de.hshannover.inform.gnuman.Constants.TEXTURE_PATH_PREFIX + "editor/mapSpriteSheet_flashmap.png";

    final static int[][] GHOST_HOUSE = {
            {0, 0, 0, 0, 4, 0, 0, 0},
            {1, 1, 1, 6, 6, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 1},
            {1, 8, 0, 8, 0, 8, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1}
    };

    final static Color[] EDITOR_BACKGROUND_COLOR = {
            Color.DARKGRAY,         //Empty
            Color.BLACK,            //Wall
            Color.web("#ffc231"),   //Food (Cell)
            Color.web("#ff5836"),   //Power up (Cell)
            Color.DARKGRAY,         //Ghost Spawn
            Color.DARKGREEN,        //Player Spawn
            Color.DARKGRAY,         //Invisible Player Wall
            Color.web("#FF2A99"),   //Special Item Spawn
            Color.DARKGRAY          //Ghost House Spawn
    };
}
