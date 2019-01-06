package de.hshannover.inform.gnuman.app.mapeditor;

import java.awt.Point;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.model.storage.MapData;

/**
 * Undo/Redo for simple block operations.
 * @author Marc Herschel
 */

class BlockModificationOperation implements Operation {
    private StaticObjects block;
    private Point cell;

    BlockModificationOperation(StaticObjects block, Point cell) {
        this.cell = cell;
        this.block = block;
    }

    @Override
    public void execute(MapData toManipulate) {
        StaticObjects tmp = toManipulate.getData()[cell.y][cell.x];
        toManipulate.setMapData(block, cell.x, cell.y);
        block = tmp;
    }
}
