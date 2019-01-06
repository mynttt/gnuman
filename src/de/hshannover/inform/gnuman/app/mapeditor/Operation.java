package de.hshannover.inform.gnuman.app.mapeditor;

import de.hshannover.inform.gnuman.app.model.storage.MapData;

/**
 * Shared interface for all Undo/Redo implementations.
 * @author Marc Herschel
 */

interface Operation {
    void execute(MapData toManipulate);
}
