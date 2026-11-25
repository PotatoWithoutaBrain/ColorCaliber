package com.ColorCaliber.frontend.weapon;

import com.ColorCaliber.frontend.pool.BulletPool;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * Upgrade Weapon Option 2 ("SMG"):
 * Fires automatically and rapidly while holding the trigger,
 * but each individual bullet deals reduced damage.
 */
public class SmgWeapon extends Weapon {
    private static final Color BULLET_COLOR = new Color(0.4f, 1f, 0.35f, 1f);
    private static final float MAX_JITTER_RAD = (float) Math.toRadians(4.0);

    public SmgWeapon() {
        // Rapid automatic fire (0.07s between shots), reduced damage per bullet (10 vs 25)
        super("Rapid SMG", 0.07f, 10f, 640f);
    }

    @Override
    public void tryFire(float startX, float startY, float angleRad,
                        boolean isShootJustPressed, boolean isShootHeld,
                        BulletPool bulletPool, float arenaWidth, float arenaHeight) {
        if (isShootHeld && cooldownTimer <= 0f) {
            float jitter = MathUtils.random(-MAX_JITTER_RAD, MAX_JITTER_RAD);
            bulletPool.spawnBullet(
                startX, startY, 8f,
                angleRad + jitter, bulletSpeed, baseDamage,
                true, BULLET_COLOR, arenaWidth, arenaHeight
            );
            cooldownTimer = fireDelay;
        }
    }
}
