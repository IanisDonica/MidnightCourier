package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;

/**
 * Free-direction force-based movement controller - EXTREME INERTIA EDITION.
 * - Up/Down/Left/Right: apply force in that world direction
 * - Player faces direction of INPUT FORCE (not movement direction)
 * - Smooth rotation when changing input direction
 * - EXTREME INERTIA: Takes ~2 seconds to reverse direction from full speed
 * - Sprint: boost force strength
 *
 * Physics: friction = 0.97 means only 3% energy loss per frame
 * At 60 FPS, takes approximately 2 seconds to stop from full speed
 */
public class DriftyMovementController {

    private float acceleration = 2f;       // Force strength applied per frame (very low = heavy)
    private float maxSpeed = 3f;           // Speed cap
    private float friction = 0.999f;         // Momentum retention (0.97 = 3% loss per frame = EXTREME drift)
    private float rotationSpeed = 200f;     // Degrees per second for smooth rotation to input direction

    public Vector2 velocity = new Vector2();
    private float orientation = 90f;        // Current facing direction in degrees
    private float targetOrientation = 90f;  // Target rotation direction (direction of input force)

    public DriftyMovementController() {
        this.velocity.setZero();
        this.orientation = 90f;
        this.targetOrientation = 90f;
    }

    /**
     * Update movement each frame
     */
    public void update(float deltaTime, boolean moveUp, boolean moveDown,
                       boolean moveLeft, boolean moveRight, boolean sprinting) {

        // Apply forces in absolute world directions (not orientation-based)
        applyForces(deltaTime, moveUp, moveDown, moveLeft, moveRight, sprinting);

        // Update target orientation based on input
        updateTargetOrientation(moveUp, moveDown, moveLeft, moveRight);

        // Smoothly rotate toward target orientation
        updateOrientation(deltaTime);

        // Apply friction/inertia - momentum naturally decays
        applyFriction();
    }

    /**
     * Update target orientation based on input direction
     * This is where the player WANTS to face based on input
     */
    private void updateTargetOrientation(boolean moveUp, boolean moveDown,
                                         boolean moveLeft, boolean moveRight) {
        if (moveUp || moveDown || moveLeft || moveRight) {
            float inputX = 0;
            float inputY = 0;

            if (moveUp) inputY += 1;
            if (moveDown) inputY -= 1;
            if (moveLeft) inputX -= 1;
            if (moveRight) inputX += 1;

            // Calculate angle of input direction
            if (inputX != 0 || inputY != 0) {
                targetOrientation = new Vector2(inputX, inputY).angleDeg();
            }
        }
    }

    /**
     * Smoothly rotate current orientation toward target orientation
     * Takes the shortest path (handles 180-degree reversals through intermediate angles)
     */
    private void updateOrientation(float deltaTime) {
        float rotationAmount = rotationSpeed * deltaTime;

        // Calculate shortest rotation path
        float angleDiff = targetOrientation - orientation;

        // Normalize to -180 to 180 range
        while (angleDiff > 180) angleDiff -= 360;
        while (angleDiff < -180) angleDiff += 360;

        // Rotate toward target, but don't overshoot
        if (Math.abs(angleDiff) > rotationAmount) {
            orientation += Math.signum(angleDiff) * rotationAmount;
        } else {
            orientation = targetOrientation;
        }

        // Keep orientation in 0-360 range
        orientation = (orientation % 360f + 360f) % 360f;
    }

    /**
     * Apply forces in absolute world directions (up, down, left, right)
     * Keys directly add force in world space, not based on facing direction
     */
    private void applyForces(float deltaTime, boolean moveUp, boolean moveDown,
                             boolean moveLeft, boolean moveRight, boolean sprinting) {
        float forceStrength = acceleration;
        if (sprinting) {
            forceStrength *= 1.5f;
        }

        // Apply forces in absolute world directions
        if (moveUp) {
            velocity.y += forceStrength * deltaTime;  // Force upward (+Y)
        }
        if (moveDown) {
            velocity.y -= forceStrength * deltaTime;  // Force downward (-Y)
        }
        if (moveRight) {
            velocity.x += forceStrength * deltaTime;  // Force right (+X)
        }
        if (moveLeft) {
            velocity.x -= forceStrength * deltaTime;  // Force left (-X)
        }

        // Cap speed at maxSpeed
        float maxSpeedActual = maxSpeed;
        if (sprinting) {
            maxSpeedActual *= 1.5f;
        }
        if (velocity.len() > maxSpeedActual) {
            velocity.setLength(maxSpeedActual);
        }
    }

    /**
     * Apply friction/inertia - momentum naturally decays over time
     * EXTREME INERTIA: 0.97 friction means only 3% energy loss per frame
     * At full speed, takes ~2 seconds to completely stop
     */
    private void applyFriction() {
        if (velocity.len() < 0.01f) {
            velocity.setZero();  // Stop if speed is negligible
        } else {
            velocity.scl(friction);  // Apply friction decay - very high friction = extreme drift
        }
    }

    /**
     * Get unit vector in facing direction
     * 90 degrees = up (+Y), 0 degrees = right (+X), 270 degrees = down (-Y), 180 degrees = left (-X)
     */
    private Vector2 getDirectionVector() {
        float radians = orientation * MathUtils.degreesToRadians;
        return new Vector2(MathUtils.cos(radians), MathUtils.sin(radians));
    }

    // ============ GETTERS ============

    public Vector2 getVelocity() {
        return velocity.cpy();
    }

    public float getOrientation() {
        return orientation;
    }

    public float getInputAngle() {
        return targetOrientation;
    }

    public float getSpeed() {
        return velocity.len();
    }

    // ============ SETTERS ============

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void reset() {
        velocity.setZero();
        orientation = 90f;
        targetOrientation = 90f;
    }
}
