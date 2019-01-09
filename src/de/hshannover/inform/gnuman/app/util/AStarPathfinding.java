package de.hshannover.inform.gnuman.app.util;

import java.util.List;
import java.util.LinkedList;
import de.hshannover.inform.gnuman.app.model.storage.PathNode;

/**
 * Implementation of A* Pathfinding for 4 directions.
 * @author Marc Herschel
 */

public class AStarPathfinding {
    private int width, height;
    private PathNode[][] nodes;

    /**
     * Construct with already preset nodes.
     * @param nodes to use
     */
    public AStarPathfinding(PathNode[][] nodes) {
        this.width = nodes[0].length;
        this.height = nodes.length;
        this.nodes = nodes;
    }

    /**
     * Constructs an AStar object that allows for pathfinding.
     * @param map 2D boolean array, true => Walkable
     */
    public AStarPathfinding(boolean[][] map) {
        this.width = map[0].length;
        this.height = map.length;
        this.nodes = new PathNode[this.height][this.width];
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                nodes[y][x] = new PathNode(x, y, map[y][x]);
            }
        }
    }

    /**
     * The actual A-Star algorithm.<br>
     * Heuristic (h) uses the Manhattan distance because we only move UP, DOWN, LEFT, RIGHT.
     * Cost (f) uses the heuristic value.
     * (g) is not even needed because the Manhattan distance alone seems to work perfectly.
     * @param startX Start from X
     * @param startY Start from Y
     * @param targetX Our target X
     * @param targetY Our target Y
     * @return An empty list if impossible or already at the position, else a list of nodes that represent the path.
     */
    public final LinkedList<PathNode> findPath(int startX, int startY, int targetX, int targetY) {

        //Case 1: Start == Position
        if(startX == targetX && startY == targetY) {
            return new LinkedList<>();
        }

        //Nodes have been visited
        List<PathNode> openList = new LinkedList<>();
        //Nodes have been expanded
        List<PathNode> closedList = new LinkedList<>();

        //Visit our starting node
        openList.add(nodes[startY][startX]);

        while(true) {
            PathNode current = getNodeWithLowestCost(openList);
            openList.remove(current);
            closedList.add(current);

            //Case 2: We arrived at our target position, halt and backtrack the path.
            if((current.getX() == targetX) && (current.getY() == targetY)) {
                return calcPath(nodes[startY][startX], current);
            }

            //Explore neighbors.
            List<PathNode> neighboringNodes = getNeighbors(current, closedList);
            for(PathNode neighbor : neighboringNodes) {
                if(!openList.contains(neighbor)) {
                    neighbor.setParent(current);
                    neighbor.calculateHeuristic(nodes[targetY][targetX]);
                    neighbor.setG(current);
                    openList.add(neighbor);
                } else if (neighbor.getG() > neighbor.previewG(current)) {
                    neighbor.setParent(current);
                    neighbor.setG(current);
                }
            }

            //Case 3: There is no possible path.
            if(openList.isEmpty()) {
                return new LinkedList<>();
            }
        }
    }

    /**
     * Build the path via backtracking from our target node.
     * @param start Start node.
     * @param target Target node.
     * @return A set of nodes that contain the path from start to target.
     */
    private LinkedList<PathNode> calcPath(PathNode start, PathNode target) {
        LinkedList<PathNode> path = new LinkedList<>();
        PathNode currentNode = target;
        while(!currentNode.equals(start)) {
            path.addFirst(currentNode);
            currentNode = currentNode.getParent();
        }
        return path;
    }

    /**
     * Find the node with the lowest cost.
     * @param list of nodes to search in.
     * @return the node with the lowest cost of the list.
     */
    private PathNode getNodeWithLowestCost(List<PathNode> list) {
        PathNode lowestCost = list.get(0);
        for(PathNode node : list) {
            if(node.getCost() < lowestCost.getCost()) {
                lowestCost = node;
            }
        }
        return lowestCost;
    }

    /**
     * Discover neighbors for the node.
     * @param node To search neighbors for.
     * @param closedList List of nodes that had already have their neighbors explored for.
     * @return A list of neighbors of the specified nodes.
     */
    private List<PathNode> getNeighbors(PathNode node, List<PathNode> closedList) {
        List<PathNode> neighbours = new LinkedList<>();
        int x = node.getX();
        int y = node.getY();

    //Add up to 4 possible neighbor nodes and avoid out of bound exceptions.
        if(x > 0) { addNeighbourIfPossible(this.getNode(x - 1, y), neighbours, closedList); }
        if(x < width - 1) { addNeighbourIfPossible(this.getNode(x + 1, y), neighbours, closedList); }
        if(y > 0) { addNeighbourIfPossible(this.getNode(x, y - 1), neighbours, closedList); }
        if(y < height - 1) { addNeighbourIfPossible(this.getNode(x, y + 1), neighbours, closedList); }

        return neighbours;
    }

    /**
     * If the node is even to be considered to be a neighbor.<br>
     * That is the case if it is not null, the player is able to move onto and it has not been fully explored yet.
     * @param node node to evaluate
     * @param neighbours neighbours to search against
     * @param closed closed list
     */
    private void addNeighbourIfPossible(PathNode node, List<PathNode> neighbours, List<PathNode> closed) {
        if(node != null && node.isManeuverable() && !closed.contains(node)) {
            neighbours.add(node);
        }
    }

    /**
     * Get a node at a certain position.
     * @param x x-Position of the node.
     * @param y y-Position of the node.
     * @return The node at the specified position.
     */
    private PathNode getNode(int x, int y) {
        return nodes[y][x];
    }
}
