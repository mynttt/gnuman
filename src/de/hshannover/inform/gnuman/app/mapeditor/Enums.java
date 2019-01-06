package de.hshannover.inform.gnuman.app.mapeditor;

class Enums {

    enum BackgroundRenderStates {
        BLOCKS_COLOR, BACKGROUND, FLASHMAP
    }

    enum ProgressStates {
        SAVED, UNSAVED
    }

    enum CreationStates {
        NEW, LOADED, UNDEFINED
    }

    enum Tools {
        DRAW, LINE, ERASE, MODIFIER, INSPECTOR, A_STAR_ALGORITHM
    }

    enum TwoPointsSelectionStates {
        NONE, FIRST_SET, BOTH_SET
    }

}
