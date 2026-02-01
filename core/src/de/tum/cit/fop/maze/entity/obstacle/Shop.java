package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.tum.cit.fop.maze.system.HUD;
import de.tum.cit.fop.maze.entity.MapObject;

/**
 * Shop obstacle that enables the shop button when nearby.
 */
public class Shop extends MapObject {
    /** Texture region for the shop sprite. */
    private final TextureRegion textureRegion;
    /** HUD used to show the shop button. */
    private final HUD hud;

    /**
     * Creates a shop at the given position.
     *
     * @param x x position
     * @param y y position
     * @param hud HUD for shop button visibility
     */
    public Shop(float x, float y, HUD hud) {
        setPosition(x, y);
        setSize(3, 2);
        this.hud = hud;
        Texture textureSheet = new Texture(Gdx.files.internal("Shop.png"));
        this.textureRegion = new TextureRegion(textureSheet, 0, 0, frameWidth * 6, frameHeight * 4);
    }

    /**
     * Expands bounds to allow interaction at a distance.
     */
    @Override
    protected void updateBounds() {
        // Since we want the game to actually show the open shop button even when the player i'snt hugging it
        bounds.set(getX() - 1f, getY() - 1f, getWidth() + 2f, getHeight() + 2f);
    }


    /**
     * Shows the shop button when the player collides.
     */
    @Override
    protected void collision() {
        if (hud != null) {
            hud.setShopButtonVisible(true);
        }
    }

    /**
     * Draws the shop sprite.
     *
     * @param batch sprite batch
     * @param parentAlpha parent alpha
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(textureRegion, getX(), getY(), getWidth(), getHeight());
    }
}
