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
 * Enemy (Policeman/Jandarmeria class)
 */
public class Enemy extends Obstacle {
    private enum EnemyState {CHASING,PATROLLING,PATROL_WAIT,RETREATING,RETREAT_WAIT}

    private static final float PATH_RECALC_INTERVAL = 0.5f;
    /** Target distance threshold for path steps */
    private static final float TARGET_EPS = 0.05f;
    /** The (game units) distance at which a tile is considered "centered" */
    private static final float CENTER_EPS = 0.02f;
    /** Speed scale when patrolling or retreating */
    private static final float PATROL_SPEED_SCALE = 0.5f;
    private static final int VISION_RANGE_TILES = 20;
    private static final float MAX_RETREAT_DURATION_SECONDS = 4f;

    /** Time (seconds) for which the police will run after the player,
     *  after that if they are still in chase mode, they will start running slower
     *   to give the player a chance to escape them
     * */
    private static final float RUN_DURATION_SECONDS = 10f;
    private static final float RUN_SPEED_MULTIPLIER = 2f;
    /** Collision layer for movement checks */
    private final TiledMapTileLayer collisionLayer;
    private final int mapWidth, mapHeight;
    private final float baseSpeed = 2.2f;
    /** Pathfinder for tile navigation */
    private final Pathfinder pathfinder;
    /** Behavior controller for chasing/retreating/patroling states */
    private final ChaseBehavior chaseBehavior;
    private final RetreatBehavior retreatBehavior;
    private final PatrolBehaviour patrolBehavior;

    /** This is for the global retreat functionality, (if the player gets hit, all enemies that chase him will go into retreat) the enemies
     *  constantly checks to see if a retreat has been activated (global token is higher than local one), if yes - it increments
     *  the local token (ack the retreat) and retreats if appropritate (chasing the player), doing it this way (as opposed to keeping a boolean value)
     *  ensures that the player can still be targeted (by guards not witness to their arrest) and simplifies the logic.

     *  the retreat check is done at the beginning of act(), and the global retreat token is done in the collision code.*/
    private static int globalRetreatToken = 0;
    private int lastRetreatToken = 0;

    /** Current path of tile points */
    private ArrayList<GridPoint2> path = new ArrayList<>();
    /** Current index in the path */
    private int pathIndex = 0;
    private float pathRecalcTimer = 0f;
    private float retreatTimer = 0f;
    private float runTimer = 0f;
    private boolean isRunning = false;
    private float centerX = 0f, centerY = 0f;
    /** Last path goal x/y coordinates, MIN_VALUE to force a path recalc */
    private int lastGoalX = Integer.MIN_VALUE, lastGoalY = Integer.MIN_VALUE;
    /** Current behavior state */
    private EnemyState state = EnemyState.CHASING; // Initial state
    /** Facing direction for animation */
    private char facingDirection = 'd';
    private static Animation<TextureRegion> walkNorthAnimation, walkSouthAnimation, walkEastAnimation, walkWestAnimation;
    private static boolean animationsInitialized = false;

    /**
     * Creates an enemy at a given position
     *
     * @param collisionLayer collision layer for movement checks
     * @param x spawn x position
     * @param y spawn y position
     */
    public Enemy(TiledMapTileLayer collisionLayer, float x, float y) {
        super(x, y, 1,1, 0,0,3);
        this.collisionLayer = collisionLayer;
        pathfinder = new Pathfinder(collisionLayer);
        mapWidth = collisionLayer.getWidth(); mapHeight = collisionLayer.getHeight();
        chaseBehavior = new ChaseBehavior(mapWidth, mapHeight, collisionLayer);
        retreatBehavior = new RetreatBehavior(mapWidth, mapHeight, collisionLayer);
        patrolBehavior = new PatrolBehaviour(mapWidth, mapHeight, collisionLayer);

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
     * Initializes behavior once added to the stage.
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
        super.act(delta);
        centerX = getX() + getWidth() / 2f; centerY = getY() + getHeight() / 2f;

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
                if (!isRunning && canSeePlayer()) {
                    isRunning = true;
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
            int goalX = coords[2], goalY = coords[3];

            if (!needsRepath) {
                needsRepath = goalX != lastGoalX || goalY != lastGoalY;
            }

            if (needsRepath) {
                path = pathfinder.findPath(coords[0], coords[1], goalX, goalY);
                pathIndex = 0;
                lastGoalX = goalX; lastGoalY = goalY;
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
        if (isRunning) {
            runTimer += delta;
            if (runTimer >= RUN_DURATION_SECONDS) {
                isRunning = false;
                runTimer = 0f;
            }
        }

        if (pathIndex >= path.size()) {
            moveToTileCenter(delta);
            return;
        }
        GridPoint2 target = path.get(pathIndex);

        float targetX = target.x + 0.5f, targetY = target.y + 0.5f;
        float dx = targetX - centerX, dy = targetY - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < TARGET_EPS) {
            pathIndex++;
            return;
        }

        updateFacingDirection(dx, dy);
        float speedScale = (state == EnemyState.PATROLLING || state == EnemyState.RETREATING) ? PATROL_SPEED_SCALE : 1f;
        if (isRunning && state == EnemyState.CHASING) {
            speedScale *= RUN_SPEED_MULTIPLIER;
        }
        float step = Math.min(baseSpeed * speedScale * delta, dist);
        setPosition(getX() + (dx / dist) * step, getY() + (dy / dist) * step);
    }

    /**
     * Moves the enemy toward the center of the current tile.
     *
     * @param delta frame delta time
     */
    private void moveToTileCenter(float delta) {
        float targetX = MathUtils.floor(centerX) + 0.5f, targetY = MathUtils.floor(centerY) + 0.5f;
        float dx = targetX - centerX, dy = targetY - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < TARGET_EPS) {
            setPosition(getX() + dx, getY() + dy);
            return;
        }
        updateFacingDirection(dx, dy);
        float step = Math.min(baseSpeed * delta, dist);
        setPosition(getX() + (dx / dist) * step, getY() + (dy / dist) * step);
    }

    /**
     * Updates the facing direction based on the movement vector.
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
     * Initializes the walking animations once.

     * Will return for all but the first enemy (since it's static)
     * the idea is to not have the same texture loaded for every enemy object
     * curently this also means that all animations are synced.
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

        int frameW = 24, frameH = 24;

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
        List<GridPoint2> candidates = filterSpawnCandidates(walkableTiles, player, collisionLayer.getWidth(), collisionLayer.getHeight(), 2);
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
        int width = collisionLayer.getWidth(), height = collisionLayer.getHeight();
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
     * Checks whether spawning at a location would collide with actors.
     *
     * @param stage stage containing actors
     * @param x spawn x
     * @param y spawn y
     * @return {@code true} if collision would occur
     */
    private static boolean wouldCollideAt(Stage stage, float x, float y) {
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
            player.playSoundEffect("siren.ogg", 1f);
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
    @SuppressWarnings("DuplicatedCode")
    // Creating a helper class is too much for this
    // Adding them as obj vars isn't correct as we must guarantee that they are always correct,
    // and computing them in act() isn't enough (submethods can change the position of the enemy).
    // Thus, actually removing the duplicateCode would be too much of a hassle
    private boolean isCenteredOnTile() {
        float targetX = MathUtils.floor(centerX) + 0.5f, targetY = MathUtils.floor(centerY) + 0.5f;
        float dx = targetX - centerX, dy = targetY - centerY;

        if (Math.abs(dx) < CENTER_EPS && Math.abs(dy) < CENTER_EPS) {
            setPosition(getX() + dx, getY() + dy);
            return true;
        }
        return false;
    }

    /**
     * Determines if the enemy has a line of sight to the player.
     *
     * @return {@code true} if the player is visible
     */
    private boolean canSeePlayer() {
        int startX = clampTileX(getX() + getWidth() / 2f), startY = clampTileY(getY() + getHeight() / 2f);
        int goalX = clampTileX(player.getX() + player.getWidth() / 2f), goalY = clampTileY(player.getY() + player.getHeight() / 2f);

        int visionRange = Math.max(1, Math.round(VISION_RANGE_TILES * player.getDetectionRangeMultiplier()));
        if (Math.abs(goalX - startX) > visionRange || Math.abs(goalY - startY) > visionRange) {
            return false;
        }

        int dx = Math.abs(goalX - startX),dy = Math.abs(goalY - startY);
        int sx = startX < goalX ? 1 : -1, sy = startY < goalY ? 1 : -1;
        int err = dx - dy;
        int x = startX, y = startY;

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
        isRunning = false;
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
        isRunning = false;
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
        isRunning = false;
        runTimer = 0f;
        pathRecalcTimer = 0f;
    }

    /**
     * Transitions to the retreat waiting state.
     */
    private void enterRetreatWait() {
        state = EnemyState.RETREAT_WAIT;
        retreatBehavior.startWaiting();
        isRunning = false;
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
     * Setting lastGoalX/Y to MIN_VALUE forces a path recalc
     * next tick since there will be no matching real tile
     */
    private void resetPathing() {
        path.clear();
        pathIndex = 0;
        pathRecalcTimer = 0f;
        lastGoalX = Integer.MIN_VALUE; lastGoalY = Integer.MIN_VALUE;
    }

    /**
     * Ensures the enemy is drawn above collectibles.

     * Find the highest z-index among collectibles, then place the enemy just above them
     * while still staying below the player (if present) and within actor bounds.

     * Decided to do it here instead of in the gameScreen because it simplified the logic
     * (entities may still be spawned midgame, so keeping track of it inside the EnemyLogic is simpler)
     */
    private void ensureAboveCollectibles() {
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
