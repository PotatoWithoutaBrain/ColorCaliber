package com.ColorCaliber.frontend.entity;

import com.ColorCaliber.frontend.pool.BulletPool;
import com.ColorCaliber.frontend.weapon.ShotgunWeapon;
import com.ColorCaliber.frontend.weapon.SimpleWeapon;
import com.ColorCaliber.frontend.weapon.SmgWeapon;
import com.ColorCaliber.frontend.weapon.Weapon;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Represents the user-controlled Player in ColorCaliber.
 * Rendered as a colored square with an aim indicator and health bar.
 */
public class Player extends Entity {
    private static final float DEFAULT_SIZE = 28f;
    private static final float BASE_SPEED = 240f;

    private float maxHp;
    private float hp;
    private float speedMultiplier;
    private float aimAngleRad;

    private float moveDirX;
    private float moveDirY;
    private boolean shootJustPressed;
    private boolean shootHeld;

    private float hitFlashTimer;
    private float redGridDamageTimer;

    private Weapon weapon;
    private boolean weaponUpgraded;

    private final float arenaWidth;
    private final float arenaHeight;

    public Player(float startX, float startY, float arenaWidth, float arenaHeight) {
        super(startX, startY, DEFAULT_SIZE, DEFAULT_SIZE, new Color(0.2f, 0.9f, 0.35f, 1f));
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.maxHp = 100f;
        this.hp = maxHp;
        this.speedMultiplier = 1f;
        this.aimAngleRad = MathUtils.PI * 0.5f; // facing straight up by default
        this.weapon = new SimpleWeapon();
        this.weaponUpgraded = false;
    }

    public void reset(float startX, float startY) {
        setPosition(startX, startY);
        this.hp = maxHp;
        this.speedMultiplier = 1f;
        this.hitFlashTimer = 0f;
        this.redGridDamageTimer = 0f;
        this.aimAngleRad = MathUtils.PI * 0.5f;
        this.weapon = new SimpleWeapon();
        this.weaponUpgraded = false;
    }

    public void setInput(float moveDirX, float moveDirY, float targetX, float targetY,
                         boolean shootJustPressed, boolean shootHeld) {
        this.moveDirX = 0f;
        this.moveDirY = 0f;
        this.aimAngleRad = MathUtils.PI * 0.5f; // Always shoots straight up
        this.shootJustPressed = shootJustPressed;
        this.shootHeld = shootHeld;
    }

    public void setInput(boolean shootJustPressed, boolean shootHeld) {
        this.moveDirX = 0f;
        this.moveDirY = 0f;
        this.aimAngleRad = MathUtils.PI * 0.5f; // Always shoots straight up
        this.shootJustPressed = shootJustPressed;
        this.shootHeld = shootHeld;
    }

    @Override
    public void update(float delta) {
        // Player is not movable yet - stationary green box
        x = MathUtils.clamp(x, 0f, arenaWidth - width);
        y = MathUtils.clamp(y, 0f, arenaHeight - height);
        updateBounds();

        if (hitFlashTimer > 0f) {
            hitFlashTimer -= delta;
        }
        if (redGridDamageTimer > 0f) {
            redGridDamageTimer -= delta;
        }

        weapon.update(delta);
    }

    public void handleShooting(BulletPool bulletPool) {
        float gunTipX = getCenterX() + MathUtils.cos(aimAngleRad) * (width * 0.6f);
        float gunTipY = getCenterY() + MathUtils.sin(aimAngleRad) * (height * 0.6f);
        weapon.tryFire(gunTipX, gunTipY, aimAngleRad, shootJustPressed, shootHeld,
            bulletPool, arenaWidth, arenaHeight);
    }

    public void takeDamage(float amount) {
        if (amount <= 0f) return;
        hp -= amount;
        hitFlashTimer = 0.15f;
        if (hp <= 0f) {
            hp = maxHp; // Automatically replenish so game keeps running
        }
    }

    public void applyRedGridDamage(float damagePerTick) {
        if (redGridDamageTimer <= 0f) {
            takeDamage(damagePerTick);
            redGridDamageTimer = 0.45f;
        }
    }

    public void heal(float amount) {
        if (amount <= 0f) return;
        hp = Math.min(maxHp, hp + amount);
    }

    public void chooseShotgun() {
    }

    public void chooseSmg() {
    }

    public void upgradeCurrentWeaponLevel() {
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        // Pure Green Box Player (no UI, no aiming indicator)
        if (hitFlashTimer > 0f) {
            shapeRenderer.setColor(1f, 0.4f, 0.4f, 1f);
        } else {
            shapeRenderer.setColor(0.12f, 0.75f, 0.3f, 1f);
        }
        shapeRenderer.rect(x, y, width, height);

        // Inner green box
        shapeRenderer.setColor(0.25f, 0.95f, 0.45f, 1f);
        shapeRenderer.rect(x + 4f, y + 4f, width - 8f, height - 8f);
    }

    public boolean isDead() {
        return false; // Never triggers game over, keeps running
    }

    public float getHp() {
        return hp;
    }

    public float getMaxHp() {
        return maxHp;
    }

    public void setSpeedMultiplier(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public boolean isWeaponUpgraded() {
        return weaponUpgraded;
    }
}
