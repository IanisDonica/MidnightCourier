package de.tum.cit.fop.maze.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.system.CollisionHandler;
import de.tum.cit.fop.maze.system.DriftyMovementController;

/**
 * Player entity with movement, health, and interaction state.
 */
public class Player extends Entity {
    /** Collision handler for movement checks. */
    private final CollisionHandler collisionHandler;
    /** Listener for game-over events. */
    private final GameOverListener gameOverListener;
    /** Stunned animation for facing down. */
    protected Animation<TextureRegion> stunnedDownAnimation;
    /** Stunned animation for facing up. */
    protected Animation<TextureRegion> stunnedUpAnimation;
    /** Stunned animation for facing left. */
    protected Animation<TextureRegion> stunnedLeftAnimation;
    /** Stunned animation for facing right. */
    protected Animation<TextureRegion> stunnedRightAnimation;
    /** Listener for death cause events. */
    private DeathCauseListener deathCauseListener;
    /** Movement input flags. */
    private boolean moveUp, moveDown, moveLeft, moveRight;
    /** Whether sprinting is active. */
    private boolean sprinting;
    /** Maximum hit points. */
    private int maxHp = 3;
    /** Current hit points. */
    private int hp = maxHp;
    /** Timer for speed-up buff. */
    private float speedUpTimer = 0;
    /** Multiplier for drink duration. */
    private float drinkDurationMultiplier = 1f;
    /** Whether the player has the key. */
    private boolean hasKey = false;
    /** Whether the player is stunned. */
    private boolean stunned = false;
    /** Remaining stun duration. */
    private float stunDuration;
    /** Last input direction for knockback. */
    private char lastInputDirection = 'd';
    /** Whether game-over was already triggered. */
    private boolean gameOverTriggered = false;
    /** Speed multiplier from upgrades or buffs. */
    private float speedMultiplier = 1f;
    /** Frame movement deltas. */
    private float speedX, speedY;
    /** Whether pothole damage is ignored. */
    private boolean potholeImmune = false;
    /** Multiplier for enemy detection radius. */
    private float detectionRangeMultiplier = 1f;
    /** World bounds width. */
    private float worldWidth = 0f;
    /** World bounds height. */
    private float worldHeight = 0f;
    /** Debug speed multiplier. */
    private float debugSpeedMultiplier = 1f;
    /** Whether god mode is enabled. */
    private boolean godMode = false;
    /** Movement controller for drift-style motion. */
    private final DriftyMovementController driftyMovementController;
    /// private final GameOverListener trapOverListener;

    //Initialize the player on a specific coordinate point
    /**
     * Creates a player at a given position.
     *
     * @param collisionLayer collision layer for movement checks
     * @param x starting x position
     * @param y starting y position
     * @param gameOverListener listener for game-over events
     */
    public Player(TiledMapTileLayer collisionLayer, float x, float y, GameOverListener gameOverListener) {
        super(x, y);
        initialiseAnimations();
        setSize(0.75f, 0.75f);
        this.collisionHandler = new CollisionHandler(collisionLayer);
        this.gameOverListener = gameOverListener;
        this.driftyMovementController = new DriftyMovementController();
    }

    /**
     * Sets upward movement input.
     *
     * @param moveUp whether moving up
     */
    public void setMoveUp(boolean moveUp) {
        this.moveUp = moveUp;
        if (moveUp) {
            lastInputDirection = 'u';
        }
    }

    /**
     * Sets downward movement input.
     *
     * @param moveDown whether moving down
     */
    public void setMoveDown(boolean moveDown) {
        this.moveDown = moveDown;
        if (moveDown) {
            lastInputDirection = 'd';
        }
    }

    /**
     * Sets left movement input.
     *
     * @param moveLeft whether moving left
     */
    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
        if (moveLeft) {
            lastInputDirection = 'l';
        }
    }

    /**
     * Sets right movement input.
     *
     * @param moveRight whether moving right
     */
    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
        if (moveRight) {
            lastInputDirection = 'r';
        }
    }

    /**
     * Sets sprinting state.
     *
     * @param sprinting whether sprinting
     */
    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }

    /**
     * Sets movement speed multiplier.
     *
     * @param speedMultiplier new multiplier
     */
    public void setSpeedMultiplier(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    /**
     * Sets debug speed multiplier.
     *
     * @param debugSpeedMultiplier new debug multiplier
     */
    public void setDebugSpeedMultiplier(float debugSpeedMultiplier) {
        this.debugSpeedMultiplier = debugSpeedMultiplier;
    }

    /**
     * Sets world bounds used for movement constraints.
     *
     * @param worldWidth world width
     * @param worldHeight world height
     */
    public void setWorldBounds(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    /**
     * Sets whether the player is immune to pothole damage.
     *
     * @param potholeImmune immunity flag
     */
    public void setPotholeImmune(boolean potholeImmune) {
        this.potholeImmune = potholeImmune;
    }

    /**
     * Sets the enemy detection radius multiplier.
     *
     * @param detectionRangeMultiplier multiplier for enemy vision range
     */
    public void setDetectionRangeMultiplier(float detectionRangeMultiplier) {
        this.detectionRangeMultiplier = detectionRangeMultiplier;
    }

    /**
     * Returns the enemy detection radius multiplier.
     *
     * @return detection range multiplier
     */
    public float getDetectionRangeMultiplier() {
        return detectionRangeMultiplier;
    }

    /**
     * Sets maximum hit points.
     *
     * @param maxHp new max HP
     */
    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    /**
     * Returns maximum hit points.
     *
     * @return max HP
     */
    public int getMaxHp() {
        return maxHp;
    }

    /**
     * Sets multiplier for energy drink duration.
     *
     * @param drinkDurationMultiplier new multiplier
     */
    public void setDrinkDurationMultiplier(float drinkDurationMultiplier) {
        this.drinkDurationMultiplier = drinkDurationMultiplier;
    }

    /**
     * Initializes player animation frames.
     */
    private void initialiseAnimations() {
        Texture walkSheet = new Texture(Gdx.files.internal("character.png"));
        Texture walkSheetDownUp = new Texture(Gdx.files.internal("CharacterUpDown.png"));
        Texture walkSheetRight = new Texture(Gdx.files.internal("Character_Right.png"));
        Texture walkSheetLeft = new Texture(Gdx.files.internal("Character_Left.png"));
        Texture stunLeftSheet = new Texture(Gdx.files.internal("CharacterLeftStun.png"));
        Texture stunRightSheet = new Texture(Gdx.files.internal("CharacterRightStun.png"));
        Texture stunUpDownSheet = new Texture(Gdx.files.internal("CharacterUpDownStun.png"));

        int frameWidth = 16;
        int frameHeight = 32;
        int animationFrames = 4;

        int frameWidthDownUp = 11;
        int frameHeightDownUp = 20;
        ///int animationFramesDownUp = 2;

        int frameWidthRightLeft = 16;
        int frameHeightRightLeft = 20;

        Array<TextureRegion> walkDownFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> walkRightFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> walkUpFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> walkLeftFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> stunnedDownFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> stunnedUpFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> stunnedLeftFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> stunnedRightFrames = new Array<>(TextureRegion.class);

        for (int col = 0; col < animationFrames; col++) {
            walkUpFrames.add(new TextureRegion(walkSheetDownUp, col * frameWidthDownUp, 0, frameWidthDownUp, frameHeightDownUp));
            walkRightFrames.add(new TextureRegion(walkSheetRight, col * frameWidthRightLeft, 0, frameWidthRightLeft, frameHeightRightLeft));
            walkDownFrames.add(new TextureRegion(walkSheetDownUp, col * frameWidthDownUp, 20, frameWidthDownUp, frameHeightDownUp));
            walkLeftFrames.add(new TextureRegion(walkSheetLeft, col * frameWidthRightLeft, 0, frameWidthRightLeft, frameHeightRightLeft));
            stunnedLeftFrames.add(new TextureRegion(stunLeftSheet, col * frameWidthRightLeft, 0, frameWidthRightLeft, frameHeightRightLeft));
            stunnedRightFrames.add(new TextureRegion(stunRightSheet, col * frameWidthRightLeft, 0, frameWidthRightLeft, frameHeightRightLeft));
            stunnedUpFrames.add(new TextureRegion(stunUpDownSheet, col * frameWidthDownUp, 0, frameWidthDownUp, frameHeightDownUp));
            stunnedDownFrames.add(new TextureRegion(stunUpDownSheet, col * frameWidthDownUp, 20, frameWidthDownUp, frameHeightDownUp));
        }

        downAnimation = new Animation<>(0.15f, walkDownFrames);
        rightAnimation = new Animation<>(0.15f, walkRightFrames);
        upAnimation = new Animation<>(0.15f, walkUpFrames);
        leftAnimation = new Animation<>(0.15f, walkLeftFrames);
        stunnedDownAnimation = new Animation<>(0.15f, stunnedDownFrames);
        stunnedUpAnimation = new Animation<>(0.15f, stunnedUpFrames);
        stunnedLeftAnimation = new Animation<>(0.15f, stunnedLeftFrames);
        stunnedRightAnimation = new Animation<>(0.15f, stunnedRightFrames);
    }

    /**
     * Applies damage with default death cause.
     *
     * @param damage damage amount
     */
    public void damage(int damage) {
        damage(damage, DeathCause.ARRESTED);
    }

    /**
     * Applies damage and triggers death handling if needed.
     *
     * @param damage damage amount
     * @param cause death cause
     */
    public void damage(int damage, DeathCause cause) {
        if (godMode) {
            return;
        }
        hp -= damage;
        if (hp <= 0 && !gameOverTriggered) {
            gameOverTriggered = true;
            if (deathCauseListener != null) {
                deathCauseListener.onDeath(cause);
            } else if (gameOverListener != null) {
                gameOverListener.onGameOver();
            }
            return;
        }
        stunned = true;
        stunDuration = 0.5f;
    }

    /**
     * Sets the death cause listener.
     *
     * @param deathCauseListener listener instance
     */
    public void setDeathCauseListener(DeathCauseListener deathCauseListener) {
        this.deathCauseListener = deathCauseListener;
    }

    /**
     * Draws the player using the current animation frame.
     *
     * @param batch sprite batch
     * @param parentAlpha parent alpha
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion currentFrame;
        if (stunned) {
            currentFrame = switch (facingDirection) {
                case 'l' -> stunnedLeftAnimation.getKeyFrame(animationTime, true);
                case 'r' -> stunnedRightAnimation.getKeyFrame(animationTime, true);
                case 'u' -> stunnedUpAnimation.getKeyFrame(animationTime, true);
                default -> stunnedDownAnimation.getKeyFrame(animationTime, true);
            };
        } else {
            currentFrame = switch (facingDirection) {
                case 'l' -> leftAnimation.getKeyFrame(animationTime, true);
                case 'r' -> rightAnimation.getKeyFrame(animationTime, true);
                case 'u' -> upAnimation.getKeyFrame(animationTime, true);
                default -> downAnimation.getKeyFrame(animationTime, true);
            };
        }

        batch.draw(currentFrame, getX(), getY(), getWidth(), getHeight());
    }

    /**
     * Updates player movement, animations, and camera following.
     *
     * @param delta frame delta time
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        if (!stunned) {
            // Update controller with current input
            // Controller now handles smooth rotation toward input direction
            driftyMovementController.update(delta, moveUp, moveDown, moveLeft, moveRight, sprinting);

            // Get velocity and apply delta scaling ONLY ONCE
            Vector2 vel = driftyMovementController.getVelocity();
            speedX = vel.x * delta * speedMultiplier;
            speedY = vel.y * delta * speedMultiplier;

            // Apply movement with collision checking
            float nextX = getX() + speedX;
            float nextY = getY() + speedY;

            if (speedX > 0) { // Moving right
                if (collisionHandler.checkCollision(this, 'r')) {
                    setX(nextX);
                } else {
                    driftyMovementController.velocity.x = 0;
                    driftyMovementController.velocity.y /= 2;
                }
            } else if (speedX < 0) { // Moving left
                if (collisionHandler.checkCollision(this, 'l')) {
                    setX(nextX);
                } else {
                    driftyMovementController.velocity.x = 0;
                    driftyMovementController.velocity.y /= 2;
                }
            }

            if (speedY > 0) { // Moving up
                if (collisionHandler.checkCollision(this, 'u')) {
                    setY(nextY);
                } else {
                    driftyMovementController.velocity.y = 0;
                    driftyMovementController.velocity.x /= 2;
                }
            } else if (speedY < 0) { // Moving down
                if (collisionHandler.checkCollision(this, 'd')) {
                    setY(nextY);
                } else {
                    driftyMovementController.velocity.y = 0;
                    driftyMovementController.velocity.x /= 2;
                }
            }

        } else {
            // Handle knockback
            float speed = 2.5f * delta * speedMultiplier;
            float deltaX = 0, deltaY = 0;
            switch (lastInputDirection) {
                case 'u' -> deltaY -= speed;
                case 'd' -> deltaY += speed;
                case 'l' -> deltaX += speed;
                case 'r' -> deltaX -= speed;
                default -> deltaX = 0;
            }

            stunDuration -= delta;
            if (stunDuration < 0) {
                stunned = false;
            }

            if ((deltaX > 0 && collisionHandler.checkCollision(this, 'r')) || (deltaX < 0 && collisionHandler.checkCollision(this, 'l'))){
                setX(getX() + deltaX);
            }

            if ((deltaY > 0 && collisionHandler.checkCollision(this, 'u')) || (deltaY < 0 && collisionHandler.checkCollision(this, 'd'))) {
                setY(getY() + deltaY);
            }
        }

        animationTime += delta;

        // Handle speed-up buff
        if (speedUpTimer > 0) {
            speedUpTimer -= delta;
        }

        float orientation = driftyMovementController.getOrientation();
        if (orientation >= 315 || orientation < 45) {
            facingDirection = 'r'; // Right (0°)
        } else if (orientation >= 45 && orientation < 135) {
            facingDirection = 'u'; // Up (90°)
        } else if (orientation >= 135 && orientation < 225) {
            facingDirection = 'l'; // Left (180°)
        } else {
            facingDirection = 'd'; // Down (270°)
        }

        // Update camera to follow player
        this.getStage().getCamera().position.set(getX() + getWidth() / 2, getY() + getHeight() / 2, 0);
        this.getStage().getCamera().update();
    }

    /**
     * Returns the movement controller.
     *
     * @return movement controller
     */
    public DriftyMovementController getMovementController() {
        return driftyMovementController;
    }

    /**
     * Applies the energy drink speed-up effect.
     */
    public void drinkEnergyDrink() {
        //This method seems useless, but it its mostly for later in order to handle sounds / screen effects etc.
        speedUpTimer = 5f * drinkDurationMultiplier;
    }

    /**
     * Marks the key as collected.
     */
    public void pickupKey() {
        // For more complex logic later
        this.hasKey = true;
    }

    /**
     * Returns current frame speed X.
     *
     * @return speedX
     */
    public float getSpeedX(){
        return speedX;
    }

    /**
     * Returns current frame speed Y.
     *
     * @return speedY
     */
    public float getSpeedY(){
        return speedY;
    }

    /**
     * Clears the key possession state.
     */
    public void clearKey() {
        this.hasKey = false;
    }

    /**
     * Returns whether the key is owned.
     *
     * @return {@code true} if key is owned
     */
    public boolean hasKey() {
        return hasKey;
    }

    /**
     * Returns current hit points.
     *
     * @return HP
     */
    public int getHp() {
        return this.hp;
    }

    /**
     * Sets current hit points, clamped to max HP.
     *
     * @param hp new HP value
     */
    public void setHp(int hp) {
        this.hp = Math.min(hp, maxHp);
    }

    /**
     * Returns whether the player is stunned.
     *
     * @return {@code true} if stunned
     */
    public boolean isStunned() {
        return stunned;
    }

    /**
     * Returns whether pothole immunity is active.
     *
     * @return {@code true} if immune
     */
    public boolean isPotholeImmune() {
        return potholeImmune;
    }

    /**
     * Enables or disables god mode.
     *
     * @param godMode new god mode state
     */
    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
    }

    /**
     * Returns whether god mode is enabled.
     *
     * @return {@code true} if enabled
     */
    public boolean isGodMode() {
        return godMode;
    }

    /**
     * Returns whether game over has already been triggered.
     *
     * @return {@code true} if triggered
     */
    public boolean isGameOverTriggered() {
        return gameOverTriggered;
    }

    /**
     * Listener for generic game-over events.
     */
    public interface GameOverListener {
        void onGameOver();
    }

    /**
     * Listener for death events with a cause.
     */
    public interface DeathCauseListener {
        void onDeath(DeathCause cause);
    }
}
