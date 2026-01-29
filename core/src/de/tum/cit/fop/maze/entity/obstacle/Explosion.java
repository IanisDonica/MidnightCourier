package de.tum.cit.fop.maze.entity.obstacle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import de.tum.cit.fop.maze.entity.DeathCause;
import de.tum.cit.fop.maze.entity.Player;

public class Explosion extends Actor {
    private static final float LIFETIME = 0.6f;
    private static TextureRegion texture;
    private static boolean textureInitialized = false;
    private final Player player;
    private final float radius;
    private float timer = 0f;
    private boolean damaged = false;

    public Explosion(float centerX, float centerY, float radius, Player player) {
        this.player = player;
        this.radius = radius;
        if (!textureInitialized) {
            textureInitialized = true;
            Texture textureSheet = new Texture(Gdx.files.internal("objects.png"));
            texture = new TextureRegion(textureSheet, 64, 48, 16, 16);
        }
        setSize(5f, 5f);
        setPosition(centerX - getWidth() / 2f, centerY - getHeight() / 2f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!damaged) {
            damaged = true;
            float playerCenterX = player.getX() + player.getWidth() / 2f;
            float playerCenterY = player.getY() + player.getHeight() / 2f;
            float dx = playerCenterX - (getX() + getWidth() / 2f);
            float dy = playerCenterY - (getY() + getHeight() / 2f);
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist <= radius) {
                player.damage(1, DeathCause.BMW_EXPLOSION);
            }
        }
        timer += delta;
        if (timer >= LIFETIME) {
            remove();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }
}
