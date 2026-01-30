package de.tum.cit.fop.maze.entity;

/**
 * Causes of player death.
 */
public enum DeathCause {
    ARRESTED,
    BMW, // when the player is ran over by the BMW
    BMW_EXPLOSION,
    POTHOLE, // Pothole
    TIMEOUT // Timeout in Endless
}
