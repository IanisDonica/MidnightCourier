package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Movement controller with inertia and smooth orientation changes.
 */
public class DriftyMovementController {
    /**
     * Current velocity vector.
     */
    public Vector2 velocity = new Vector2();
    /**
     * Acceleration strength applied to movement inputs.
     */
    private float acceleration = 2f;
    /**
     * Maximum movement speed.
     */
    private float maxSpeed = 3f;
    /**
     * Friction multiplier applied per frame.
     */
    private float friction = 0.999f; // 0 means maximum friction, 1 is no friction
    /**
     * Degrees per second for smooth rotation to input direction.
     */
    private float rotationSpeed = 200f;     // Degrees per second for smooth rotation to input direction
    /**
     * Current orientation in degrees.
     */
    private float orientation = 90f;
    /**
     * Target orientation in degrees based on input.
     */
    private float targetOrientation = 90f;
    private boolean decelarating = false;

    /**
     * Creates a movement controller with default parameters.
     */
    public DriftyMovementController() {
        this.velocity.setZero();
        this.orientation = 90f;
        this.targetOrientation = 90f;
    }

    /**
     * Update movement each frame
     */
    /**
     * Updates movement, orientation, and friction for the current frame.
     *
     * @param deltaTime frame delta time in seconds
     * @param moveUp    whether moving up
     * @param moveDown  whether moving down
     * @param moveLeft  whether moving left
     * @param moveRight whether moving right
     * @param sprinting whether sprinting is active
     */
    public void update(float deltaTime, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight, boolean sprinting) {
        applyForces(deltaTime, moveUp, moveDown, moveLeft, moveRight, sprinting);
        updateTargetOrientation(moveUp, moveDown, moveLeft, moveRight);
        updateOrientation(deltaTime);
        applyFriction();
    }

    /**
     * Updates the target orientation based on input state.
     *
     * @param moveUp    whether moving up
     * @param moveDown  whether moving down
     * @param moveLeft  whether moving left
     * @param moveRight whether moving right
     */
    private void updateTargetOrientation(boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight) {
        if (moveUp || moveDown || moveLeft || moveRight) {
            float inputX = 0;
            float inputY = 0;

            if (moveUp) inputY += 1;
            if (moveDown) inputY -= 1;
            if (moveLeft) inputX -= 1;
            if (moveRight) inputX += 1;

            if (inputX != 0 || inputY != 0) {
                targetOrientation = new Vector2(inputX, inputY).angleDeg();
            }
        }
    }

    public boolean isDecelerating() {
        return decelarating && velocity.len() != 0;
    }

    /**
     * Gradually rotates the orientation toward the target orientation.
     *
     * @param deltaTime frame delta time in seconds
     */
    private void updateOrientation(float deltaTime) {
        float rotationAmount = rotationSpeed * deltaTime;

        float angleDiff = targetOrientation - orientation;

        while (angleDiff > 180) angleDiff -= 360;
        while (angleDiff < -180) angleDiff += 360;

        if (Math.abs(angleDiff) > rotationAmount) {
            orientation += Math.signum(angleDiff) * rotationAmount;
        } else {
            orientation = targetOrientation;
        }

        orientation = orientation % 360f;
    }

    /**
     * Applies movement forces based on input.
     *
     * @param deltaTime frame delta time in seconds
     * @param moveUp    whether moving up
     * @param moveDown  whether moving down
     * @param moveLeft  whether moving left
     * @param moveRight whether moving right
     * @param sprinting whether sprinting is active
     */
    private void applyForces(float deltaTime, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight, boolean sprinting) {
        float forceStrength = acceleration;
        if (sprinting) {
            forceStrength *= 1.5f;
        }

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

        decelarating = !moveUp && !moveDown && !moveLeft && !moveRight;
        // Cap speed at maxSpeed
        float maxSpeedActual = maxSpeed;
        if (sprinting) {
            maxSpeedActual *= 1.5f;
        }
        if (velocity.len() > maxSpeedActual) {
            velocity.scl(0.9f);
        }
    }

    /**
     * Applies friction to the velocity.
     */
    private void applyFriction() {
        if (velocity.len() < 0.5f) {
            if (decelarating) velocity.setZero();  // Stop if speed is negligible
        } else {
            float currentFriction = friction;

            float movementAngle = velocity.angleDeg();
            float angleDiff = Math.abs(movementAngle - orientation);
            if (angleDiff > 180) {
                angleDiff = 360 - angleDiff;
            }

            if (angleDiff > 35) {
                // Reduce the friction multiplier (meaning more friction)
                // The lower the value, the higher the friction.
                currentFriction = friction * 0.995f;
            }

            velocity.scl(currentFriction);  // Apply friction decay
        }
    }


    /**
     * Computes the forward direction vector from the current orientation.
     *
     * @return unit direction vector
     */
    private Vector2 getDirectionVector() {
        float radians = orientation * MathUtils.degreesToRadians;
        return new Vector2(MathUtils.cos(radians), MathUtils.sin(radians));
    }

    /**
     * Returns a copy of the current velocity.
     *
     * @return velocity copy
     */
    public Vector2 getVelocity() {
        return velocity.cpy();
    }

    /**
     * Returns the current orientation in degrees.
     *
     * @return orientation angle
     */
    public float getOrientation() {
        return orientation;
    }

    /**
     * Returns the target input orientation in degrees.
     *
     * @return target orientation
     */
    public float getInputAngle() {
        return targetOrientation;
    }

    /**
     * Returns the current speed magnitude.
     *
     * @return speed value
     */
    public float getSpeed() {
        return velocity.len();
    }

    /**
     * Sets the acceleration strength.
     *
     * @param acceleration new acceleration
     */
    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * Sets the maximum speed.
     *
     * @param maxSpeed new max speed
     */
    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     * Sets the friction multiplier.
     *
     * @param friction new friction value
     */
    public void setFriction(float friction) {
        this.friction = friction;
    }

    /**
     * Sets the rotation speed in degrees per second.
     *
     * @param rotationSpeed new rotation speed
     */
    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    /**
     * Resets velocity and orientation to defaults.
     */
    public void reset() {
        velocity.setZero();
        orientation = 90f;
        targetOrientation = 90f;
    }
}
