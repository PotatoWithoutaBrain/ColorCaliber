package com.ColorCaliber.frontend.entity;

import com.ColorCaliber.frontend.animation.DirectionalAnimation;
import com.ColorCaliber.frontend.pool.BulletPool;
import com.ColorCaliber.frontend.weapon.SimpleWeapon;
import com.ColorCaliber.frontend.weapon.Weapon;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

/**
 * Represents the user-controlled Player in ColorCaliber.
 * Rendered using an 8-way walking sprite sheet with dedicated idle animation (skeleton_walk.png).
 * Supports cursor-based aiming, directional facing towards crosshair when stationary,
 * and idle animation when facing down.
 */
public class Player extends Entity implements Disposable {
    private static final float DEFAULT_SIZE = 32f;
    private static final float BASE_SPEED = 240f;
    private static final float SPRITE_RENDER_SIZE = 48f;

    private static final float ISO_TILE_WIDTH = 64f;
    private static final float ISO_TILE_HEIGHT = 32f;
    private static final float INPUT_DEADZONE = 0.2f;

    private float maxHp;
    private float hp;
    private float speedMultiplier;
    private float aimAngleRad;

    private float targetX;
    private float targetY;
    private float moveDirX;
    private float moveDirY;
    private boolean shootJustPressed;
    private boolean shootHeld;

    // Dodge Roll state & parameters
    public static final float ROLL_DISTANCE_TILES = 1.5f;
    public static final float ROLL_DURATION = 0.16f;
    public static final float ROLL_COOLDOWN = 0.10f;
    public static final float SCREEN_Y_DASH_MULTIPLIER = 1.35f;

    private final Vector2 dashDir = new Vector2();
    private final Vector2 dashDelta = new Vector2();

    private boolean isDodgeRolling;
    private float rollTimer;
    private float rollCooldownTimer;
    private float rollStartX;
    private float rollStartY;
    private float rollTargetX;
    private float rollTargetY;
    private float rollDirX;
    private float rollDirY;
    private boolean dodgeRollJustPressed;

    // Ghost trail for dodge roll placeholder visuals
    private static final int TRAIL_COUNT = 3;
    private final float[] trailX = new float[TRAIL_COUNT];
    private final float[] trailY = new float[TRAIL_COUNT];
    private final float[] trailAlpha = new float[TRAIL_COUNT];

    private float hitFlashTimer;
    private float redGridDamageTimer;

    private Weapon weapon;
    private boolean weaponUpgraded;

    private final float arenaWidth;
    private final float arenaHeight;

    private DirectionalAnimation directionalAnimation;

    public Player(float startX, float startY, float arenaWidth, float arenaHeight) {
        super(startX, startY, DEFAULT_SIZE, DEFAULT_SIZE, new Color(0.2f, 0.9f, 0.35f, 1f));
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.maxHp = 500f;
        this.hp = maxHp;
        this.speedMultiplier = 1f;
        this.aimAngleRad = MathUtils.PI * 0.5f; // facing straight up by default
        this.targetX = startX;
        this.targetY = startY + 100f;
        this.weapon = new SimpleWeapon();
        this.weaponUpgraded = false;

        initAnimation();
    }

    private void initAnimation() {
        if (Gdx.files != null && Gdx.files.internal("skeleton_walk.png").exists()) {
            try {
                this.directionalAnimation = new DirectionalAnimation("skeleton_walk.png", 64, 64, 0.08f, 0.09f);
            } catch (Exception e) {
                Gdx.app.error("Player", "Could not load skeleton_walk.png: " + e.getMessage());
                this.directionalAnimation = null;
            }
        }
    }

    public void reset(float startX, float startY) {
        setPosition(startX, startY);
        this.hp = maxHp;
        this.speedMultiplier = 1f;
        this.hitFlashTimer = 0f;
        this.redGridDamageTimer = 0f;
        this.moveDirX = 0f;
        this.moveDirY = 0f;
        this.aimAngleRad = MathUtils.PI * 0.5f;
        this.targetX = startX;
        this.targetY = startY + 100f;
        this.weapon = new SimpleWeapon();
        this.weaponUpgraded = false;

        this.isDodgeRolling = false;
        this.rollTimer = 0f;
        this.rollCooldownTimer = 0f;
        this.dodgeRollJustPressed = false;
        this.rollDirX = 0f;
        this.rollDirY = 0f;
        for (int i = 0; i < TRAIL_COUNT; i++) {
            trailAlpha[i] = 0f;
        }

        if (directionalAnimation != null) {
            directionalAnimation.setFacing(DirectionalAnimation.Direction.DOWN);
            directionalAnimation.setMoving(false);
            directionalAnimation.setWalkStateTime(0f);
            directionalAnimation.setIdleStateTime(0f);
        }
    }

    /**
     * Updates movement direction, cursor target position for aiming, shooting triggers,
     * and dodge roll trigger.
     */
    public void setInput(float moveDirX, float moveDirY, float targetX, float targetY,
                         boolean shootJustPressed, boolean shootHeld,
                         boolean dodgeRollJustPressed) {
        this.moveDirX = moveDirX;
        this.moveDirY = moveDirY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.aimAngleRad = MathUtils.atan2(targetY - getCenterY(), targetX - getCenterX());
        this.shootJustPressed = shootJustPressed;
        this.shootHeld = shootHeld;
        this.dodgeRollJustPressed = dodgeRollJustPressed;
    }

    public void setInput(float moveDirX, float moveDirY, float targetX, float targetY,
                         boolean shootJustPressed, boolean shootHeld,
                         boolean spaceJustPressed, boolean spaceHeld) {
        setInput(moveDirX, moveDirY, targetX, targetY, shootJustPressed, shootHeld, spaceJustPressed);
    }

    public void setJumpInput(boolean spaceJustPressed, boolean spaceHeld) {
        this.dodgeRollJustPressed = spaceJustPressed;
    }

    public void setInput(float moveDirX, float moveDirY, float targetX, float targetY,
                         boolean shootJustPressed, boolean shootHeld) {
        setInput(moveDirX, moveDirY, targetX, targetY, shootJustPressed, shootHeld, false);
    }

    public void setInput(float moveDirX, float moveDirY, boolean shootJustPressed, boolean shootHeld) {
        setInput(moveDirX, moveDirY, this.targetX, this.targetY, shootJustPressed, shootHeld, false);
    }

    public void setInput(boolean shootJustPressed, boolean shootHeld) {
        setInput(this.moveDirX, this.moveDirY, this.targetX, this.targetY, shootJustPressed, shootHeld, false);
    }

    /**
     * Initiates a 1.5-tile dodge roll with invincibility frames (i-frames).
     * Roll direction follows current movement (WASD) if moving,
     * or character facing direction (towards crosshair) if stationary.
     */
    public void startDodgeRoll() {
        if (isDodgeRolling || rollCooldownTimer > 0f) return;

        this.isDodgeRolling = true;
        this.rollTimer = 0f;
        this.rollStartX = x;
        this.rollStartY = y;

        int signX = toAxisSign(moveDirX, INPUT_DEADZONE);
        int signY = toAxisSign(moveDirY, INPUT_DEADZONE);

        // Stationary: fall back to current facing direction (towards crosshair)
        if (signX == 0 && signY == 0) {
            DirectionalAnimation.Direction facing = (directionalAnimation != null)
                ? directionalAnimation.getFacing()
                : DirectionalAnimation.Direction.DOWN;

            switch (facing) {
                case UP_RIGHT: signX =  1; signY =  1; break;
                case UP_LEFT:  signX = -1; signY =  1; break;
                case RIGHT:    signX =  1; signY =  0; break;
                case LEFT:     signX = -1; signY =  0; break;
                case UP:       signX =  0; signY =  1; break;
                case DOWN:
                default:       signX =  0; signY = -1; break;
            }
        }

        this.rollDirX = signX;
        this.rollDirY = signY;

        // Normalize 8-way screen input first so all directions start at unit length 1.0f,
        // then apply 1.35f vertical compensation for the 2:1 rotated diamond grid:
        // - Horizontal: 1.5 grid diagonals (96px)
        // - Vertical: ~2.025 grid diagonals (64.8px)
        dashDir.set(signX, signY).nor();
        float horizontalDashDist = ROLL_DISTANCE_TILES * ISO_TILE_WIDTH;
        float verticalDashDist = ROLL_DISTANCE_TILES * ISO_TILE_HEIGHT * SCREEN_Y_DASH_MULTIPLIER;
        dashDelta.set(
            dashDir.x * horizontalDashDist,
            dashDir.y * verticalDashDist
        );

        this.rollTargetX = MathUtils.clamp(x + dashDelta.x, 0f, arenaWidth - width);
        this.rollTargetY = MathUtils.clamp(y + dashDelta.y, 0f, arenaHeight - height);

        // Initialize ghost trail
        for (int i = 0; i < TRAIL_COUNT; i++) {
            trailX[i] = x;
            trailY[i] = y;
            trailAlpha[i] = 0f;
        }
    }

    public void startJump(int tiles) {
        startDodgeRoll();
    }

    public void startJump(float tiles) {
        startDodgeRoll();
    }

    private static int toAxisSign(float value, float deadzone) {
        if (value > deadzone) return 1;
        if (value < -deadzone) return -1;
        return 0;
    }

    @Override
    public void update(float delta) {
        if (rollCooldownTimer > 0f) {
            rollCooldownTimer -= delta;
        }

        // Trigger dodge roll immediately upon Space press
        if (dodgeRollJustPressed && !isDodgeRolling && rollCooldownTimer <= 0f) {
            startDodgeRoll();
        }

        if (isDodgeRolling) {
            rollTimer += delta;
            float progress = Math.min(1f, rollTimer / ROLL_DURATION);

            // Front-loaded cubic ease-out so the burst of speed happens on Frame 1
            float ease = Interpolation.pow3Out.apply(progress);
            x = MathUtils.lerp(rollStartX, rollTargetX, ease);
            y = MathUtils.lerp(rollStartY, rollTargetY, ease);

            // Record ghost trail samples
            int trailIdx = Math.min(TRAIL_COUNT - 1, (int)(progress * TRAIL_COUNT));
            trailX[trailIdx] = x;
            trailY[trailIdx] = y;
            trailAlpha[trailIdx] = 0.45f * (1f - progress);

            if (progress >= 1f) {
                x = rollTargetX;
                y = rollTargetY;
                isDodgeRolling = false;
                rollCooldownTimer = ROLL_COOLDOWN;
            }
        } else {
            // Controllable ground movement
            x += moveDirX * BASE_SPEED * speedMultiplier * delta;
            y += moveDirY * BASE_SPEED * speedMultiplier * delta;
        }

        x = MathUtils.clamp(x, 0f, arenaWidth - width);
        y = MathUtils.clamp(y, 0f, arenaHeight - height);
        updateBounds();

        // Update animation:
        // During dodge roll, animate rapidly in the roll direction (placeholder roll effect)
        // When moving normally, walk in movement direction
        // When stationary, face crosshair cursor direction (idle if down)
        if (directionalAnimation != null) {
            if (isDodgeRolling) {
                directionalAnimation.update(delta * 2.8f, rollDirX, rollDirY, rollDirX, rollDirY);
            } else {
                float aimDx = targetX - getCenterX();
                float aimDy = targetY - getCenterY();
                directionalAnimation.update(delta, moveDirX, moveDirY, aimDx, aimDy);
            }
        }

        if (hitFlashTimer > 0f) {
            hitFlashTimer -= delta;
        }
        if (redGridDamageTimer > 0f) {
            redGridDamageTimer -= delta;
        }

        weapon.update(delta);
    }

    public void handleShooting(BulletPool bulletPool) {
        // Aim directly at cursor position
        this.aimAngleRad = MathUtils.atan2(targetY - getCenterY(), targetX - getCenterX());

        float gunTipX = getCenterX() + MathUtils.cos(aimAngleRad) * (width * 0.6f);
        float gunTipY = getCenterY() + MathUtils.sin(aimAngleRad) * (height * 0.6f);
        weapon.tryFire(gunTipX, gunTipY, aimAngleRad, shootJustPressed, shootHeld,
            bulletPool, arenaWidth, arenaHeight);
    }

    public void takeDamage(float amount) {
        if (amount <= 0f || isDodgeRolling()) return; // Complete invincibility while dodge rolling!
        hp -= amount;
        hitFlashTimer = 0.15f;
        if (hp <= 0f) {
            hp = 0f;
        }
    }

    public void applyRedGridDamage(float damagePerTick) {
        if (isDodgeRolling()) return; // Complete invincibility to grid hazard damage during dodge roll!
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

    /**
     * SpriteBatch rendering for the animated sprite.
     * Incorporates invincibility shimmer, ghost trail afterimages,
     * and subtle roll squash/stretch during dodge roll.
     */
    public void render(SpriteBatch batch) {
        if (directionalAnimation != null) {
            float drawX = x + (width - SPRITE_RENDER_SIZE) * 0.5f;
            float drawY = y;

            // Render ghost trail / afterimages during dodge roll
            if (isDodgeRolling) {
                for (int i = 0; i < TRAIL_COUNT; i++) {
                    if (trailAlpha[i] > 0.05f) {
                        batch.setColor(0.35f, 0.75f, 1f, trailAlpha[i]);
                        float gx = trailX[i] + (width - SPRITE_RENDER_SIZE) * 0.5f;
                        float gy = trailY[i];
                        directionalAnimation.draw(batch, gx, gy, SPRITE_RENDER_SIZE, SPRITE_RENDER_SIZE);
                    }
                }
            }

            // Roll visual silhouette: slight crouch/tumble scale during mid-roll
            float renderW = SPRITE_RENDER_SIZE;
            float renderH = SPRITE_RENDER_SIZE;

            if (isDodgeRolling) {
                float progress = Math.min(1f, rollTimer / ROLL_DURATION);
                float squash = MathUtils.sin(progress * MathUtils.PI);
                renderH = SPRITE_RENDER_SIZE * (1f - squash * 0.22f);
                renderW = SPRITE_RENDER_SIZE * (1f + squash * 0.15f);
                drawX = x + (width - renderW) * 0.5f;
                // Invincibility shimmer color (cyan/white glow)
                batch.setColor(0.70f, 0.90f, 1f, 0.95f);
            } else if (hitFlashTimer > 0f) {
                batch.setColor(1f, 0.4f, 0.4f, 1f);
            } else {
                batch.setColor(Color.WHITE);
            }

            directionalAnimation.draw(batch, drawX, drawY, renderW, renderH);
            batch.setColor(Color.WHITE);
        }
    }

    /**
     * Fallback shape rendering if sprite batch is not active.
     */
    @Override
    public void render(ShapeRenderer shapeRenderer) {
        if (directionalAnimation != null) {
            return; // Already rendered via SpriteBatch
        }

        if (isDodgeRolling) {
            shapeRenderer.setColor(0.3f, 0.8f, 1f, 1f); // Cyan invincibility box
        } else if (hitFlashTimer > 0f) {
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
        return hp <= 0f;
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

    public DirectionalAnimation getDirectionalAnimation() {
        return directionalAnimation;
    }

    public float getAimAngleRad() {
        return aimAngleRad;
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetY() {
        return targetY;
    }

    public boolean isDodgeRolling() {
        return isDodgeRolling;
    }

    public boolean isInvincible() {
        return isDodgeRolling;
    }

    public float getRollProgress() {
        return isDodgeRolling ? Math.min(1f, rollTimer / ROLL_DURATION) : 0f;
    }

    public float getRollCooldownTimer() {
        return rollCooldownTimer;
    }

    // Compatibility stubs
    public boolean isJumping() {
        return false;
    }

    public boolean isAirborne() {
        return isDodgeRolling;
    }

    public float getJumpZ() {
        return 0f;
    }

    public float getJumpTiles() {
        return ROLL_DISTANCE_TILES;
    }

    public float getSpaceHoldTimer() {
        return 0f;
    }

    public boolean isSpaceKeyDown() {
        return false;
    }

    public boolean isJumpCancelled() {
        return false;
    }

    @Override
    public void dispose() {
        if (directionalAnimation != null) {
            directionalAnimation.dispose();
            directionalAnimation = null;
        }
    }
}
