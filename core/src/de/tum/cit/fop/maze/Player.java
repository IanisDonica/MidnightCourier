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
    private boolean moveLeft, moveRight, moveUp, moveDown;

    // So they can later be remapped by the player
    private final int moveUpKeys = Input.Keys.W;
    private final int moveDownKeys = Input.Keys.S;
    private final int moveLeftKeys = Input.Keys.A;
    private final int moveRightKeys = Input.Keys.D;

    private boolean moving;


    public Player(TiledMap map) {
        this.map = map;
        initialiseAnimations();
        setSize(16,32);

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
        float speed = 50f * delta;

        float deltaX = 0, deltaY = 0;

        moving = false;

        if (moveUp) { deltaY += speed; moving = true; }
        if (moveDown) { deltaY += -speed; moving = true; }
        if (moveLeft) { deltaX += -speed; moving = true; }
        if (moveRight) { deltaX += speed; moving = true; }

        float nextX = deltaX + getX(), nextY = deltaY + getY();

        if (!isCellBlocked(nextX, nextY, 16)) {
            this.setPosition(nextX, nextY);
        } else {
            float halfX, halfY;

            if (moveUp) {
                halfY = nextY - nextY % 16;
            } else if (moveDown) {
                halfY = nextY + 16 - nextY % 16;
            } else {
                halfY = nextY;
            }

            if (moveRight) {
                halfX = nextX - nextX % 16;
            } else if (moveLeft) {
                halfX = nextX + 16 - nextX % 16;
            } else {
                halfX = nextX;
            }

            if (!isCellBlocked(nextX, halfY, 16)) {
                this.setPosition(nextX, halfY);
            } else if (!isCellBlocked(halfX, nextY, 16)) {
                this.setPosition(halfX, nextY);
            } else {
                this.setPosition(halfX, halfY);
            }
        }

        if (moving) animationTime += delta;
        stateTime += delta;
    }

    private boolean isCellBlocked(float x, float y, float tileSize) {
        // Convert world pixels to grid coordinates
        int gridX = (int) ((x + 8) / tileSize);
        int gridY = (int) ((y + 8 )/ tileSize);

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(gridX, gridY);

        // If the cell exists, it's a wall (or check for a "blocked" property)
        return cell != null;
    }

    /*
        TODO: this should be a method of a Entity
        superclass that the Player and other entities
        (BMW drivers for example) should extend
    public boolean collisionCheck(int direction) {
        this.moving = true;
        return true;
    }
     */
}
