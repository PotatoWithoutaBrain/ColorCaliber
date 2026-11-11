package com.ColorCaliber.frontend.pool;

/**
 * Interface for objects managed by an {@link ObjectPool}.
 * Part of the Object Pool Design Pattern.
 */
public interface Poolable {
    /**
     * Resets the object state before returning it to the pool or reusing it.
     */
    void reset();

    /**
     * Returns whether this object is currently active in the game world.
     */
    boolean isActive();

    /**
     * Sets whether this object is active in the game world.
     */
    void setActive(boolean active);
}
