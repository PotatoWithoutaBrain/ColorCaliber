package com.ColorCaliber.frontend.weapon;

import com.ColorCaliber.frontend.pool.BulletPool;
import com.badlogic.gdx.graphics.Color;

/**
 * Initial weapon ("Pistol" / "Simple Blaster"):
 * Fires immediately when the shoot key/button is pressed (semi-automatic)
 * with no delay between shots.
 */
public class SimpleWeapon extends Weapon {
    private static final Color BULLET_COLOR = new Color(0.25f, 0.98f, 0.45f, 1f);

    public SimpleWeapon() {
        super("Straight Blaster", 0.0f, 30f, 580f);
    }

    @Override
    public void tryFire(float startX, float startY, float angleRad,
                        boolean isShootJustPressed, boolean isShootHeld,
                        BulletPool bulletPool, float arenaWidth, float arenaHeight) {
        // Semi-automatic with no delay: fires on every discrete press
        if (isShootJustPressed && cooldownTimer <= 0f) {
            bulletPool.spawnBullet(
                startX, startY, 10f,
                angleRad, bulletSpeed, baseDamage,
                true, BULLET_COLOR, arenaWidth, arenaHeight
            );
            cooldownTimer = fireDelay;
        }
    }
}
