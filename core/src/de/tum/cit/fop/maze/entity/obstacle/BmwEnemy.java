package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.system.AchievementManager;
import de.tum.cit.fop.maze.ai.RoadPathfinder;
import de.tum.cit.fop.maze.entity.DeathCause;
import de.tum.cit.fop.maze.entity.Player;
import de.tum.cit.fop.maze.entity.obstacle.Enemy;
import de.tum.cit.fop.maze.entity.obstacle.JandarmeriaDeath;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.utils.Array;

/**
 * BMW enemy that drives along road tiles and collides with actors.
 */
public class BmwEnemy extends Obstacle {
    // TODO merge common stuff
    // at the moment BMWEnemy is more or less enemy with some extra stuff, ill merge the code later
    /** Target distance threshold for path steps. */
    private static final float TARGET_EPS = 0.05f;
    /** Centering threshold for tile alignment. */
    private static final float CENTER_EPS = 0.02f;
    /** Width of the BMW when horizontal. */
    private static final int BMW_WIDTH_HORIZONTAL = 2;
    /** Height of the BMW when horizontal. */
    private static final int BMW_HEIGHT_HORIZONTAL = 1;
    /** Width of the BMW when vertical. */
    private static final int BMW_WIDTH_VERTICAL = 1;
    /** Height of the BMW when vertical. */
    private static final int BMW_HEIGHT_VERTICAL = 2;
    /** Road layer used for pathfinding. */
    private static TiledMapTileLayer roadLayer;
    /** Pathfinder for road movement. */
    private static RoadPathfinder pathfinder;
    /** Map width in tiles. */
    private static int mapWidth;
    /** Map height in tiles. */
    private static int mapHeight;
    /** Base movement speed. */
    private final float speed = 6f;
    /** Cached road tiles. */
    protected static final List<GridPoint2> roadTiles = new ArrayList<>();
    /** Current path of tile points. */
    private ArrayList<GridPoint2> path = new ArrayList<>();
    /** Current index in the path. */
    private int pathIndex = 0;
    /** Current goal tile x. */
    private int goalX = Integer.MIN_VALUE;
    /** Current goal tile y. */
    private int goalY = Integer.MIN_VALUE;
    /** Whether removal is pending after collision. */
    private boolean pendingRemove = false;
    /** Drive animation for north movement. */
    private static Animation<TextureRegion> driveNorthAnimation;
    /** Drive animation for south movement. */
    private static Animation<TextureRegion> driveSouthAnimation;
    /** Drive animation for east movement. */
    private static Animation<TextureRegion> driveEastAnimation;
    /** Drive animation for west movement. */
    private static Animation<TextureRegion> driveWestAnimation;
    /** Whether animations were initialized. */
    private static boolean animationsInitialized = false;
    /** Current facing direction. */
    private Direction facingDirection = Direction.N;

    /**
     * Cardinal directions for BMW orientation.
     */
    private enum Direction {
        N, S, E, W
    }

    /**
     * Creates a BMW enemy at a given position.
     *
     * @param roadLayer road layer for pathfinding
     * @param x spawn x position
     * @param y spawn y position
     */
    public BmwEnemy(TiledMapTileLayer roadLayer, float x, float y) {
        super(x, y, BMW_WIDTH_HORIZONTAL, BMW_HEIGHT_HORIZONTAL, 0, 0, 1);
        setRoadLayer(roadLayer);
        initDriveAnimation();
    }

    /**
     * Initializes behavior once added to stage.
     */
    @Override
    protected void onAddedToStage() {
        super.onAddedToStage();
        pickNewGoal();
        recalcPath();
    }

    /**
     * Updates BMW movement and handles collisions.
     *
     * @param delta frame delta time
     */
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

    /**
     * Handles collision with the player.
     */
    @Override
    protected void collision() {
        if (!player.isStunned()) {
            if (!player.isGodMode()) {
                AchievementManager.incrementProgress("german_engineering", 1);
            }
            player.damage(999, DeathCause.BMW);
        }
    }

    /**
     * Checks for collisions with other BMWs or guards.
     *
     * @return {@code true} if a collision was handled
     */
    private boolean checkBmwCollisions() {
        Stage stage = getStage();
        if (pendingRemove || stage == null) {
            return pendingRemove;
        }
        for (int i = 0; i < stage.getActors().size; i++) {
            Actor actor = stage.getActors().get(i);
            if (actor instanceof BmwEnemy other) {
                if (other == this || other.pendingRemove) {
                    continue;
                }
                if (getBounds().overlaps(other.getBounds())) {
                    handleBmwCollision(other);
                    return true;
                }
            } else if (actor instanceof Enemy guard) {
                Rectangle guardBounds = new Rectangle(guard.getX(), guard.getY(), guard.getWidth(), guard.getHeight());
                if (getBounds().overlaps(guardBounds)) {
                    handleGuardCollision(guard);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handles collisions between two BMWs.
     *
     * @param other other BMW
     */
    private void handleBmwCollision(BmwEnemy other) {
        pendingRemove = true;
        other.pendingRemove = true;
        Stage stage = getStage();
        if (stage == null) {
            remove();
            other.remove();
            return;
        }
        float centerX = (getX() + getWidth() / 2f + other.getX() + other.getWidth() / 2f) / 2f;
        float centerY = (getY() + getHeight() / 2f + other.getY() + other.getHeight() / 2f) / 2f;
        stage.addActor(new Explosion(centerX, centerY, 5f, player));
        Rectangle cameraView = null;
        if (stage.getCamera() instanceof com.badlogic.gdx.graphics.OrthographicCamera camera) {
            float viewW = camera.viewportWidth * camera.zoom;
            float viewH = camera.viewportHeight * camera.zoom;
            float viewX = camera.position.x - viewW / 2f;
            float viewY = camera.position.y - viewH / 2f;
            cameraView = new Rectangle(viewX, viewY, viewW, viewH);
        }
        spawnRandomBmws(player, stage, 2, cameraView);
        remove();
        other.remove();
    }

    /**
     * Handles collision with a guard enemy.
     *
     * @param guard guard enemy
     */
    private void handleGuardCollision(Enemy guard) {
        Stage stage = getStage();
        if (stage == null) {
            guard.remove();
            return;
        }
        float centerX = guard.getX() + guard.getWidth() / 2f;
        float centerY = guard.getY() + guard.getHeight() / 2f;
        stage.addActor(new JandarmeriaDeath(centerX, centerY));
        guard.remove();
    }

    /**
     * Initializes drive animations once.
     */
    private void initDriveAnimation() {
        if (animationsInitialized) {
            return;
        }
        animationsInitialized = true;
        Texture northSheet = new Texture(com.badlogic.gdx.Gdx.files.internal("Blue_SPORT_CLEAN_NORTH_000-sheet.png"));
        Texture southSheet = new Texture(com.badlogic.gdx.Gdx.files.internal("Blue_SPORT_CLEAN_SOUTH_000-sheet.png"));
        Texture eastSheet = new Texture(com.badlogic.gdx.Gdx.files.internal("Blue_SPORT_CLEAN_EAST_000-sheet.png"));
        Texture westSheet = new Texture(com.badlogic.gdx.Gdx.files.internal("Blue_SPORT_CLEAN_WEST_000-sheet.png"));
        int frameW = 100;
        int frameH = 100;
        Array<TextureRegion> northFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> southFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> eastFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> westFrames = new Array<>(TextureRegion.class);
        for (int col = 0; col < 4; col++) {
            northFrames.add(new TextureRegion(northSheet, col * frameW, 0, frameW, frameH));
            southFrames.add(new TextureRegion(southSheet, col * frameW, 0, frameW, frameH));
            eastFrames.add(new TextureRegion(eastSheet, col * frameW, 0, frameW, frameH));
            westFrames.add(new TextureRegion(westSheet, col * frameW, 0, frameW, frameH));
        }
        driveNorthAnimation = new Animation<>(0.3f / 4f, northFrames);
        driveSouthAnimation = new Animation<>(0.3f / 4f, southFrames);
        driveEastAnimation = new Animation<>(0.3f / 4f, eastFrames);
        driveWestAnimation = new Animation<>(0.3f / 4f, westFrames);
    }

    /**
     * Draws the BMW using the current facing animation.
     *
     * @param batch sprite batch
     * @param parentAlpha parent alpha
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion currentFrame = switch (facingDirection) {
            case S -> driveSouthAnimation.getKeyFrame(animationTime, true);
            case E -> driveEastAnimation.getKeyFrame(animationTime, true);
            case W -> driveWestAnimation.getKeyFrame(animationTime, true);
            default -> driveNorthAnimation.getKeyFrame(animationTime, true);
        };
        batch.draw(currentFrame, getX(), getY(), getWidth(), getHeight());
    }

    /**
     * Returns current bounding rectangle.
     *
     * @return bounds rectangle
     */
    private Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    /**
     * Spawns random BMWs on road tiles.
     *
     * @param player player used for distance checks
     * @param stage stage to add BMWs to
     * @param amount number to spawn
     */
    public static void spawnRandomBmws(Player player, Stage stage, int amount) {
        spawnRandomBmws(player, stage, amount, null);
    }

    /**
     * Spawns random BMWs on road tiles, avoiding camera view.
     *
     * @param player player used for distance checks
     * @param stage stage to add BMWs to
     * @param amount number to spawn
     * @param cameraView camera bounds to avoid
     */
    public static void spawnRandomBmws(Player player, Stage stage, int amount, Rectangle cameraView) {
        if (roadLayer == null || player == null || stage == null) {
            return;
        }
        List<GridPoint2> roadTiles = collectRoadTiles(roadLayer);
        if (roadTiles.isEmpty()) {
            return;
        }
        List<GridPoint2> candidates = getSpawnCandidates(roadTiles, player, 2);
        int spawned = 0;
        while (spawned < amount && !candidates.isEmpty()) {
            int index = MathUtils.random(candidates.size() - 1);
            GridPoint2 target = candidates.remove(index);
            float centerX = target.x + 0.5f;
            float centerY = target.y + 0.5f;
            if (wouldCollideAt(stage, centerX, centerY)) {
                continue;
            }
            if (cameraView != null && wouldOverlapCamera(centerX, centerY, cameraView)) {
                continue;
            }
            float spawnX = centerX - (BMW_WIDTH_HORIZONTAL / 2f);
            float spawnY = centerY - (BMW_HEIGHT_HORIZONTAL / 2f);
            stage.addActor(new BmwEnemy(roadLayer, spawnX, spawnY));
            spawned++;
        }
    }

    /**
     * Filters spawn candidates by distance from the player.
     *
     * @param roadTiles road tile list
     * @param player player reference
     * @param distance minimum distance in tiles
     * @return candidate tiles
     */
    private static List<GridPoint2> getSpawnCandidates(List<GridPoint2> roadTiles, Player player, int distance) {
        int playerTileX = clampTileCoord(player.getX() + player.getWidth() / 2f, roadLayer.getWidth());
        int playerTileY = clampTileCoord(player.getY() + player.getHeight() / 2f, roadLayer.getHeight());
        List<GridPoint2> candidates = new ArrayList<>(roadTiles.size());
        for (GridPoint2 tile : roadTiles) {
            if (Math.abs(tile.x - playerTileX) <= distance && Math.abs(tile.y - playerTileY) <= distance) {
                continue;
            }
            candidates.add(tile);
        }
        return candidates;
    }

    /**
     * Collects all road tiles from the road layer.
     *
     * @param roadLayer road layer to scan
     * @return list of road tiles
     */
    private static List<GridPoint2> collectRoadTiles(TiledMapTileLayer roadLayer) {
        List<GridPoint2> tiles = new ArrayList<>();
        int width = roadLayer.getWidth();
        int height = roadLayer.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (roadLayer.getCell(x, y) != null) {
                    tiles.add(new GridPoint2(x, y));
                }
            }
        }
        return tiles;
    }

    /**
     * Clamps a tile coordinate to map bounds.
     *
     * @param center center coordinate
     * @param max max bound
     * @return clamped tile coordinate
     */
    private static int clampTileCoord(float center, int max) {
        int tile = MathUtils.floor(center);
        if (tile < 0) {
            return 0;
        }
        if (tile >= max) {
            return max - 1;
        }
        return tile;
    }

    /**
     * Checks whether spawning would collide with existing actors.
     *
     * @param stage stage containing actors
     * @param centerX center x
     * @param centerY center y
     * @return {@code true} if collision would occur
     */
    private static boolean wouldCollideAt(Stage stage, float centerX, float centerY) {
        if (stage == null) {
            return true;
        }
        // check if you spawn the BMW when he is moving left/right
        Rectangle spawnBoundsHorizontal = new Rectangle(
                centerX - (BMW_WIDTH_HORIZONTAL / 2f),
                centerY - (BMW_HEIGHT_HORIZONTAL / 2f),
                BMW_WIDTH_HORIZONTAL,
                BMW_HEIGHT_HORIZONTAL
        );

        // check if you spawn the BMW when he is moving up/down
        Rectangle spawnBoundsVertical = new Rectangle(
                centerX - (BMW_WIDTH_VERTICAL / 2f),
                centerY - (BMW_HEIGHT_VERTICAL / 2f),
                BMW_WIDTH_VERTICAL,
                BMW_HEIGHT_VERTICAL
        );


        // if at any point a bmw cant spawn in either orientation we dont spawn him there
        // in theory a bmw could spawn in a borked positions, for example with one tile over the edge
        // however it would take him at most 1 frame to swich his orientation or move to a valid state
        // so this will not be fixed
        for (Actor actor : stage.getActors()) {
            Rectangle actorBounds = new Rectangle(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight());
            if (spawnBoundsHorizontal.overlaps(actorBounds) || spawnBoundsVertical.overlaps(actorBounds)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a spawn would overlap the camera view.
     *
     * @param centerX center x
     * @param centerY center y
     * @param cameraView camera bounds
     * @return {@code true} if overlapping
     */
    private static boolean wouldOverlapCamera(float centerX, float centerY, Rectangle cameraView) {
        Rectangle spawnBoundsHorizontal = new Rectangle(
                centerX - (BMW_WIDTH_HORIZONTAL / 2f),
                centerY - (BMW_HEIGHT_HORIZONTAL / 2f),
                BMW_WIDTH_HORIZONTAL,
                BMW_HEIGHT_HORIZONTAL
        );
        Rectangle spawnBoundsVertical = new Rectangle(
                centerX - (BMW_WIDTH_VERTICAL / 2f),
                centerY - (BMW_HEIGHT_VERTICAL / 2f),
                BMW_WIDTH_VERTICAL,
                BMW_HEIGHT_VERTICAL
        );
        return spawnBoundsHorizontal.overlaps(cameraView) || spawnBoundsVertical.overlaps(cameraView);
    }

    /**
     * Caches road tiles from the current road layer.
     */
    private static void cacheRoadTiles() {
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (roadLayer.getCell(x, y) != null) {
                    roadTiles.add(new GridPoint2(x, y));
                }
            }
        }
    }

    /**
     * Recomputes cached road tiles.
     */
    public static void recomputeRoadTiles() {
        if (roadLayer == null) {
            return;
        }
        roadTiles.clear();
        cacheRoadTiles();
    }

    /**
     * Sets the road layer used for BMW navigation.
     *
     * @param newRoadLayer new road layer
     */
    public static void setRoadLayer(TiledMapTileLayer newRoadLayer) {
        if (newRoadLayer == null) {
            return;
        }
        if (BmwEnemy.roadLayer != newRoadLayer) {
            BmwEnemy.roadLayer = newRoadLayer;
            pathfinder = new RoadPathfinder(newRoadLayer);
            mapWidth = newRoadLayer.getWidth();
            mapHeight = newRoadLayer.getHeight();
            recomputeRoadTiles();
        } else if (roadTiles.isEmpty()) {
            recomputeRoadTiles();
        }
    }

    /**
     * Picks a new random goal tile from road tiles.
     */
    private void pickNewGoal() {
        if (roadTiles.isEmpty()) {
            return;
        }
        GridPoint2 target = roadTiles.get(MathUtils.random(roadTiles.size() - 1));
        goalX = target.x;
        goalY = target.y;
    }

    /**
     * Recalculates the path to the current goal.
     */
    private void recalcPath() {
        int startX = clampTileX(getX() + getWidth() / 2f);
        int startY = clampTileY(getY() + getHeight() / 2f);
        path = pathfinder.findPath(startX, startY, goalX, goalY);
        pathIndex = 0;
    }

    /**
     * Moves along the current path.
     *
     * @param delta frame delta time
     */
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
        updateFacingDirection(dx, dy);
        float step = Math.min(speed * delta, dist);
        setPosition(getX() + (dx / dist) * step, getY() + (dy / dist) * step);
    }

    /**
     * Moves toward the center of the current tile.
     *
     * @param delta frame delta time
     */
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
        updateFacingDirection(dx, dy);
        float step = Math.min(speed * delta, dist);
        setPosition(getX() + (dx / dist) * step, getY() + (dy / dist) * step);
    }

    /**
     * Checks whether the BMW is centered on a tile.
     *
     * @return {@code true} if centered
     */
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

    /**
     * Clamps x coordinate to map bounds.
     *
     * @param centerX center coordinate
     * @return clamped tile x
     */
    private static int clampTileX(float centerX) {
        return pathfinder.clampCoord(MathUtils.floor(centerX), mapWidth);
    }

    /**
     * Clamps y coordinate to map bounds.
     *
     * @param centerY center coordinate
     * @return clamped tile y
     */
    private static int clampTileY(float centerY) {
        return pathfinder.clampCoord(MathUtils.floor(centerY), mapHeight);
    }

    /**
     * Updates size based on movement direction.
     *
     * @param dx delta x
     * @param dy delta y
     */
    private void updateSizeForDirection(float dx, float dy) {
        if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > 0f) {
            setSizeKeepingCenter(1f, 2f);
        } else {
            setSizeKeepingCenter(2f, 1f);
        }
    }

    /**
     * Updates facing direction based on movement vector.
     *
     * @param dx delta x
     * @param dy delta y
     */
    private void updateFacingDirection(float dx, float dy) {
        if (Math.abs(dx) >= Math.abs(dy)) {
            facingDirection = dx >= 0f ? Direction.E : Direction.W;
        } else {
            facingDirection = dy >= 0f ? Direction.N : Direction.S;
        }
    }

    /**
     * Sets size while keeping the current center position.
     *
     * @param width new width
     * @param height new height
     */
    private void setSizeKeepingCenter(float width, float height) {
        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;
        setSize(width, height);
        setPosition(centerX - width / 2f, centerY - height / 2f);
    }
}
