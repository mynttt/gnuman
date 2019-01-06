package de.hshannover.inform.gnuman.app.mapeditor;

/**
 * Simple camera for zooming and moving arround the map.
 * @author Marc Herschel
 */

class ViewPort {
    double x, y;

    void reset() {
        x = y = 0;
    }

    int cellX(int scale) { return (int) x / scale; }
    int cellY(int scale) { return (int) y / scale; }

    private double skipNPixelsFromFirstX(int scale) {
        return Math.abs((x < scale ? scale - x : Math.abs((x - scale) - cellX(scale) * scale)) - scale);
    }

    private double skipNPixelsFromFirstY(int scale) {
        return Math.abs((y < scale ? scale - y : Math.abs((y - scale) - cellY(scale) * scale)) - scale);
    }

    int interpolatedCellX(double posX, int scale) {
        if(posX <= scale - skipNPixelsFromFirstX(scale)) {
            return (int) (x + posX - skipNPixelsFromFirstX(scale)) / scale;
        } else {
            return (int) (x + (scale - skipNPixelsFromFirstX(scale)) + posX - (scale - skipNPixelsFromFirstX(scale))) / scale;
        }
    }

    int interpolatedCellY(double posY, int scale) {
        if(posY <= scale - skipNPixelsFromFirstY(scale)) {
            return (int) (y + posY - skipNPixelsFromFirstY(scale)) / scale;
        } else {
            return (int) (y + (scale - skipNPixelsFromFirstY(scale)) + posY - (scale - skipNPixelsFromFirstY(scale))) / scale;
        }
    }

}
