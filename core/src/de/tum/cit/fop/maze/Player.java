package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class Player extends Actor {
    private Animation<TextureRegion> characterAnimation;
    private float stateTime;
    private float animationTime;
    private final CollisionHandler collisionHandler;
    private float speed;

    /*
        Movement flags:
        When a keyDown event is registered, the specific flag is set to True,
        on a keyUp they are set to false. Did it so that the input doesn't have
        to be polled every frame, and so that there is more flexibility for stuff
        like speed-up animations/cooldown etc.
     */
    private boolean moveUp, moveDown, moveLeft, moveRight, moving;
    private boolean sprinting;

    public void setMoveUp(boolean moveUp) {
        this.moveUp = moveUp;
    }

    public void setMoveDown(boolean moveDown) {
        this.moveDown = moveDown;
    }

    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
    }

    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
    }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }

    public float getSpeed() {
        return speed;
    }

    public void multiplySpeed(float speedMultiplier) {
        this.speed *= speedMultiplier;
    }

    public Player(TiledMap map) {
        initialiseAnimations();
        setSize(1,2);
        this.collisionHandler = new CollisionHandler(map);
    }

    //Initialize the player on a specific coordinate point
    public Player(TiledMap map, float x, float y) {
        this(map);
        setX(x);
        setY(y);
    }

    private void initialiseAnimations() {
        Texture walkSheet = new Texture(Gdx.files.internal("character.png"));

        int frameWidth = 16;
        int frameHeight = 32;
        int animationFrames = 4;

        Array<TextureRegion> walkFrames = new Array<>(TextureRegion.class);

        for (int col = 0; col < animationFrames; col++) {
            walkFrames.add(new TextureRegion(walkSheet, col * frameWidth, 0, frameWidth, frameHeight));
        }

        characterAnimation = new Animation<>(0.1f, walkFrames);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion currentFrame = characterAnimation.getKeyFrame(animationTime, true);

        batch.draw(currentFrame, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        speed = 2.5f * delta;
        if (sprinting) { speed *= 1.6f; }

        float deltaX = 0, deltaY = 0;


        if (moveUp) { deltaY += speed;}
        if (moveDown) { deltaY += -speed;}
        if (moveLeft) { deltaX += -speed;}
        if (moveRight) { deltaX += speed;}

        moving = false;
        if (deltaX != 0 || deltaY != 0) {
            float length = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            deltaX = deltaX * speed / length;
            deltaY = deltaY * speed / length;
            moving = true;
        }

        float nextX = deltaX + getX(), nextY = deltaY + getY();

        if (deltaY > 0 && collisionHandler.checkCollision(this, 'u')) setPosition(getX(), nextY);
        if (deltaY < 0 && collisionHandler.checkCollision(this, 'd')) setPosition(getX(), nextY);
        if (deltaX > 0 && collisionHandler.checkCollision(this, 'r')) setPosition(nextX, getY());
        if (deltaX < 0 && collisionHandler.checkCollision(this, 'l')) setPosition(nextX, getY());

        if (moving) {
            animationTime += delta;
            this.getStage().getCamera().position.set(getX() + getWidth() / 2, getY() + getHeight() / 2, 0);
            this.getStage().getCamera().update();
        }
        stateTime += delta;
    }

}
