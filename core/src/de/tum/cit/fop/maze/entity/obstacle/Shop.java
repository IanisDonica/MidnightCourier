package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import de.tum.cit.fop.maze.HUD;
import de.tum.cit.fop.maze.entity.MapObject;

public class Shop extends MapObject {
    private final TextureRegion textureRegion;
    private final HUD hud;

    public Shop(float x, float y, HUD hud) {
        setPosition(x, y);
        setSize(4, 2);
        this.hud = hud;
        Texture textureSheet = new Texture(Gdx.files.internal("basictiles.png"));
        this.textureRegion = new TextureRegion(textureSheet, 0, 0, frameWidth * 4, frameHeight * 2);
    }

    @Override
    protected void updateBounds() {
        // Since we want the game to actually show the open shop button even when the player isnt hugging it
        bounds.set(getX() - 1f, getY() - 1f, getWidth() + 2f, getHeight() + 2f);
    }


    @Override
    protected void collision() {
        if (hud != null) {
            hud.setShopButtonVisible(true);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(textureRegion, getX(), getY(), getWidth(), getHeight());
    }
}
