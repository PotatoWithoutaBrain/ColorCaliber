package com.ColorCaliber.frontend.entity;

import com.ColorCaliber.frontend.pool.Poolable;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Pooled bullet entity used for both player weapons and enemy bullet-hell patterns.
 * Rendered as a colored rectangle.
 */
public class Bullet extends Entity implements Poolable {
    private float vx;
    private float vy;
    private float damage;
    private boolean fromPlayer;
    private boolean active;

    private float arenaWidth = 1200f;
    private float arenaHeight = 720f;

    public Bullet() {
        super(0, 0, 10, 10, Color.YELLOW);
        this.active = false;
    }

    public void init(float centerX, float centerY, float size, float vx, float vy,
                     float damage, boolean fromPlayer, Color bulletColor,
                     float arenaWidth, float arenaHeight) {
        this.width = size;
        this.height = size;
        this.x = centerX - size * 0.5f;
        this.y = centerY - size * 0.5f;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.fromPlayer = fromPlayer;
        this.color.set(bulletColor);
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.active = true;
        updateBounds();
    }

    @Override
    public void update(float delta) {
        if (!active) return;

        x += vx * delta;
        y += vy * delta;
        updateBounds();

        // Deactivate if outside arena boundaries
        if (x + width < 0 || x > arenaWidth || y + height < 0 || y > arenaHeight) {
            active = false;
        }
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        if (!active) return;
        // Outer rectangle
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width, height);

        // Bright inner center for visibility in bullet hell
        shapeRenderer.setColor(1f, 1f, 1f, 0.85f);
        float margin = Math.max(1.5f, width * 0.25f);
        shapeRenderer.rect(x + margin, y + margin, width - margin * 2, height - margin * 2);
    }

    @Override
    public void reset() {
        this.x = -100f;
        this.y = -100f;
        this.vx = 0f;
        this.vy = 0f;
        this.damage = 0f;
        this.fromPlayer = false;
        updateBounds();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public float getDamage() {
        return damage;
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }
}
