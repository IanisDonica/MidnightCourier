package de.tum.cit.fop.maze;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Array;

public class Player extends Actor {
    private Animation<TextureRegion> characterAnimation;
    private float stateTime;
    private float animationTime;
    private final TiledMap map;
    private final TiledMapTileLayer collisionLayer;

    /*
        Movement flags:
        When a keyDown event is registered, the specific flag is set to True,
        on a keyUp they are set to false. Did it so that the input doesn't have
        to be polled every frame, and so that there is more flexibility for stuff
        like speed-up animations/cooldown etc.
     */
    private boolean moveUp, moveDown, moveLeft, moveRight, moving;
    private boolean sprinting;

    // So they can later be remapped by the player


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

    public Player(TiledMap map) {
        this.map = map;
        initialiseAnimations();
        setSize(1,2);

        this.collisionLayer = (TiledMapTileLayer) map.getLayers().get("Walls");
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
        float speed = 2f * delta;
        if (sprinting) { speed *= 2f; }

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

        if (!isCellBlocked(nextX, nextY)) {
            this.setPosition(nextX, nextY);
        } else {
            float halfX = (nextX - nextX % 1) + 0.5f;
            float halfY = (nextY - nextY % 1) + 0.5f;

            // TODO Fix corder collision make code more clear

            //System.out.printf("HalfX/Y(%s:%s)%n", halfX, halfY);
            //Xor operations (ie, only 1 movement direction)
            // TODO Make it so that the player can only move in one direction per axis,
            //  the code here assumes that that is already the case.
            if (moveUp ^ moveDown ^ moveLeft ^ moveRight) {
                if (moveUp || moveDown) {
                    if (!isCellBlocked(getX() - 0.001f, getY())) {
                        this.setPosition(getX() - 0.001f, getY());
                    } else {
                        this.setY(halfY);
                    }
                } else {
                    if (!isCellBlocked( getX(), getY() - 0.001f)) {
                        this.setPosition(getX(), getY() - 0.001f);
                    } else {
                        this.setY(halfY);
                    }
                }
            } else {
                if (moveLeft && moveUp) {
                    //Try and see if the X coordinate is the problem
                    if (!isCellBlocked(halfX, nextY)) {
                        setPosition(halfX, nextY);
                    }
                    // Try and see if the Y coordinate is the problem
                    else if (!isCellBlocked(nextX, halfY)) {
                        setPosition(nextX, halfY);
                    }
                    // Both are the problem
                    else {
                        setPosition(halfX, halfY);
                    }
                } else {
                    // Try and see if the Y coordinate is the problem
                    if (!isCellBlocked(nextX, halfY)) {
                        setPosition(nextX, halfY);
                    }
                    //Try and see if the X coordinate is the problem
                    else if (!isCellBlocked(halfX, nextY)) {
                        setPosition(halfX, nextY);
                    }
                    // Both are the problem
                    else {
                        setPosition(halfX, halfY);
                    }
                }
            }
        }

        if (moving) {
            animationTime += delta;
            this.getStage().getCamera().position.set(getX() + getWidth() / 2, getY() + getHeight() / 2, 0);
            this.getStage().getCamera().update();
        }
        stateTime += delta;
    }

    private boolean isCellBlocked(float x, float y) {
        float offsetX = 0, offsetY = 0;

        float halfX = (x - x % 1) + 0.4f;
        float halfY = (y - y % 1) + 0.4f;

        if (getX() >= halfX && moveRight) {offsetX = 0.02f;}
        if (getY() >= halfY && moveUp) {offsetY = 0.02f;}
        if (!moving) { return false; }

        int gridX = (int) (x + 0.5f - offsetX);
        int gridY = (int) (y + 0.5f - offsetY);

        //System.out.printf("Player(%s:%s), gridX:%s, gridY%s%n", getX(), getY(), gridX, gridY);

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(gridX, gridY);

        return cell != null;
    }
}
