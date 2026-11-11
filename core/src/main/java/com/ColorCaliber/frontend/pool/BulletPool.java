package com.ColorCaliber.frontend.pool;

import com.ColorCaliber.frontend.entity.Bullet;
import com.badlogic.gdx.graphics.Color;

/**
 * Concrete Object Pool for {@link Bullet} instances.
 */
public class BulletPool extends ObjectPool<Bullet> {

    public BulletPool() {
        super(150, 1000);
    }

    @Override
    protected Bullet createObject() {
        return new Bullet();
    }

    /**
     * Convenience helper to spawn a bullet at a given angle in radians.
     */
    public Bullet spawnBullet(float startX, float startY, float size,
                              float angleRad, float speed, float damage,
                              boolean fromPlayer, Color color,
                              float arenaWidth, float arenaHeight) {
        Bullet bullet = obtain();
        float vx = (float) Math.cos(angleRad) * speed;
        float vy = (float) Math.sin(angleRad) * speed;
        bullet.init(startX, startY, size, vx, vy, damage, fromPlayer, color, arenaWidth, arenaHeight);
        return bullet;
    }
}
