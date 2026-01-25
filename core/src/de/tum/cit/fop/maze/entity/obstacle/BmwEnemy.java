package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.ai.RoadPathfinder;

import java.util.ArrayList;
import java.util.List;

public class BmwEnemy extends Obstacle {
    private static final float TARGET_EPS = 0.05f;
    private static final float CENTER_EPS = 0.02f;
    private final TiledMapTileLayer roadLayer;
    private final RoadPathfinder pathfinder;
    private final int mapWidth;
    private final int mapHeight;
    private final float speed = 6f;
    private final List<GridPoint2> roadTiles = new ArrayList<>();
    private ArrayList<GridPoint2> path = new ArrayList<>();
    private int pathIndex = 0;
    private int goalX = Integer.MIN_VALUE;
    private int goalY = Integer.MIN_VALUE;
    private boolean pendingRemove = false;

    public BmwEnemy(TiledMapTileLayer roadLayer, float x, float y) {
        super(x, y, 2, 1, 0, 0, 1);
        this.roadLayer = roadLayer;
        this.pathfinder = new RoadPathfinder(roadLayer);
        this.mapWidth = roadLayer.getWidth();
        this.mapHeight = roadLayer.getHeight();
        cacheRoadTiles();
    }

    @Override
    protected void onAddedToStage() {
        super.onAddedToStage();
        pickNewGoal();
        recalcPath();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (roadTiles.isEmpty()) {
            return;
        }
        if (checkBmwCollisions()) {
            return;
        }
        if ((pathIndex >= path.size() || path.isEmpty()) && isCenteredOnTile()) {
            pickNewGoal();
            recalcPath();
        }
        followPath(delta);
    }

    @Override
    protected void collision() {
        if (!player.isStunned()) {
            player.damage(1);
        }
    }

    private boolean checkBmwCollisions() {
        if (pendingRemove || getStage() == null) {
            return pendingRemove;
        }
        for (int i = 0; i < getStage().getActors().size; i++) {
            if (!(getStage().getActors().get(i) instanceof BmwEnemy other)) {
                continue;
            }
            if (other == this || other.pendingRemove) {
                continue;
            }
            if (getBounds().overlaps(other.getBounds())) {
                handleBmwCollision(other);
                return true;
            }
        }
        return false;
    }

    private void handleBmwCollision(BmwEnemy other) {
        pendingRemove = true;
        other.pendingRemove = true;
        if (getStage() == null) {
            remove();
            other.remove();
            return;
        }
        float centerX = (getX() + getWidth() / 2f + other.getX() + other.getWidth() / 2f) / 2f;
        float centerY = (getY() + getHeight() / 2f + other.getY() + other.getHeight() / 2f) / 2f;
        getStage().addActor(new Explosion(centerX, centerY, 5f, player));
        for (int i = 0; i < 2; i++) {
            spawnRandomBmw();
        }
        remove();
        other.remove();
    }

    private void spawnRandomBmw() {
        if (roadTiles.isEmpty() || getStage() == null) {
            return;
        }
        GridPoint2 target = roadTiles.get(MathUtils.random(roadTiles.size() - 1));
        float spawnX = target.x + 0.5f - 1f;
        float spawnY = target.y + 0.5f - 0.5f;
        BmwEnemy bmw = new BmwEnemy(roadLayer, spawnX, spawnY);
        getStage().addActor(bmw);
    }

    private com.badlogic.gdx.math.Rectangle getBounds() {
        return new com.badlogic.gdx.math.Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    private void cacheRoadTiles() {
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (roadLayer.getCell(x, y) != null) {
                    roadTiles.add(new GridPoint2(x, y));
                }
            }
        }
    }

    private void pickNewGoal() {
        if (roadTiles.isEmpty()) {
            return;
        }
        GridPoint2 target = roadTiles.get(MathUtils.random(roadTiles.size() - 1));
        goalX = target.x;
        goalY = target.y;
    }

    private void recalcPath() {
        int startX = clampTileX(getX() + getWidth() / 2f);
        int startY = clampTileY(getY() + getHeight() / 2f);
        path = pathfinder.findPath(startX, startY, goalX, goalY);
        pathIndex = 0;
    }

    private void followPath(float delta) {
        if (pathIndex >= path.size()) {
            moveToTileCenter(delta);
            return;
        }
        GridPoint2 target = path.get(pathIndex);
        float targetX = target.x + 0.5f;
        float targetY = target.y + 0.5f;
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float dx = targetX - centerX;
        float dy = targetY - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < TARGET_EPS) {
            pathIndex++;
            return;
        }

        updateSizeForDirection(dx, dy);
        float step = Math.min(speed * delta, dist);
        setPosition(getX() + (dx / dist) * step, getY() + (dy / dist) * step);
    }

    private void moveToTileCenter(float delta) {
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float targetX = MathUtils.floor(centerX) + 0.5f;
        float targetY = MathUtils.floor(centerY) + 0.5f;
        float dx = targetX - centerX;
        float dy = targetY - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < TARGET_EPS) {
            setPosition(getX() + dx, getY() + dy);
            return;
        }
        updateSizeForDirection(dx, dy);
        float step = Math.min(speed * delta, dist);
        setPosition(getX() + (dx / dist) * step, getY() + (dy / dist) * step);
    }

    private boolean isCenteredOnTile() {
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        float targetX = MathUtils.floor(centerX) + 0.5f;
        float targetY = MathUtils.floor(centerY) + 0.5f;
        float dx = targetX - centerX;
        float dy = targetY - centerY;

        if (Math.abs(dx) < CENTER_EPS && Math.abs(dy) < CENTER_EPS) {
            setPosition(getX() + dx, getY() + dy);
            return true;
        }
        return false;
    }

    private int clampTileX(float centerX) {
        return pathfinder.clampCoord(MathUtils.floor(centerX), mapWidth);
    }

    private int clampTileY(float centerY) {
        return pathfinder.clampCoord(MathUtils.floor(centerY), mapHeight);
    }

    private void updateSizeForDirection(float dx, float dy) {
        if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > 0f) {
            setSizeKeepingCenter(1f, 2f);
        } else {
            setSizeKeepingCenter(2f, 1f);
        }
    }

    private void setSizeKeepingCenter(float width, float height) {
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        setSize(width, height);
        setPosition(centerX - width / 2f, centerY - height / 2f);
    }
}
