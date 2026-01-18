package de.tum.cit.fop.maze.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.system.CollisionHandler;

import java.awt.*;

public class Player extends Entity {
    private final CollisionHandler collisionHandler;
    protected Animation<TextureRegion> stunnedAnimation;
    private boolean moveUp, moveDown, moveLeft, moveRight;
    private boolean sprinting;
    private int hp = 10;
    private float speedUpTimer = 0;
    private boolean hasKey = false;
    private boolean stunned = false;
    private float stunDuration;
    private char lastInputDirection = 'd';

    //Initialize the player on a specific coordinate point
    public Player(TiledMapTileLayer collisionLayer, float x, float y) {
        super(x, y);
        initialiseAnimations();
        setSize(1, 2);
        this.collisionHandler = new CollisionHandler(collisionLayer);
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

    private void initialiseAnimations() {
        Texture walkSheet = new Texture(Gdx.files.internal("character.png"));

        int frameWidth = 16;
        int frameHeight = 32;
        int animationFrames = 4;

        Array<TextureRegion> walkDownFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> walkRightFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> walkUpFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> walkLeftFrames = new Array<>(TextureRegion.class);
        Array<TextureRegion> stunnedFrames = new Array<>(TextureRegion.class);

        for (int col = 0; col < animationFrames; col++) {
            walkDownFrames.add(new TextureRegion(walkSheet, col * frameWidth, 0, frameWidth, frameHeight));
            walkRightFrames.add(new TextureRegion(walkSheet, col * frameWidth, 32, frameWidth, frameHeight));
            walkUpFrames.add(new TextureRegion(walkSheet, col * frameWidth, 64, frameWidth, frameHeight));
            walkLeftFrames.add(new TextureRegion(walkSheet, col * frameWidth, 96, frameWidth, frameHeight));
            stunnedFrames.add(new TextureRegion(walkSheet, 80 + col * frameWidth, 96, frameWidth, frameHeight));
        }

        downAnimation = new Animation<>(0.15f, walkDownFrames);
        rightAnimation = new Animation<>(0.15f, walkRightFrames);
        upAnimation = new Animation<>(0.15f, walkUpFrames);
        leftAnimation = new Animation<>(0.15f, walkLeftFrames);
        stunnedAnimation = new Animation<>(0.15f, stunnedFrames);
    }

    public void damage(int damage) {
        hp -= damage;
        if (hp < 1) {
            //TODO make it nice
            throw new RuntimeException("You died");
        }
        stunned = true;
        stunDuration = 0.5f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion currentFrame;
        if (stunned) {
            currentFrame = stunnedAnimation.getKeyFrame(animationTime, true);
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
        speed = 2.5f * delta;
        if (sprinting) {speed *= 2f;}
        float deltaX = 0, deltaY = 0;

        if (speedUpTimer > 0) {
            speed *= 2.5f;
            speedUpTimer -= delta; //This can go into the negatives but it shouldn't really be a big deal
        }

        if (!stunned) {
            if (moveUp) {deltaY += speed;}
            if (moveDown) {deltaY -= speed;}
            if (moveLeft) {deltaX -= speed;}
            if (moveRight) {deltaX += speed;}
        } else {
            switch (lastInputDirection) {
                case 'u' -> deltaY -= speed * 10;
                case 'd' -> deltaY += speed * 10;
                case 'l' -> deltaX += speed * 10;
                case 'r' -> deltaX -= speed * 10;
                default -> deltaX += speed * 10;
            }
            stunDuration -= delta;
            if (stunDuration < 0) {
                stunned = false;
            }
        }

        if (deltaX != 0 || deltaY != 0) {
            float length = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            deltaX = deltaX * speed / length;
            deltaY = deltaY * speed / length;
        } else return;

        float nextX = deltaX + getX(), nextY = deltaY + getY();

        if (deltaY > 0 && collisionHandler.checkCollision(this, 'u')) {
            setPosition(getX(), nextY);
            facingDirection = 'u';
        }
        if (deltaY < 0 && collisionHandler.checkCollision(this, 'd')) {
            setPosition(getX(), nextY);
            facingDirection = 'd';
        }
        if (deltaX > 0 && collisionHandler.checkCollision(this, 'r')) {
            setPosition(nextX, getY());
            facingDirection = 'r';
        }
        if (deltaX < 0 && collisionHandler.checkCollision(this, 'l')) {
            setPosition(nextX, getY());
            facingDirection = 'l';
        }

        animationTime += delta;
        this.getStage().getCamera().position.set(getX() + getWidth() / 2, getY() + getHeight() / 2, 0);
        this.getStage().getCamera().update();
    }

    public void drinkEnergyDrink() {
        //This method seems useless, but it its mostly for later in order to handle sounds / screen effects etc.
        speedUpTimer = 5f;
    }

    public void pickupKey() {
        // For more complex logic later
        this.hasKey = true;
    }

    public boolean hasKey() {
        return hasKey;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }
    public int getHp() {
        return this.hp;
    }

    public boolean isStunned() {
        return stunned;
    }
}
