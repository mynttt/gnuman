package de.hshannover.inform.gnuman.app.mapeditor;

import java.awt.Point;
import java.util.ArrayList;
import de.hshannover.inform.gnuman.app.enums.gameobjects.StaticObjects;
import de.hshannover.inform.gnuman.app.model.storage.MapData;

/**
 * Undo/Redo for batch block operations.
 * @author Marc Herschel
 */

class BatchBlockModificationOperation implements Operation {
    private ArrayList<StaticObjects> blocks;
    private ArrayList<Point> cells;

    BatchBlockModificationOperation(ArrayList<StaticObjects> blocks, ArrayList<Point> cells) {
        this.blocks = blocks;
        this.cells = cells;
    }

    @Override
    public void execute(MapData toManipulate) {
        StaticObjects tmp;
        for(int i = 0; i < blocks.size(); i++) {
            tmp = toManipulate.getData()[cells.get(i).y][cells.get(i).x];
            toManipulate.setMapData(blocks.get(i), cells.get(i).x, cells.get(i).y);
            blocks.set(i, tmp);
        }
    }
}
