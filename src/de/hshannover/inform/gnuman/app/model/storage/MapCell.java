package de.hshannover.inform.gnuman.app.model.storage;

/**
 * Represents a cell on the map.
 * @author Marc Herschel
 */

public class MapCell {
    private int cellX, cellY;

    public MapCell(int cellX, int cellY) {
        this.cellX = cellX;
        this.cellY = cellY;
    }

    public int getCellX() { return cellX; }
    public int getCellY() { return cellY; }

    /**
     * Check collisions with other cells.
     * @param x cell to check
     * @param y cell to check
     * @return true if collision
     */
    public boolean collision(int x, int y) { return cellX == x && cellY == y; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + cellX;
        result = prime * result + cellY;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MapCell other = (MapCell) obj;
        if (cellX != other.cellX)
            return false;
        if (cellY != other.cellY)
            return false;
        return true;
    }

    @Override
    public String toString() { return "Cell: X-Cell -> " + cellX + " Y-Cell -> " + cellY; }
}
