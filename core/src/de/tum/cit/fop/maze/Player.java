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
    private boolean moveUp, moveDown, moveLeft, moveRight;

    // So they can later be remapped by the player
    private final int moveUpKeys = Input.Keys.W;
    private final int moveDownKeys = Input.Keys.S;
    private final int moveLeftKeys = Input.Keys.A;
    private final int moveRightKeys = Input.Keys.D;

    private float deltaX;
    private float deltaY;
    private float nextX;
    private float nextY;

    private boolean moving;


    public Player(TiledMap map) {
        this.map = map;
        initialiseAnimations();
        setSize(1,2);

        this.collisionLayer = (TiledMapTileLayer) map.getLayers().get("Walls");

        this.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return switch (keycode) {
                    case moveUpKeys -> {
                        moveUp = true;
                        yield true;
                    }
                    case moveDownKeys -> {
                        moveDown = true;
                        yield true;
                    }
                    case moveLeftKeys -> {
                        moveLeft = true;
                        yield true;
                    }
                    case moveRightKeys -> {
                        moveRight = true;
                        yield true;
                    }
                    default -> false; // Key not handled
                };
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                return switch (keycode) {
                    case moveUpKeys -> {
                        moveUp = false;
                        yield true;
                    }
                    case moveDownKeys -> {
                        moveDown = false;
                        yield true;
                    }
                    case moveLeftKeys -> {
                        moveLeft = false;
                        yield true;
                    }
                    case moveRightKeys -> {
                        moveRight = false;
                        yield true;
                    }
                    default -> false;
                };
            }
        });
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

        float deltaX = 0, deltaY = 0;

        moving = false;

        if (moveUp) { deltaY += speed; moving = true; }
        if (moveDown) { deltaY += -speed; moving = true; }
        if (moveLeft) { deltaX += -speed; moving = true; }
        if (moveRight) { deltaX += speed; moving = true; }

        float nextX = deltaX + getX(), nextY = deltaY + getY();

        if (!isCellBlocked(nextX, nextY)) {
            this.setPosition(nextX, nextY);
        } else {
            float halfX = (nextX - nextX % 1) + 0.5f;
            float halfY = (nextY - nextY % 1) + 0.5f;

            // TODO Fix corder collision make code more clear

            //System.out.printf("HalfX/Y(%s:%s)%n", halfX, halfY);
            //Xor operations (ie, only 1 movement direction)
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
