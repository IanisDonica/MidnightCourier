package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import de.tum.cit.fop.maze.ai.ChaseBehavior;
import de.tum.cit.fop.maze.ai.Pathfinder;
import de.tum.cit.fop.maze.ai.PatrolBehaviour;
import de.tum.cit.fop.maze.ai.RetreatBehavior;
import de.tum.cit.fop.maze.entity.collectible.Collectible;
import de.tum.cit.fop.maze.entity.Player;
import de.tum.cit.fop.maze.system.AchievementManager;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.utils.Array;

/**
 * Enemy obstacle that uses pathfinding and AI behaviors.
 */
public class Enemy extends Obstacle {
    /**
     * Internal state for enemy behavior.
     */
    private enum EnemyState {
        CHASING,
        PATROLLING,
        PATROL_WAIT,
        RETREATING,
        RETREAT_WAIT
    }

    /** Path recalculation interval in seconds. */
    private static final float PATH_RECALC_INTERVAL = 0.5f;
    /** Target distance threshold for path steps. */
    private static final float TARGET_EPS = 0.05f;
    /** Centering threshold for tile alignment. */
    private static final float CENTER_EPS = 0.02f;
    /** Speed scale when patrolling or retreating. */
    private static final float PATROL_SPEED_SCALE = 0.5f;
    /** Vision range in tiles. */
    private static final int VISION_RANGE_TILES = 20;
    /** Maximum retreat duration in seconds. */
    private static final float MAX_RETREAT_DURATION_SECONDS = 4f;
    /** Duration for running state in seconds. */
    private static final float RUN_DURATION_SECONDS = 10f;
    /** Speed multiplier while running. */
    private static final float RUN_SPEED_MULTIPLIER = 2f;
    /** Collision layer for movement checks. */
    private final TiledMapTileLayer collisionLayer;
    /** Map width in tiles. */
    private final int mapWidth;
    /** Map height in tiles. */
    private final int mapHeight;
    /** Base movement speed. */
    private final float speed;
    /** Pathfinder for tile navigation. */
    private final Pathfinder pathfinder;
    /** Behavior controller for chasing. */
    private final ChaseBehavior chaseBehavior;
    /** Behavior controller for retreating. */
    private final RetreatBehavior retreatBehavior;
    /** Behavior controller for patrolling. */
    private final PatrolBehaviour patrolBehavior;
    /** Global token to force all enemies to retreat. */
    private static int globalRetreatToken = 0;
    /** Current path of tile points. */
    private ArrayList<GridPoint2> path = new ArrayList<>();
    /** Current index in the path. */
    private int pathIndex = 0;
    /** Timer for path recalculation. */
    private float pathRecalcTimer = 0f;
    /** Timer for retreat duration. */
    private float retreatTimer = 0f;
    /** Timer for running duration. */
    private float runTimer = 0f;
    /** Whether running is active. */
    private boolean running = false;
    /** Last path goal x coordinate. */
    private int lastGoalX = Integer.MIN_VALUE; // TODO Remove this
    /** Last path goal y coordinate. */
    private int lastGoalY = Integer.MIN_VALUE;
    /** Last applied global retreat token. */
    private int lastRetreatToken = 0;
    /** Current behavior state. */
    private EnemyState state = EnemyState.CHASING; // Initial state
    /** Facing direction for animation. */
    private char facingDirection = 'd';
    /** Walk animation for north movement. */
    private static Animation<TextureRegion> walkNorthAnimation;
    /** Walk animation for south movement. */
    private static Animation<TextureRegion> walkSouthAnimation;
    /** Walk animation for east movement. */
    private static Animation<TextureRegion> walkEastAnimation;
    /** Walk animation for west movement. */
    private static Animation<TextureRegion> walkWestAnimation;
    /** Whether animations were initialized. */
    private static boolean animationsInitialized = false;

    /**
     * Creates an enemy at a given position.
     *
     * @param collisionLayer collision layer for movement checks
     * @param x spawn x position
     * @param y spawn y position
     */
    public Enemy(TiledMapTileLayer collisionLayer, float x, float y) {
        super(x, y, 1,1, 0,0,3);
        this.collisionLayer = collisionLayer;
        this.pathfinder = new Pathfinder(collisionLayer);
        this.mapWidth = collisionLayer.getWidth();
        this.mapHeight = collisionLayer.getHeight();
        this.chaseBehavior = new ChaseBehavior(mapWidth, mapHeight, collisionLayer);
        this.retreatBehavior = new RetreatBehavior(mapWidth, mapHeight, collisionLayer);
        this.patrolBehavior = new PatrolBehaviour(mapWidth, mapHeight, collisionLayer);
        this.speed = 2.2f;
        initWalkAnimations();
    }

    /**
     * Returns the collision layer used by this enemy.
     *
     * @return collision layer
     */
    public TiledMapTileLayer getCollisionLayer() {
        return collisionLayer;
    }

    /**
     * Initializes behavior once added to stage.
     */
    @Override
    protected void onAddedToStage() {
        super.onAddedToStage();
        Stage stage = getStage();
        chaseBehavior.setStage(stage);
        retreatBehavior.setStage(stage);
        patrolBehavior.setStage(stage);
        int[] coords = computePathCoords();
        path = pathfinder.findPath(coords[0], coords[1], coords[2], coords[3]);
        pathIndex = 0;
        ensureAboveCollectibles();
    }

    /**
     * Updates the enemy AI and movement.
     *
     * @param delta frame delta time
     */
    @Override
    public void act(float delta) {
        //TODO remove duplicate code
        super.act(delta);

        // If a global retreat has been called, only retreat if you are actually chasing the enemy.
        if (lastRetreatToken != globalRetreatToken) {
            lastRetreatToken = globalRetreatToken;
            if (state != EnemyState.RETREATING && state != EnemyState.RETREAT_WAIT) {
                enterRetreating();
            }
        }

        if (state == EnemyState.RETREATING || state == EnemyState.RETREAT_WAIT) {
            retreatTimer += delta;
            if (retreatTimer >= MAX_RETREAT_DURATION_SECONDS) {
                chaseBehavior.reset();
                enterPatrolling();
                return;
            }
        }

        switch (state) {
            case RETREAT_WAIT:
                if (retreatBehavior.updateWait(delta)) {
                    chaseBehavior.reset();
                    enterPatrolling();
                }
                return;
            case PATROL_WAIT:
                if (patrolBehavior.updateWait(delta)) {
                    enterPatrolling();
                }
                return;
            case PATROLLING:
                if (canSeePlayer()) {
                    enterChasing();
                    break;
                }
                if (!path.isEmpty() && pathIndex >= path.size() && isCenteredOnTile()) {
                    enterPatrolWait();
                    return;
                }
                break;
            case CHASING:
                if (chaseBehavior.shouldRetreat(canSeePlayer(), delta)) {
                    enterRetreating();
                    return;
                }
                if (!running && canSeePlayer()) {
                    running = true;
                    runTimer = 0f;
                }
                break;
            case RETREATING:
                if (!path.isEmpty() && pathIndex >= path.size() && isCenteredOnTile()) {
                    enterRetreatWait();
                    return;
                }
                break;
            default:
                break;
        }

        pathRecalcTimer -= delta;
        boolean pathExhausted = pathIndex >= path.size();
        if (pathRecalcTimer <= 0f && isCenteredOnTile()) {
            boolean needsRepath = path.isEmpty() || pathExhausted;
            int[] coords = computePathCoords();
            int goalX = coords[2];
            int goalY = coords[3];
            if (!needsRepath) {
                boolean goalChanged = goalX != lastGoalX || goalY != lastGoalY;
                needsRepath = goalChanged;
            }
            if (needsRepath) {
                path = pathfinder.findPath(coords[0], coords[1], goalX, goalY);
                pathIndex = 0;
                lastGoalX = goalX;
                lastGoalY = goalY;
                pathRecalcTimer = PATH_RECALC_INTERVAL;
            }
        }

        followPath(delta);
    }

    /**
     * Moves the enemy along the current path.
     *
     * @param delta frame delta time
     */
    private void followPath(float delta) {
        if (running) {
            runTimer += delta;
            if (runTimer >= RUN_DURATION_SECONDS) {
                running = false;
                runTimer = 0f;
            }
        }
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

        updateFacingDirection(dx, dy);
        float speedScale = (state == EnemyState.PATROLLING || state == EnemyState.RETREATING) ? PATROL_SPEED_SCALE : 1f;
        if (running && state == EnemyState.CHASING) {
            speedScale *= RUN_SPEED_MULTIPLIER;
        }
        float step = Math.min(speed * speedScale * delta, dist);
        setPosition(getX() + (dx / dist) * step, getY() + (dy / dist) * step);
    }

    /**
     * Moves the enemy toward the center of the current tile.
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
        updateFacingDirection(dx, dy);
        float step = Math.min(speed * delta, dist);
        setPosition(getX() + (dx / dist) * step, getY() + (dy / dist) * step);
    }

    /**
     * Updates facing direction based on movement vector.
     *
     * @param dx delta x
     * @param dy delta y
     */
    private void updateFacingDirection(float dx, float dy) {
        if (Math.abs(dx) >= Math.abs(dy)) {
            facingDirection = dx >= 0f ? 'r' : 'l';
        } else {
            facingDirection = dy >= 0f ? 'u' : 'd';
        }
    }

    /**
     * Initializes walking animations once.
     */
    private void initWalkAnimations() {
        if (animationsInitialized) {
            return;
        }
        animationsInitialized = true;
        Texture northSheet = new Texture(com.badlogic.gdx.Gdx.files.internal("Police/NORTH_MOVEMENT.png"));
        Texture southSheet = new Texture(com.badlogic.gdx.Gdx.files.internal("Police/SOUTH_MOVEMENT.png"));
        Texture eastSheet = new Texture(com.badlogic.gdx.Gdx.files.internal("Police/EAST_MOVEMENT.png"));
        Texture westSheet = new Texture(com.badlogic.gdx.Gdx.files.internal("Police/WEST_MOVEMENT.png"));

        int frameW = 24;
        int frameH = 24;
        Array<TextureRegion> northFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> southFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> eastFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> westFrames = new Array<>(TextureRegion.class);

        for (int col = 0; col < 6; col++) {
            northFrames.add(new TextureRegion(northSheet, col * frameW, 0, frameW, frameH));
            southFrames.add(new TextureRegion(southSheet, col * frameW, 0, frameW, frameH));
        }
        for (int col = 0; col < 4; col++) {
            eastFrames.add(new TextureRegion(eastSheet, col * frameW, 0, frameW, frameH));
            westFrames.add(new TextureRegion(westSheet, col * frameW, 0, frameW, frameH));
        }

        walkNorthAnimation = new Animation<>(0.15f, northFrames);
        walkSouthAnimation = new Animation<>(0.15f, southFrames);
        walkEastAnimation = new Animation<>(0.15f, eastFrames);
        walkWestAnimation = new Animation<>(0.15f, westFrames);
    }

    /**
     * Draws the enemy using the current facing animation.
     *
     * @param batch sprite batch
     * @param parentAlpha parent alpha
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion currentFrame = switch (facingDirection) {
            case 'l' -> walkWestAnimation.getKeyFrame(animationTime, true);
            case 'r' -> walkEastAnimation.getKeyFrame(animationTime, true);
            case 'u' -> walkNorthAnimation.getKeyFrame(animationTime, true);
            default -> walkSouthAnimation.getKeyFrame(animationTime, true);
        };
        batch.draw(currentFrame, getX(), getY(), getWidth(), getHeight());
    }

    /**
     * Spawns random enemies in the world.
     *
     * @param player player used for distance checks
     * @param stage stage to add enemies to
     * @param collisionLayer collision layer to use
     * @param amount number to spawn
     */
    public static void spawnRandomEnemies(Player player, Stage stage, TiledMapTileLayer collisionLayer, int amount) {
        spawnRandomEnemies(player, stage, collisionLayer, amount, null, null);
    }

    /**
     * Spawns random enemies with optional camera constraints and output list.
     *
     * @param player player used for distance checks
     * @param stage stage to add enemies to
     * @param collisionLayer collision layer to use
     * @param amount number to spawn
     * @param cameraView camera view bounds to avoid
     * @param outEnemies list to collect spawned enemies
     */
    public static void spawnRandomEnemies(Player player,
                                          Stage stage,
                                          TiledMapTileLayer collisionLayer,
                                          int amount,
                                          Rectangle cameraView,
                                          List<Enemy> outEnemies) {
        if (player == null || stage == null || collisionLayer == null) {
            return;
        }
        List<GridPoint2> walkableTiles = collectWalkableTiles(collisionLayer);
        if (walkableTiles.isEmpty()) {
            return;
        }
        List<GridPoint2> candidates = getSpawnCandidates(walkableTiles, player, 2, collisionLayer);
        int spawned = 0;
        while (spawned < amount && !candidates.isEmpty()) {
            int index = MathUtils.random(candidates.size() - 1);
            GridPoint2 target = candidates.remove(index);
            float spawnX = target.x;
            float spawnY = target.y;
            if (cameraView != null) {
                Rectangle spawnBounds = new Rectangle(spawnX, spawnY, 1f, 1f);
                if (spawnBounds.overlaps(cameraView)) {
                    continue;
                }
            }
            if (wouldCollideAt(stage, spawnX, spawnY)) {
                continue;
            }
            Enemy enemy = new Enemy(collisionLayer, spawnX, spawnY);
            stage.addActor(enemy);
            if (outEnemies != null) {
                outEnemies.add(enemy);
            }
            spawned++;
        }
    }

    /**
     * Collects all walkable tiles from the collision layer.
     *
     * @param collisionLayer collision layer to scan
     * @return list of walkable tiles
     */
    private static List<GridPoint2> collectWalkableTiles(TiledMapTileLayer collisionLayer) {
        List<GridPoint2> tiles = new ArrayList<>();
        int width = collisionLayer.getWidth();
        int height = collisionLayer.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (collisionLayer.getCell(x, y) == null) {
                    tiles.add(new GridPoint2(x, y));
                }
            }
        }
        return tiles;
    }

    /**
     * Filters spawn candidates by distance from the player.
     *
     * @param tiles candidate tiles
     * @param player player reference
     * @param distance minimum distance in tiles
     * @param collisionLayer collision layer for bounds
     * @return filtered candidate list
     */
    private static List<GridPoint2> getSpawnCandidates(List<GridPoint2> tiles, Player player, int distance, TiledMapTileLayer collisionLayer) {
        int playerTileX = clampTileCoord(player.getX() + player.getWidth() / 2f, collisionLayer.getWidth());
        int playerTileY = clampTileCoord(player.getY() + player.getHeight() / 2f, collisionLayer.getHeight());
        List<GridPoint2> candidates = new ArrayList<>(tiles.size());
        for (GridPoint2 tile : tiles) {
            if (Math.abs(tile.x - playerTileX) <= distance && Math.abs(tile.y - playerTileY) <= distance) {
                continue;
            }
            candidates.add(tile);
        }
        return candidates;
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
     * Checks whether spawning at a location would collide with actors.
     *
     * @param stage stage containing actors
     * @param x spawn x
     * @param y spawn y
     * @return {@code true} if collision would occur
     */
    private static boolean wouldCollideAt(Stage stage, float x, float y) {
        // It probably will never be null, but why not check
        if (stage == null) {
            return true;
        }
        Rectangle spawnBounds = new Rectangle(x, y, 1f, 1f);
        for (Actor actor : stage.getActors()) {
            Rectangle actorBounds = new Rectangle(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight());
            if (spawnBounds.overlaps(actorBounds)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles collision with the player and triggers retreat.
     */
    @Override
    protected void collision() {
        if (!player.isStunned() && state != EnemyState.RETREATING && state != EnemyState.RETREAT_WAIT) {
            boolean willArrest = !player.isGodMode()
                    && !player.isGameOverTriggered()
                    && player.getHp() <= 1;
            player.damage(1);
            if (willArrest) {
                AchievementManager.incrementProgress("first_time_for_everything", 1);
                AchievementManager.incrementProgress("third_strike_and_out", 1);
            }
            globalRetreatToken++; // causes all enemies to go into retreat
            lastRetreatToken = globalRetreatToken;
            enterRetreating();
        }
    }

    /**
     * Computes current path coordinates based on state.
     *
     * @return start and goal tile coordinates
     */
    private int[] computePathCoords() {
        int startX = clampTileX(getX() + getWidth() / 2f);
        int startY = clampTileY(getY() + getHeight() / 2f);
        int goalX;
        int goalY;
        switch (state) {
            case RETREATING:
                GridPoint2 retreatTarget = retreatBehavior.getRetreatTarget();
                goalX = clampTileX(retreatTarget.x);
                goalY = clampTileY(retreatTarget.y);
                break;
            case PATROLLING:
                GridPoint2 patrolTarget = patrolBehavior.getPatrolTarget();
                goalX = clampTileX(patrolTarget.x);
                goalY = clampTileY(patrolTarget.y);
                break;
            default:
                goalX = clampTileX(player.getX() + player.getWidth() / 2f);
                goalY = clampTileY(player.getY() + player.getHeight() / 2f);
                break;
        }
        return new int[]{startX, startY, goalX, goalY};
    }

    /**
     * Checks if the enemy is centered on the current tile.
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
     * Determines if the enemy has line of sight to the player.
     *
     * @return {@code true} if the player is visible
     */
    private boolean canSeePlayer() {
        int startX = clampTileX(getX() + getWidth() / 2f);
        int startY = clampTileY(getY() + getHeight() / 2f);
        int goalX = clampTileX(player.getX() + player.getWidth() / 2f);
        int goalY = clampTileY(player.getY() + player.getHeight() / 2f);

        if (Math.abs(goalX - startX) > VISION_RANGE_TILES || Math.abs(goalY - startY) > VISION_RANGE_TILES) {
            return false;
        }

        int dx = Math.abs(goalX - startX);
        int dy = Math.abs(goalY - startY);
        int sx = startX < goalX ? 1 : -1;
        int sy = startY < goalY ? 1 : -1;
        int err = dx - dy;
        int x = startX;
        int y = startY;

        while (true) {
            if (!isWalkable(x, y) && !(x == startX && y == startY)) {
                return false;
            }
            if (x == goalX && y == goalY) {
                return true;
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    /**
     * Checks whether a tile is walkable.
     *
     * @param x tile x
     * @param y tile y
     * @return {@code true} if walkable
     */
    private boolean isWalkable(int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) {
            return false;
        }
        return collisionLayer.getCell(x, y) == null;
    }

    /**
     * Clamps X coordinate to map bounds.
     *
     * @param centerX center coordinate
     * @return clamped tile x
     */
    private int clampTileX(float centerX) {
        return pathfinder.clampCoord(MathUtils.floor(centerX), mapWidth);
    }

    /**
     * Clamps Y coordinate to map bounds.
     *
     * @param centerY center coordinate
     * @return clamped tile y
     */
    private int clampTileY(float centerY) {
        return pathfinder.clampCoord(MathUtils.floor(centerY), mapHeight);
    }

    /**
     * Transitions to the retreating state.
     */
    private void enterRetreating() {
        state = EnemyState.RETREATING;
        patrolBehavior.clear();
        retreatBehavior.startRetreat();
        retreatTimer = 0f;
        running = false;
        runTimer = 0f;
        resetPathing();
        chaseBehavior.reset();
    }

    /**
     * Transitions to the patrolling state.
     */
    private void enterPatrolling() {
        state = EnemyState.PATROLLING;
        patrolBehavior.startPatrol();
        retreatTimer = 0f;
        running = false;
        runTimer = 0f;
        resetPathing();
    }

    /**
     * Transitions to the patrol waiting state.
     */
    private void enterPatrolWait() {
        state = EnemyState.PATROL_WAIT;
        patrolBehavior.startWaiting();
        retreatTimer = 0f;
        running = false;
        runTimer = 0f;
        pathRecalcTimer = 0f;
    }

    /**
     * Transitions to the retreat waiting state.
     */
    private void enterRetreatWait() {
        state = EnemyState.RETREAT_WAIT;
        retreatBehavior.startWaiting();
        running = false;
        runTimer = 0f;
        pathRecalcTimer = 0f;
    }

    /**
     * Transitions to the chasing state.
     */
    private void enterChasing() {
        state = EnemyState.CHASING;
        patrolBehavior.clear();
        retreatTimer = 0f;
        resetPathing();
        chaseBehavior.reset();
    }

    /**
     * Resets pathing state and timers.
     */
    private void resetPathing() {
        path.clear();
        pathIndex = 0;
        pathRecalcTimer = 0f;
        lastGoalX = Integer.MIN_VALUE;
        lastGoalY = Integer.MIN_VALUE;
    }

    //TODO see if this is needed
    /**
     * Ensures the enemy is drawn above collectibles.
     */
    private void ensureAboveCollectibles() {
        if (getStage() == null) {
            return;
        }
        int maxPickupZ = -1;
        for (int i = 0; i < getStage().getActors().size; i++) {
            if (getStage().getActors().get(i) instanceof Collectible) {
                int z = getStage().getActors().get(i).getZIndex();
                if (z > maxPickupZ) {
                    maxPickupZ = z;
                }
            }
        }
        if (maxPickupZ >= 0) {
            int targetZ = Math.min(maxPickupZ + 1, getStage().getActors().size - 1);
            if (player != null) {
                int playerZ = player.getZIndex();
                targetZ = Math.min(targetZ, Math.max(playerZ - 1, 0));
            }
            setZIndex(targetZ);
        }
    }
}
