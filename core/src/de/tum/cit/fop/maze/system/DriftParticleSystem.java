package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.entity.Player;

public class DriftParticleSystem extends Actor {

    private static class Particle {
        float x, y;
        float vx, vy;
        float life;
        float maxLife;

        Particle(float x, float y, float vx, float vy, float life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
        }

        void update(float delta) {
            x += vx * delta;
            y += vy * delta;
            life -= delta;
        }

        boolean isDead() {
            return life <= 0;
        }

        float getAlpha() {
            return (life / maxLife) * 0.7f;
        }
    }

    private Array<Particle> particles;
    private Player player;
    private float spawnCooldown = 0;
    private static final float SPAWN_INTERVAL = 0.04f;
    private static final float PARTICLE_LIFE = 0.6f;
    private static final float PARTICLE_SIZE = 0.15f;

    public DriftParticleSystem(Player player) {
        this.player = player;
        this.particles = new Array<>();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Update existing particles
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(delta);
            if (p.isDead()) {
                particles.removeIndex(i);
            }
        }

        // Spawn new particles if drifting
        spawnCooldown -= delta;
        if (spawnCooldown <= 0) {
            if (isDrifting()) {
                spawnDriftParticles();
            }
            spawnCooldown = SPAWN_INTERVAL;
        }
    }

    private boolean isDrifting() {
        Vector2 velocity = player.getMovementController().getVelocity();

        // Only drift if moving fast enough
//        if (velocity.len() < 2f) {
//            return false;
//        }

        float movementAngle = velocity.angleDeg();
        float facingAngle = player.getMovementController().getOrientation();

        float angleDiff = Math.abs(movementAngle - facingAngle);
        if (angleDiff > 180) {
            angleDiff = 360 - angleDiff;
        }

        return angleDiff > 5;
    }

    private void spawnDriftParticles() {
        float centerX = player.getX() + player.getWidth() / 2;
        float centerY = player.getY() + player.getHeight() / 8;

        Vector2 velocity = player.getMovementController().getVelocity();
        float movementAngle = velocity.angleDeg();

        // Back of player (opposite movement direction)
        float backAngle = movementAngle + 180;

        // Spawn position behind player
        float spawnX = centerX + MathUtils.cosDeg(backAngle) * 0.25f;
        float spawnY = centerY + MathUtils.sinDeg(backAngle) * 0.25f;

        // Particle velocity spreads from back
        float speed = 5f;
        float spread = 30f;

        // Spawn 2-4 particles per frame
        int count = MathUtils.random(2, 4);
        for (int i = 0; i < count; i++) {
            float angle = backAngle + MathUtils.random(-spread, spread);
            float vx = MathUtils.cosDeg(angle) * speed;
            float vy = MathUtils.sinDeg(angle) * speed;

            particles.add(new Particle(spawnX, spawnY, vx, vy, PARTICLE_LIFE));
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        // Draw all particles as gray squares
        for (Particle p : particles) {
            float alpha = p.getAlpha();
            batch.setColor(0.6f, 0.6f, 0.6f, alpha);
            batch.draw(getWhitePixel(), p.x - PARTICLE_SIZE / 2, p.y - PARTICLE_SIZE / 2,
                    PARTICLE_SIZE, PARTICLE_SIZE);
        }

        batch.setColor(Color.WHITE); // Reset color
    }

    /**
     * Get a 1x1 white pixel texture
     * Create this once and cache it, or use any white texture you have
     */
    private com.badlogic.gdx.graphics.Texture getWhitePixel() {
        // This is a simplified version - in production, cache this texture
        // For now, use a placeholder that draws a small circle
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(4, 4,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fillCircle(2, 2, 2);

        com.badlogic.gdx.graphics.Texture texture = new com.badlogic.gdx.graphics.Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void dispose() {
        particles.clear();
    }
}
