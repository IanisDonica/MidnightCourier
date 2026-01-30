package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.entity.Player;

/**
 * Particle system that renders drifting dust behind the player.
 */
public class DriftParticleSystem extends Actor {

    /**
     * Simple particle model for drift effects.
     */
    private static class Particle {
        /** Current position. */
        float x, y;
        /** Velocity components. */
        float vx, vy;
        /** Remaining lifetime. */
        float life;
        /** Initial lifetime used for alpha fade. */
        float maxLife;

        /**
         * Creates a particle instance.
         *
         * @param x start x position
         * @param y start y position
         * @param vx x velocity
         * @param vy y velocity
         * @param life lifetime in seconds
         */
        Particle(float x, float y, float vx, float vy, float life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
        }

        /**
         * Advances particle simulation by delta time.
         *
         * @param delta time step in seconds
         */
        void update(float delta) {
            x += vx * delta;
            y += vy * delta;
            life -= delta;
        }

        /**
         * Checks whether the particle has expired.
         *
         * @return {@code true} if life is depleted
         */
        boolean isDead() {
            return life <= 0;
        }

        /**
         * Returns current alpha based on remaining lifetime.
         *
         * @return alpha multiplier
         */
        float getAlpha() {
            return (life / maxLife) * 0.7f;
        }
    }

    /** Active particles. */
    private Array<Particle> particles;
    /** Player used as the emitter source. */
    private Player player;
    /** Time until the next spawn attempt. */
    private float spawnCooldown = 0;
    /** Seconds between spawn attempts. */
    private static final float SPAWN_INTERVAL = 0.04f;
    /** Lifetime of each particle in seconds. */
    private static final float PARTICLE_LIFE = 0.6f;
    /** Render size of each particle. */
    private static final float PARTICLE_SIZE = 0.15f;

    /**
     * Creates a drift particle system for a player.
     *
     * @param player player to follow
     */
    public DriftParticleSystem(Player player) {
        this.player = player;
        this.particles = new Array<>();
    }

    /**
     * Updates particles and spawns new ones while drifting.
     *
     * @param delta time step in seconds
     */
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

    /**
     * Determines whether the player is drifting based on velocity vs. facing angle.
     *
     * @return {@code true} if the player is drifting
     */
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

    /**
     * Spawns a small burst of particles behind the player.
     */
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

    /**
     * Draws the particle system.
     *
     * @param batch sprite batch used for drawing
     * @param parentAlpha parent alpha multiplier
     */
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
    /**
     * Builds a small white texture used for particle drawing.
     *
     * @return a new white texture instance
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

    /**
     * Clears all active particles.
     */
    public void dispose() {
        particles.clear();
    }
}
