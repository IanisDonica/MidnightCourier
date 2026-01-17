package de.tum.cit.fop.maze.ai;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Pathfinder {
    private final TiledMapTileLayer collisionLayer;

    public Pathfinder(TiledMapTileLayer collisionLayer) {
        this.collisionLayer = collisionLayer;
    }

    public ArrayList<GridPoint2> findPath(int startX, int startY, int goalX, int goalY) {
        int width = collisionLayer.getWidth();
        int height = collisionLayer.getHeight();

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
            for (int y = 0; y < height; y++) {
                gScore[x][y] = Float.POSITIVE_INFINITY;
            }
        }

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Node startNode = new Node(start.x, start.y, 0f, manhattan(start.x, start.y, goal.x, goal.y), null);
        gScore[start.x][start.y] = 0f;
        open.add(startNode);

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

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
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];
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
    // the coordinates can be -1 or width/height, this them to the min max values allowed
    public int clampCoord(int value, int maxExclusive) {
        if (value < 0) {
            return 0;
        }
        if (value >= maxExclusive) {
            return maxExclusive - 1;
        }
        return value;
    }

    // THis gets called once the algo sees the taget node, from there is creates a path by following the nodes backwords to the player
    private ArrayList<GridPoint2> reconstructPath(Node node, int startX, int startY) {
        ArrayList<GridPoint2> result = new ArrayList<>();
        Node current = node;
        while (current != null) {
            if (!(current.x == startX && current.y == startY)) {
                result.add(0, new GridPoint2(current.x, current.y));
            }
            current = current.parent;
        }
        return result;
    }

    // The A-star heuristic, manhattan since we are in a grid
    private float manhattan(int x, int y, int goalX, int goalY) {
        return Math.abs(goalX - x) + Math.abs(goalY - y);
    }

    private boolean isWalkable(int x, int y) {
        if (x < 0 || y < 0 || x >= collisionLayer.getWidth() || y >= collisionLayer.getHeight()) {
            return false;
        }
        return collisionLayer.getCell(x, y) == null;
    }

    //BFS cloest walkable
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

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (!queue.isEmpty()) {
            GridPoint2 current = queue.poll();
            for (int i = 0; i < 4; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];
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

    // Abstraction class, this is prob overkill, but it's way easier to work with this class
    private static class Node {
        private final int x;
        private final int y;
        private final float g;
        private final float f;
        private final Node parent;

        private Node(int x, int y, float g, float f, Node parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.f = f;
            this.parent = parent;
        }
    }
}
