package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * A* pathfinding on a tiled collision layer.
 */
public class Pathfinder {
    /** X offsets for cardinal movement. */
    private static final int[] DIR_X = {1, -1, 0, 0};
    /** Y offsets for cardinal movement. */
    private static final int[] DIR_Y = {0, 0, 1, -1};
    /** Collision layer used to check walkability. */
    private final TiledMapTileLayer collisionLayer;

    /**
     * Creates a pathfinder for a collision layer.
     *
     * @param collisionLayer collision layer
     */
    public Pathfinder(TiledMapTileLayer collisionLayer) {
        this.collisionLayer = collisionLayer;
    }

    /**
     * Finds a path between start and goal coordinates.
     *
     * @param startX start tile x
     * @param startY start tile y
     * @param goalX goal tile x
     * @param goalY goal tile y
     * @return list of path points, empty if none
     */
    public ArrayList<GridPoint2> findPath(int startX, int startY, int goalX, int goalY) {
        int width = collisionLayer.getWidth();
        int height = collisionLayer.getHeight();

        startX = clampCoord(startX, width);
        startY = clampCoord(startY, height);
        goalX = clampCoord(goalX, width);
        goalY = clampCoord(goalY, height);

        GridPoint2 start = findClosestWalkable(startX, startY);
        GridPoint2 goal = findClosestWalkable(goalX, goalY);
        if (start == null || goal == null) {
            return new ArrayList<>();
        }
        if (start.equals(goal)) {
            return new ArrayList<>();
        }

        float[][] gScore = new float[width][height];
        boolean[][] closed = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(gScore[x], Float.POSITIVE_INFINITY);
        }

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Node startNode = new Node(start.x, start.y, 0f, manhattan(start.x, start.y, goal.x, goal.y), null);
        gScore[start.x][start.y] = 0f;
        open.add(startNode);

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (closed[current.x][current.y]) {
                continue;
            }
            if (current.x == goal.x && current.y == goal.y) {
                return reconstructPath(current, start.x, start.y);
            }
            closed[current.x][current.y] = true;

            for (int i = 0; i < 4; i++) {
                int nx = current.x + DIR_X[i];
                int ny = current.y + DIR_Y[i];
                if (!isWalkable(nx, ny) || closed[nx][ny]) {
                    continue;
                }
                float tentativeG = current.g + 1f;
                if (tentativeG < gScore[nx][ny]) {
                    gScore[nx][ny] = tentativeG;
                    float f = tentativeG + manhattan(nx, ny, goal.x, goal.y);
                    open.add(new Node(nx, ny, tentativeG, f, current));
                }
            }
        }

        return new ArrayList<>();
    }

    // Sometimes due floating point precision (or the player going out of bounds),
    // the coordinates can be -1 or width/height, clamp them to the min/max allowed.
    /**
     * Clamps a coordinate to the valid map range.
     *
     * @param value coordinate value
     * @param maxExclusive max exclusive bound
     * @return clamped coordinate
     */
    public int clampCoord(int value, int maxExclusive) {
        if (value < 0) {
            return 0;
        }
        if (value >= maxExclusive) {
            return maxExclusive - 1;
        }
        return value;
    }

    /**
     * Reconstructs the path from a terminal node.
     *
     * @param node terminal node
     * @param startX start tile x
     * @param startY start tile y
     * @return reconstructed path
     */
    private ArrayList<GridPoint2> reconstructPath(Node node, int startX, int startY) {
        ArrayDeque<GridPoint2> reversed = new ArrayDeque<>();
        Node current = node;
        while (current != null) {
            if (!(current.x == startX && current.y == startY)) {
                reversed.addFirst(new GridPoint2(current.x, current.y));
            }
            current = current.parent;
        }
        return new ArrayList<>(reversed);
    }

    /**
     * Manhattan distance heuristic.
     *
     * @param x current x
     * @param y current y
     * @param goalX goal x
     * @param goalY goal y
     * @return manhattan distance
     */
    private float manhattan(int x, int y, int goalX, int goalY) {
        return Math.abs(goalX - x) + Math.abs(goalY - y);
    }

    /**
     * Checks whether a tile is walkable.
     *
     * @param x tile x
     * @param y tile y
     * @return {@code true} if walkable
     */
    private boolean isWalkable(int x, int y) {
        if (x < 0 || y < 0 || x >= collisionLayer.getWidth() || y >= collisionLayer.getHeight()) {
            return false;
        }
        return collisionLayer.getCell(x, y) == null;
    }

    /**
     * Finds the closest walkable tile to a starting position.
     *
     * @param startX start tile x
     * @param startY start tile y
     * @return closest walkable tile, or {@code null} if none
     */
    private GridPoint2 findClosestWalkable(int startX, int startY) {
        if (isWalkable(startX, startY)) {
            return new GridPoint2(startX, startY);
        }

        int width = collisionLayer.getWidth();
        int height = collisionLayer.getHeight();
        boolean[][] visited = new boolean[width][height];
        ArrayDeque<GridPoint2> queue = new ArrayDeque<>();
        queue.add(new GridPoint2(startX, startY));
        visited[startX][startY] = true;

        while (!queue.isEmpty()) {
            GridPoint2 current = queue.poll();
            for (int i = 0; i < 4; i++) {
                int nx = current.x + DIR_X[i];
                int ny = current.y + DIR_Y[i];
                if (nx < 0 || ny < 0 || nx >= width || ny >= height || visited[nx][ny]) {
                    continue;
                }
                if (isWalkable(nx, ny)) {
                    return new GridPoint2(nx, ny);
                }
                visited[nx][ny] = true;
                queue.add(new GridPoint2(nx, ny));
            }
        }
        return null;
    }

    /**
     * Node used by the A* search.
     */
    private static class Node {
        /** Tile x coordinate. */
        private final int x;
        /** Tile y coordinate. */
        private final int y;
        /** Cost from start. */
        private final float g;
        /** Estimated total cost. */
        private final float f;
        /** Parent node for path reconstruction. */
        private final Node parent;

        /**
         * Creates a node for the search.
         *
         * @param x tile x
         * @param y tile y
         * @param g cost from start
         * @param f total estimated cost
         * @param parent parent node
         */
        private Node(int x, int y, float g, float f, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = f;
            this.parent = parent;
        }
    }
}
