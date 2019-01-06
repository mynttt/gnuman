package de.hshannover.inform.gnuman.app.model;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;

/**
 * Superclass for objects on the map.
 * @author Marc Herschel
 */

public class GameObject {
    private double x, y;
    private int w, h;

    public GameObject(double x, double y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h= h;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double newX) { x = newX; }
    public void setY(double newY) { y = newY; }
    public int getWidth() { return w; }
    public int getHeight() { return h; }
    public BoundingBox getBounds() { return new BoundingBox(x, y, w, h); }
    public boolean intersects(BoundingBox other) { return doesCollide(x, y, w, h, other); }
    public boolean doesCollide(double x, double y, double w, double h, Bounds other) { if (other.isEmpty() || w < 0 || h < 0) return false; return (x + w >= other.getMinX() && y + h >= other.getMinY() && x <= other.getMaxX() && y <= other.getMaxY()); }
    @Override
    public String toString() { return "[X: " + x + "  | Y: " + y + "]"; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + h;
        result = prime * result + w;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        GameObject other = (GameObject) obj;
        if (h != other.h)
            return false;
        if (w != other.w)
            return false;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        return true;
    }
}
