package de.tum.cit.fop.maze.entity.collectible;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import de.tum.cit.fop.maze.entity.Entity;
import de.tum.cit.fop.maze.entity.Player;

public class Collectible extends Entity {
    protected final Rectangle bounds = new Rectangle();
    protected Player player;
    private boolean addedToStageFired = false;

    public Collectible(float x, float y, int w, int h) {
        super(x,y);
        setSize(w,h);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!addedToStageFired && getStage() != null) {
            addedToStageFired = true;
            onAddedToStage();
        }

        //These values are calculated to account for the fact that the heart texture isnt fully 16x16
        //When Nicolas will give us the new textures this will have to be changed.
        bounds.set(getX() + 0.25f,getY() + 0.5f, getWidth() / 2 ,getHeight() / 4);
        if(checkCollisionsWithPlayer()) { this.collision(); }
    }

    protected void onAddedToStage() {
        Stage stage = getStage();

        for (Actor actor : stage.getActors()) {
            if (actor instanceof Player pl) {
                player = pl;
                return;
            }
        }

        throw new RuntimeException("Player must be added before Collectibles");
    }

    protected boolean checkCollisionsWithPlayer() {
        return bounds.overlaps(new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight()));
    }

    protected void collision() {
        return;
    }
}
