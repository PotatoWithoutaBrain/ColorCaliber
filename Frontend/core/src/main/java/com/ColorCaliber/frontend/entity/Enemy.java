package com.ColorCaliber.frontend.entity;

import com.ColorCaliber.frontend.pool.BulletPool;
import com.ColorCaliber.frontend.pool.Poolable;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Pooled Enemy entity for ColorCaliber bullet-hell encounters.
 * Rendered as a colored rectangle with a health bar above it.
 */
public class Enemy extends Entity implements Poolable {

    public enum EnemyType {
        SHOOTER,
        SPINNER,
        DASHER,
        BOSS
    }

    private static final Color ENEMY_BULLET_RED = new Color(0.95f, 0.2f, 0.25f, 1f);

    private EnemyType type;
    private float hp;
    private float maxHp;
    private float speed;
    private float shootTimer;
    private float shootInterval;
    private float spiralAngleRad;
    private float hitFlashTimer;
    private float strafeDir;
    private float strafeTimer;
    private boolean active;

    private float arenaWidth = 1200f;
    private float arenaHeight = 800f;

    public Enemy() {
        super(0, 0, 32f, 32f, new Color(0.92f, 0.18f, 0.22f, 1f));
        this.active = false;
    }

    public void init(EnemyType type, float spawnX, float spawnY, int roundNumber,
                     float arenaWidth, float arenaHeight) {
        this.type = type;
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.spiralAngleRad = 0f;
        this.hitFlashTimer = 0f;
        this.active = true;

        this.width = 32f;
        this.height = 32f;
        this.maxHp = 200f;
        this.speed = 95f;
        this.shootInterval = 2.0f;
        this.strafeDir = MathUtils.randomBoolean() ? 1f : -1f;
        this.strafeTimer = MathUtils.random(1.5f, 3.0f);
        this.color.set(0.92f, 0.18f, 0.22f, 1f); // Red Box

        this.hp = this.maxHp;
        this.shootTimer = MathUtils.random(0.5f, shootInterval);
        this.x = MathUtils.clamp(spawnX, 0f, arenaWidth - width);
        this.y = MathUtils.clamp(spawnY, 0f, arenaHeight - height);
        updateBounds();
    }

    @Override
    public void update(float delta) {
    }

    public void updateAI(float delta, Player player, BulletPool bulletPool) {
        if (!active) return;

        if (hitFlashTimer > 0f) {
            hitFlashTimer -= delta;
        }

        strafeTimer -= delta;
        if (strafeTimer <= 0f) {
            strafeDir = -strafeDir;
            strafeTimer = MathUtils.random(1.5f, 3.0f);
        }

        float dx = player.getCenterX() - getCenterX();
        float dy = player.getCenterY() - getCenterY();
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > 0.001f) {
            float dirX = dx / dist;
            float dirY = dy / dist;
            float perpX = -dirY * strafeDir;
            float perpY = dirX * strafeDir;

            float moveX;
            float moveY;
            if (dist > 200f) {
                moveX = dirX * 0.85f + perpX * 0.35f;
                moveY = dirY * 0.85f + perpY * 0.35f;
            } else if (dist < 120f) {
                moveX = -dirX * 0.65f + perpX * 0.75f;
                moveY = -dirY * 0.65f + perpY * 0.75f;
            } else {
                moveX = perpX;
                moveY = perpY;
            }

            float moveLen = (float) Math.sqrt(moveX * moveX + moveY * moveY);
            if (moveLen > 0.001f) {
                x += (moveX / moveLen) * speed * delta;
                y += (moveY / moveLen) * speed * delta;
            }
        }

        x = MathUtils.clamp(x, 0f, arenaWidth - width);
        y = MathUtils.clamp(y, 0f, arenaHeight - height);
        updateBounds();

        shootTimer -= delta;
        if (shootTimer <= 0f) {
            firePattern(player, bulletPool);
            shootTimer = shootInterval;
        }
    }

    private void firePattern(Player player, BulletPool bulletPool) {
        float cx = getCenterX();
        float cy = getCenterY();
        float angleToPlayer = MathUtils.atan2(player.getCenterY() - cy, player.getCenterX() - cx);
        bulletPool.spawnBullet(cx, cy, 10f, angleToPlayer, 220f, 20f,
            false, ENEMY_BULLET_RED, arenaWidth, arenaHeight);
    }

    public void takeDamage(float damage) {
        if (!active) return;
        hp -= damage;
        hitFlashTimer = 0.1f;
        if (hp <= 0f) {
            hp = 0f;
            active = false;
        }
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        if (!active) return;

        // Pure Red Box Enemy (no UI / health bar)
        if (hitFlashTimer > 0f) {
            shapeRenderer.setColor(1f, 0.6f, 0.6f, 1f);
        } else {
            shapeRenderer.setColor(0.92f, 0.18f, 0.22f, 1f);
        }
        shapeRenderer.rect(x, y, width, height);

        // Inner red shade for crisp box look
        shapeRenderer.setColor(0.65f, 0.1f, 0.14f, 1f);
        shapeRenderer.rect(x + 3f, y + 3f, width - 6f, height - 6f);

        // Health bar above enemy
        float barWidth = width + 8f;
        float barHeight = 5f;
        float barX = x - 4f;
        float barY = y + height + 6f;
        float hpRatio = maxHp > 0f ? MathUtils.clamp(hp / maxHp, 0f, 1f) : 0f;

        shapeRenderer.setColor(0.12f, 0.12f, 0.15f, 0.9f);
        shapeRenderer.rect(barX - 1f, barY - 1f, barWidth + 2f, barHeight + 2f);

        shapeRenderer.setColor(0.3f, 0.08f, 0.08f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        shapeRenderer.setColor(1f - hpRatio * 0.65f, 0.2f + hpRatio * 0.7f, 0.2f, 1f);
        shapeRenderer.rect(barX, barY, barWidth * hpRatio, barHeight);
    }

    @Override
    public void reset() {
        this.x = -200f;
        this.y = -200f;
        this.hp = 0f;
        this.maxHp = 1f;
        this.shootTimer = 0f;
        this.hitFlashTimer = 0f;
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

    public EnemyType getType() {
        return type;
    }
}
