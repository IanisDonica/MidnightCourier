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

public class Player extends Entity {
    private final CollisionHandler collisionHandler;
    private final GameOverListener gameOverListener;
    protected Animation<TextureRegion> stunnedDownAnimation;
    protected Animation<TextureRegion> stunnedUpAnimation;
    protected Animation<TextureRegion> stunnedLeftAnimation;
    protected Animation<TextureRegion> stunnedRightAnimation;
    private GameOverListener deathOverListener;
    private boolean moveUp, moveDown, moveLeft, moveRight;
    private boolean sprinting;
    private int maxHp = 3;
    private int hp = maxHp;
    private float speedUpTimer = 0;
    private float drinkDurationMultiplier = 1f;
    private boolean hasKey = false;
    private boolean stunned = false;
    private float stunDuration;
    private char lastInputDirection = 'd';
    private boolean gameOverTriggered = false;
    private float speedMultiplier = 1f;
    private float speedX, speedY;
    private boolean potholeImmune = false;
    private float worldWidth = 0f;
    private float worldHeight = 0f;
    private float debugSpeedMultiplier = 1f;
    private boolean godMode = false;
    private final DriftyMovementController driftyMovementController;
    /// private final GameOverListener trapOverListener;

    //Initialize the player on a specific coordinate point
    public Player(TiledMapTileLayer collisionLayer, float x, float y, GameOverListener gameOverListener) {
        super(x, y);
        initialiseAnimations();
        setSize(0.75f, 0.75f);
        this.collisionHandler = new CollisionHandler(collisionLayer);
        this.gameOverListener = gameOverListener;
        this.driftyMovementController = new DriftyMovementController();
    }

    public void setMoveUp(boolean moveUp) {
        this.moveUp = moveUp;
        if (moveUp) {
            lastInputDirection = 'u';
        }
    }

    public void setMoveDown(boolean moveDown) {
        this.moveDown = moveDown;
        if (moveDown) {
            lastInputDirection = 'd';
        }
    }

    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
        if (moveLeft) {
            lastInputDirection = 'l';
        }
    }

    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
        if (moveRight) {
            lastInputDirection = 'r';
        }
    }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }

    public void setSpeedMultiplier(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public void setDebugSpeedMultiplier(float debugSpeedMultiplier) {
        this.debugSpeedMultiplier = debugSpeedMultiplier;
    }

    public void setWorldBounds(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void setPotholeImmune(boolean potholeImmune) {
        this.potholeImmune = potholeImmune;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setDrinkDurationMultiplier(float drinkDurationMultiplier) {
        this.drinkDurationMultiplier = drinkDurationMultiplier;
    }

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

    public void damage(int damage) {
        if (godMode) {
            return;
        }
        hp -= damage;
        if (hp <= 0 && !gameOverTriggered) {
            gameOverTriggered = true;
            if (damage >= 999 && deathOverListener != null) {
                deathOverListener.onGameOver();
            } else if (gameOverListener != null) {
                gameOverListener.onGameOver();
            }
            return;
        }
        stunned = true;
        stunDuration = 0.5f;
    }

    public void setDeathOverListener(GameOverListener deathOverListener) {
        this.deathOverListener = deathOverListener;
    }

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

    public DriftyMovementController getMovementController() {
        return driftyMovementController;
    }

    public void drinkEnergyDrink() {
        //This method seems useless, but it its mostly for later in order to handle sounds / screen effects etc.
        speedUpTimer = 5f * drinkDurationMultiplier;
    }

    public void pickupKey() {
        // For more complex logic later
        this.hasKey = true;
    }

    public float getSpeedX(){
        return speedX;
    }

    public float getSpeedY(){
        return speedY;
    }

    public void clearKey() {
        this.hasKey = false;
    }

    public boolean hasKey() {
        return hasKey;
    }

    public int getHp() {
        return this.hp;
    }

    public void setHp(int hp) {
        this.hp = Math.min(hp, maxHp);
    }

    public boolean isStunned() {
        return stunned;
    }

    public boolean isPotholeImmune() {
        return potholeImmune;
    }

    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
    }

    public boolean isGodMode() {
        return godMode;
    }

    public boolean isGameOverTriggered() {
        return gameOverTriggered;
    }

    public interface GameOverListener {
        void onGameOver();
    }
}
