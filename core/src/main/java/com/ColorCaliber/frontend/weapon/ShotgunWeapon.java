package com.ColorCaliber.frontend.weapon;

import com.ColorCaliber.frontend.pool.BulletPool;
import com.badlogic.gdx.graphics.Color;

/**
 * Upgrade Weapon Option 1 ("Shotgun"):
 * Fires 9 bullets simultaneously in a wide spread cone (semi-automatic).
 */
public class ShotgunWeapon extends Weapon {
    private static final Color BULLET_COLOR = new Color(1f, 0.65f, 0.1f, 1f);
    private static final int PELLET_COUNT = 9;
    private static final float SPREAD_STEP_RAD = (float) Math.toRadians(7.5); // 60-degree total spread cone

    public ShotgunWeapon() {
        super("Shotgun", 0.35f, 20f, 520f);
    }

    @Override
    public void tryFire(float startX, float startY, float angleRad,
                        boolean isShootJustPressed, boolean isShootHeld,
                        BulletPool bulletPool, float arenaWidth, float arenaHeight) {
        // Semi-automatic: requires pressing trigger for each shot
        if (isShootJustPressed && cooldownTimer <= 0f) {
            // 9 pellets spread symmetrically (-4 to +4 * 7.5 deg)
            int half = PELLET_COUNT / 2;
            for (int i = -half; i <= half; i++) {
                float pelletAngle = angleRad + (i * SPREAD_STEP_RAD);
                bulletPool.spawnBullet(
                    startX, startY, 10f,
                    pelletAngle, bulletSpeed, baseDamage,
                    true, BULLET_COLOR, arenaWidth, arenaHeight
                );
            }
            cooldownTimer = fireDelay;
        }
    }
}
