package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import de.tum.cit.fop.maze.entity.MapObject;
import de.tum.cit.fop.maze.entity.Player;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for obstacles with animated sprites.
 */
public class Obstacle extends MapObject {
    /** Animation used for rendering. */
    Animation<TextureRegion> animation;
    /** Sprite sheet offsets and frame count. */
    int textureOffsetX, textureOffsetY, animationFrames;
    /** Shared sprite sheet texture. */
    private static Texture sharedTextureSheet;
    /** Whether the shared texture is initialized. */
    private static boolean textureInitialized = false;

    /**
     * Creates an obstacle with given sprite offsets and size.
     *
     * @param x x position
     * @param y y position
     * @param w width in tiles
     * @param h height in tiles
     * @param textureOffsetX x offset in the sprite sheet
     * @param textureOffsetY y offset in the sprite sheet
     * @param animationFrames number of animation frames
     */
    public Obstacle(float x, float y, int w, int h, int textureOffsetX, int textureOffsetY, int animationFrames) {
        setPosition(x, y);
        setSize(w, h);
        this.animationFrames = animationFrames;
        this.textureOffsetX = textureOffsetX;
        this.textureOffsetY = textureOffsetY;
        initAnimation();
    }

    /**
     * Initializes the animation frames from the shared sprite sheet.
     */
    protected void initAnimation() {
        //This is kinda duplicate, but it will later be useful
        if (!textureInitialized) {
            textureInitialized = true;
            sharedTextureSheet = new Texture(Gdx.files.internal("objects.png"));
        }
        Array<TextureRegion> frames = new Array<>(TextureRegion.class);

        for (int col = 0; col < animationFrames; col++) {
            frames.add(new TextureRegion(sharedTextureSheet, textureOffsetX + col * frameWidth, textureOffsetY, frameWidth, frameHeight));
        }

        animation = new Animation<>(0.25f, frames);
    }

    /**
     * Draws the obstacle's animation frame.
     *
     * @param batch sprite batch
     * @param parentAlpha parent alpha
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(animation.getKeyFrame(animationTime, true), getX(), getY(), getWidth(), getHeight());
    }

    protected static List<GridPoint2> filterSpawnCandidates(List<GridPoint2> tiles,
                                                            Player player,
                                                            int mapWidth,
                                                            int mapHeight,
                                                            int minDistance) {
        int playerTileX = clampTileCoord(player.getX() + player.getWidth() / 2f, mapWidth);
        int playerTileY = clampTileCoord(player.getY() + player.getHeight() / 2f, mapHeight);
        List<GridPoint2> candidates = new ArrayList<>(tiles.size());
        for (GridPoint2 tile : tiles) {
            if (Math.abs(tile.x - playerTileX) <= minDistance
                    && Math.abs(tile.y - playerTileY) <= minDistance) {
                continue;
            }
            candidates.add(tile);
        }
        return candidates;
    }

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
}
