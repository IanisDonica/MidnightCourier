package de.tum.cit.fop.maze.entity.collectible;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.system.PointManager;

public class EnergyDrink extends Collectible {
    public EnergyDrink(float x, float y, PointManager pointManager) {
        super(x, y, 1, 1, pointManager);
        initEnergyDrinkAnimation();
    }

    private void initEnergyDrinkAnimation() {
        Texture textureSheet = new Texture(Gdx.files.internal("energy-cans/energy-blau-animation.png"));
        Array<TextureRegion> spinFrames = new Array<>(TextureRegion.class);
        int frameWidth = 32;
        int frameHeight = 32;
        int framesPerRow = 4;
        int frameCount = 16;
        for (int index = 0; index < frameCount; index++) {
            int col = index % framesPerRow;
            int row = index / framesPerRow;
            spinFrames.add(new TextureRegion(
                textureSheet,
                col * frameWidth,
                row * frameHeight,
                frameWidth,
                frameHeight
            ));
        }
        spinAnimation = new Animation<>(0.25f, spinFrames);
    }

    @Override
    protected void collision() {
        if (!this.getPickedUp()) {
            this.player.drinkEnergyDrink();
            pointManager.add(50);
            markPickedUp();
        }
    }
}
