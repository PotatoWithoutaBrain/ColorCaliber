package com.ColorCaliber.frontend.weapon;

import com.ColorCaliber.frontend.pool.BulletPool;

/**
 * Abstract base class representing a player weapon.
 * Supports cooldown timing, upgrade levels, and different firing behaviors.
 */
public abstract class Weapon {
    protected final String name;
    protected float fireDelay;
    protected float cooldownTimer;
    protected float baseDamage;
    protected float bulletSpeed;
    protected int level;

    public Weapon(String name, float fireDelay, float baseDamage, float bulletSpeed) {
        this.name = name;
        this.fireDelay = fireDelay;
        this.baseDamage = baseDamage;
        this.bulletSpeed = bulletSpeed;
        this.cooldownTimer = 0f;
        this.level = 1;
    }

    public void update(float delta) {
        if (cooldownTimer > 0f) {
            cooldownTimer -= delta;
        }
    }

    /**
     * Attempts to fire the weapon toward the target angle.
     *
     * @param startX             Player center X
     * @param startY             Player center Y
     * @param angleRad           Aim angle in radians
     * @param isShootJustPressed True only on the frame the shoot input is pressed
     * @param isShootHeld        True while the shoot input is held down
     * @param bulletPool         Object pool used to spawn bullets
     * @param arenaWidth         Width of the arena
     * @param arenaHeight        Height of the arena
     */
    public abstract void tryFire(float startX, float startY, float angleRad,
                                 boolean isShootJustPressed, boolean isShootHeld,
                                 BulletPool bulletPool, float arenaWidth, float arenaHeight);

    public void upgradeLevel() {
        level++;
        // Each additional level increases damage by 15%
        baseDamage *= 1.15f;
    }

    public String getName() {
        return level > 1 ? name + " Lv." + level : name;
    }

    public float getFireDelay() {
        return fireDelay;
    }

    public float getDamage() {
        return baseDamage;
    }

    public int getLevel() {
        return level;
    }
}
