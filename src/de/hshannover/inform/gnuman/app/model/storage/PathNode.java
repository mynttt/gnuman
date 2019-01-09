package de.hshannover.inform.gnuman.app.model.storage;

/**
 * Node used by the A* Pathfinding.
 * @author Marc Herschel
 */

public class PathNode {
    private int x, y, heuristic, g;
    private boolean maneuverable;
    private PathNode parent;

    /**
     * Constructs a Node that is used by the A* Algorithm.
     * @param x x-Position of the Node in the MapData array.
     * @param y y-Position of the Node in the MapData array.
     * @param maneuverable If the player can move on the node.
     */
    public PathNode(int x, int y, boolean maneuverable) {
        this.x = x;
        this.y = y;
        this.maneuverable = maneuverable;
    }

    /**
     * We use the Manhattan distance as heuristic because movement is restricted in four directions.
     * @param target Our target.
     */
    public void calculateHeuristic(PathNode target) {
        heuristic = Math.abs(getX() - target.getX()) + Math.abs(getY() - target.getY());
    }

    /**
     * @return x-Position of the node in the map array.
     */
    public int getX() {
        return x;
    }

    /**
     * @return y-Position of the node in the map array.
     */
    public int getY() {
        return y;
    }

    /**
     * Is the entity even allowed to step on us?
     * @return true if entities can step on the node.
     */
    public boolean isManeuverable() {
        return maneuverable;
    }

    /**
     * Get the predecessor of our node.
     * @return predecessor of our node.
     */
    public PathNode getParent() {
        return parent;
    }

    /**
     * Set the predecessor of our node.
     * @param parent predecessor of our node.
     */
    public void setParent(PathNode parent) {
        this.parent = parent;
    }

    /**
     * Return the cost of our node.
     * @return cost of node
     */
    public int getCost() {
        return g + heuristic;
    }

    /**
     * Sets the G-Variable that tracks the distance between start and the current node.
     * @param parentG Prior node to get the value from.
     */
    public void setG(PathNode parentG) {
        g = parent.getG() + 1;
    }

    /**
     * @return the G-Variable of the selected node.
     */
    public int getG() {
        return g;
    }

    /**
     * Returns the G-Variable of a this Node using another Nodes prior value to build on but doesn't set it.
     * @param parentG parent node of g node to preview
     * @return preview g variable without setting it
     */
    public int previewG(PathNode parentG) {
        return parentG.getG() + 1;
    }

    @Override
    public String toString() {
        return String.format("(X: %d | Y: %d | Heuristic: %d)", x, y, heuristic);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PathNode)) { return false; }
        return ((PathNode) o).getX() == x && ((PathNode) o).getY() == y && ((PathNode) o).isManeuverable() == maneuverable;
    }
}
