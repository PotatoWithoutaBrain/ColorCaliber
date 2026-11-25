package com.ColorCaliber.frontend;

import com.ColorCaliber.frontend.animation.DirectionalAnimation;
import com.ColorCaliber.frontend.animation.DirectionalAnimation.Direction;
import com.ColorCaliber.frontend.grid.ColourGridArena;
import com.ColorCaliber.frontend.grid.GridCell;
import com.ColorCaliber.frontend.grid.GridType;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Dedicated visual testing harness for the Isometric Colour Grid Stage,
 * 8-way walking animations (including Up-Left on Row 5, Up-Right on Row 4),
 * Idle animation (when facing down), and custom PNG crosshair aiming.
 *
 * Controls:
 * - W / A / S / D or Arrow Keys: Walk across the isometric stage
 * - Stationary: Faces crosshair direction; plays Idle animation when facing Down
 * - Left Click: Shoot bullets directly towards the mouse cursor
 * - Left Shift: Sprint (1.8x movement speed)
 * - 1..6: Force directional walk test (1=L, 2=R, 3=Down, 4=Up, 5=Up-Left, 6=Up-Right)
 * - 7: Force Idle animation test
 * - 0: Clear forced direction / return to dynamic control
 * - [ / ]: Adjust walk animation speed (slower / faster)
 * - - / +: Adjust sprite scale
 * - R: Reset position to screen center
 */
public class WalkingAnimationTest extends ApplicationAdapter {
    public static final float SCREEN_WIDTH = 1200f;
    public static final float SCREEN_HEIGHT = 800f;
    private static final float CROSSHAIR_SIZE = 32f;

    private static class TestBullet {
        float x, y, vx, vy;
        float life = 2.0f;

        TestBullet(float x, float y, float angleRad, float speed) {
            this.x = x;
            this.y = y;
            this.vx = MathUtils.cos(angleRad) * speed;
            this.vy = MathUtils.sin(angleRad) * speed;
        }
    }

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private Texture crosshairTexture;
    private ColourGridArena arena;

    private DirectionalAnimation animation;
    private float posX;
    private float posY;
    private float baseSpeed = 240f;
    private float spriteScale = 1.5f;

    // Dodge Roll state & parameters
    public static final float ROLL_DISTANCE_TILES = 1.5f;
    public static final float ROLL_DURATION = 0.26f;
    public static final float ROLL_COOLDOWN = 0.10f;

    private boolean isDodgeRolling = false;
    private float rollTimer = 0f;
    private float rollCooldownTimer = 0f;
    private float rollStartX = 0f;
    private float rollStartY = 0f;
    private float rollTargetX = 0f;
    private float rollTargetY = 0f;
    private float rollDirX = 0f;
    private float rollDirY = 0f;

    // Ghost trail for dodge roll placeholder visuals
    private static final int TRAIL_COUNT = 3;
    private final float[] trailX = new float[TRAIL_COUNT];
    private final float[] trailY = new float[TRAIL_COUNT];
    private final float[] trailAlpha = new float[TRAIL_COUNT];

    private final Vector3 mousePos = new Vector3();
    private final List<TestBullet> bullets = new ArrayList<TestBullet>();
    private float shootCooldown = 0f;

    private Direction forcedDirection = null;
    private boolean forceIdle = false;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
        viewport.apply();
        camera.position.set(SCREEN_WIDTH * 0.5f, SCREEN_HEIGHT * 0.5f, 0f);
        camera.update();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.15f);

        // Load 7-row sprite sheet (64x64 per frame)
        animation = new DirectionalAnimation("skeleton_walk.png", 64, 64, 0.08f, 0.09f);

        // Load custom crosshair PNG
        if (Gdx.files != null && Gdx.files.internal("crosshair.png").exists()) {
            crosshairTexture = new Texture(Gdx.files.internal("crosshair.png"));
            crosshairTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }

        // Initialize Isometric Colour Grid Arena
        arena = new ColourGridArena(SCREEN_WIDTH, SCREEN_HEIGHT);
        arena.generatePatternForRound(1, SCREEN_WIDTH * 0.5f, SCREEN_HEIGHT * 0.5f);

        posX = SCREEN_WIDTH * 0.5f - 32f * spriteScale;
        posY = SCREEN_HEIGHT * 0.5f - 32f * spriteScale;
    }

    @Override
    public void render() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);

        handleInput(delta);
        updateBullets(delta);
        draw();
    }

    private void handleInput(float delta) {
        // Convert mouse to world coordinates
        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(mousePos);

        // Position Reset
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            posX = SCREEN_WIDTH * 0.5f - 32f * spriteScale;
            posY = SCREEN_HEIGHT * 0.5f - 32f * spriteScale;
        }

        // Forced Direction Overrides
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) { forcedDirection = Direction.LEFT; forceIdle = false; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) { forcedDirection = Direction.RIGHT; forceIdle = false; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) { forcedDirection = Direction.DOWN; forceIdle = false; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) { forcedDirection = Direction.UP; forceIdle = false; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) { forcedDirection = Direction.UP_LEFT; forceIdle = false; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) { forcedDirection = Direction.UP_RIGHT; forceIdle = false; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) { forceIdle = true; forcedDirection = null; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) { forcedDirection = null; forceIdle = false; }

        // Frame Speed tuning
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
            animation.setFrameDuration(animation.getFrameDuration() + 0.01f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
            animation.setFrameDuration(animation.getFrameDuration() - 0.01f);
        }

        // Scale tuning
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            spriteScale = Math.max(0.5f, spriteScale - 0.25f);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS) || Gdx.input.isKeyJustPressed(Input.Keys.PLUS)) {
            spriteScale = Math.min(4.0f, spriteScale + 0.25f);
        }

        // Movement input
        float moveX = 0f;
        float moveY = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveX -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveX += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveY -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) moveY += 1f;

        if (moveX != 0f && moveY != 0f) {
            float invLen = 1f / 1.41421356f;
            moveX *= invLen;
            moveY *= invLen;
        }

        // Dodge roll input: Space triggers 1.5-tile invincible roll
        if (rollCooldownTimer > 0f) {
            rollCooldownTimer -= delta;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isDodgeRolling && rollCooldownTimer <= 0f) {
            startDodgeRoll(moveX, moveY);
        }

        if (isDodgeRolling) {
            rollTimer += delta;
            float progress = Math.min(1f, rollTimer / ROLL_DURATION);
            float ease = 1f - (1f - progress) * (1f - progress);
            posX = MathUtils.lerp(rollStartX, rollTargetX, ease);
            posY = MathUtils.lerp(rollStartY, rollTargetY, ease);

            int trailIdx = Math.min(TRAIL_COUNT - 1, (int)(progress * TRAIL_COUNT));
            trailX[trailIdx] = posX;
            trailY[trailIdx] = posY;
            trailAlpha[trailIdx] = 0.45f * (1f - progress);

            if (progress >= 1f) {
                posX = rollTargetX;
                posY = rollTargetY;
                isDodgeRolling = false;
                rollCooldownTimer = ROLL_COOLDOWN;
            }
        } else {
            float currentSpeed = baseSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                currentSpeed *= 1.8f;
            }
            posX += moveX * currentSpeed * delta;
            posY += moveY * currentSpeed * delta;
        }

        // Clamp to isometric stage boundary
        float spriteSize = 64f * spriteScale;
        float charCenterX = posX + spriteSize * 0.5f;
        float charFeetY = posY + 6f;

        float relX = charCenterX - arena.getOriginX();
        float relY = charFeetY - arena.getOriginY();
        float colF = (relY / 16f + relX / 32f) * 0.5f;
        float rowF = (relY / 16f - relX / 32f) * 0.5f;

        float margin = 0.35f;
        float clampedCol = MathUtils.clamp(colF, margin, arena.getCols() - 1f - margin);
        float clampedRow = MathUtils.clamp(rowF, margin, arena.getRows() - 1f - margin);

        if (clampedCol != colF || clampedRow != rowF) {
            float newRelX = (clampedCol - clampedRow) * 32f;
            float newRelY = (clampedCol + clampedRow) * 16f;
            posX = arena.getOriginX() + newRelX - spriteSize * 0.5f;
            posY = arena.getOriginY() + newRelY - 6f;
        }

        charCenterX = posX + spriteSize * 0.5f;
        float charCenterY = posY + spriteSize * 0.5f;
        float aimDx = mousePos.x - charCenterX;
        float aimDy = mousePos.y - charCenterY;

        // Update animation
        if (isDodgeRolling) {
            animation.update(delta * 2.8f, rollDirX, rollDirY, rollDirX, rollDirY);
        } else if (forceIdle) {
            animation.setFacing(Direction.DOWN);
            animation.setMoving(false);
            animation.setIdleStateTime(animation.getIdleStateTime() + delta);
        } else if (forcedDirection != null) {
            animation.setFacing(forcedDirection);
            animation.setMoving(true);
            animation.setWalkStateTime(animation.getWalkStateTime() + delta);
        } else {
            animation.update(delta, moveX, moveY, aimDx, aimDy);
        }

        // Shooting towards cursor (Left Click)
        if (shootCooldown > 0f) {
            shootCooldown -= delta;
        }
        boolean shoot = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        if (shoot && shootCooldown <= 0f) {
            float aimAngle = MathUtils.atan2(mousePos.y - charCenterY, mousePos.x - charCenterX);
            float spawnX = charCenterX + MathUtils.cos(aimAngle) * (spriteSize * 0.4f);
            float spawnY = charCenterY + MathUtils.sin(aimAngle) * (spriteSize * 0.4f);
            bullets.add(new TestBullet(spawnX, spawnY, aimAngle, 620f));
            shootCooldown = 0.12f;
        }
    }

    private void startDodgeRoll(float moveX, float moveY) {
        if (isDodgeRolling || rollCooldownTimer > 0f) return;
        this.isDodgeRolling = true;
        this.rollTimer = 0f;
        this.rollStartX = posX;
        this.rollStartY = posY;

        float dx = 0f;
        float dy = 0f;

        boolean isMoving = (Math.abs(moveX) > 0.05f || Math.abs(moveY) > 0.05f);
        if (isMoving) {
            boolean isRight = moveX > 0.2f;
            boolean isLeft = moveX < -0.2f;
            boolean isUp = moveY > 0.2f;
            boolean isDown = moveY < -0.2f;

            if (isRight && isUp) {
                dx = 72f; dy = 48f;
                rollDirX = 1f; rollDirY = 1f;
            } else if (isLeft && isUp) {
                dx = -72f; dy = 48f;
                rollDirX = -1f; rollDirY = 1f;
            } else if (isRight && isDown) {
                dx = 72f; dy = -48f;
                rollDirX = 1f; rollDirY = -1f;
            } else if (isLeft && isDown) {
                dx = -72f; dy = -48f;
                rollDirX = -1f; rollDirY = -1f;
            } else if (isRight) {
                dx = 100f; dy = 0f;
                rollDirX = 1f; rollDirY = 0f;
            } else if (isLeft) {
                dx = -100f; dy = 0f;
                rollDirX = -1f; rollDirY = 0f;
            } else if (isUp) {
                dx = 0f; dy = 80f;
                rollDirX = 0f; rollDirY = 1f;
            } else if (isDown) {
                dx = 0f; dy = -80f;
                rollDirX = 0f; rollDirY = -1f;
            }
        } else {
            Direction facing = animation.getFacing();
            switch (facing) {
                case UP_RIGHT:
                    dx = 72f; dy = 48f; rollDirX = 1f; rollDirY = 1f; break;
                case UP_LEFT:
                    dx = -72f; dy = 48f; rollDirX = -1f; rollDirY = 1f; break;
                case RIGHT:
                    dx = 100f; dy = 0f; rollDirX = 1f; rollDirY = 0f; break;
                case LEFT:
                    dx = -100f; dy = 0f; rollDirX = -1f; rollDirY = 0f; break;
                case UP:
                    dx = 0f; dy = 80f; rollDirX = 0f; rollDirY = 1f; break;
                case DOWN:
                default:
                    dx = 0f; dy = -80f; rollDirX = 0f; rollDirY = -1f; break;
            }
        }

        this.rollTargetX = posX + dx;
        this.rollTargetY = posY + dy;

        for (int i = 0; i < TRAIL_COUNT; i++) {
            trailX[i] = posX;
            trailY[i] = posY;
            trailAlpha[i] = 0f;
        }
    }

    private void updateBullets(float delta) {
        Iterator<TestBullet> it = bullets.iterator();
        while (it.hasNext()) {
            TestBullet b = it.next();
            b.x += b.vx * delta;
            b.y += b.vy * delta;
            b.life -= delta;
            if (b.life <= 0f || b.x < 0 || b.x > SCREEN_WIDTH || b.y < 0 || b.y > SCREEN_HEIGHT) {
                it.remove();
            }
        }
    }

    private void draw() {
        ScreenUtils.clear(0.06f, 0.07f, 0.10f, 1f);

        viewport.apply();
        camera.update();

        float spriteSize = 64f * spriteScale;

        // 1. Draw Isometric Stage via ShapeRenderer
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        arena.render(shapeRenderer);

        // Character shadow (stretches along ground during dodge roll) & bullets
        float shadowW = spriteSize * (isDodgeRolling ? 0.65f : 0.50f);
        float shadowH = spriteSize * (isDodgeRolling ? 0.16f : 0.22f);
        shapeRenderer.setColor(0f, 0f, 0f, isDodgeRolling ? 0.35f : 0.42f);
        shapeRenderer.ellipse(
            posX + spriteSize * 0.5f - shadowW * 0.5f,
            posY + 2f - shadowH * 0.5f,
            shadowW,
            shadowH
        );

        // Bullets
        shapeRenderer.setColor(0.25f, 0.98f, 0.45f, 1f);
        for (int i = 0; i < bullets.size(); i++) {
            TestBullet b = bullets.get(i);
            shapeRenderer.circle(b.x, b.y, 4f);
        }
        shapeRenderer.end();

        // 2. Draw Sprite and Crosshair PNG via SpriteBatch
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Ghost trail afterimages during dodge roll
        if (isDodgeRolling) {
            for (int i = 0; i < TRAIL_COUNT; i++) {
                if (trailAlpha[i] > 0.05f) {
                    batch.setColor(0.35f, 0.75f, 1f, trailAlpha[i]);
                    animation.draw(batch, trailX[i], trailY[i], spriteSize, spriteSize);
                }
            }
        }

        // Draw character with squash and invincibility shimmer during roll
        float renderW = spriteSize;
        float renderH = spriteSize;
        float drawPosX = posX;
        float drawPosY = posY;

        if (isDodgeRolling) {
            float progress = Math.min(1f, rollTimer / ROLL_DURATION);
            float squash = MathUtils.sin(progress * MathUtils.PI);
            renderH = spriteSize * (1f - squash * 0.22f);
            renderW = spriteSize * (1f + squash * 0.15f);
            drawPosX = posX + (spriteSize - renderW) * 0.5f;
            batch.setColor(0.70f, 0.90f, 1f, 0.95f);
        } else {
            batch.setColor(Color.WHITE);
        }

        animation.draw(batch, drawPosX, drawPosY, renderW, renderH);
        batch.setColor(Color.WHITE);

        // Render Crosshair PNG at mouse cursor
        if (crosshairTexture != null) {
            batch.draw(
                crosshairTexture,
                mousePos.x - CROSSHAIR_SIZE * 0.5f,
                mousePos.y - CROSSHAIR_SIZE * 0.5f,
                CROSSHAIR_SIZE,
                CROSSHAIR_SIZE
            );
        }

        // 3. Draw Preview strip of 12 frames at bottom
        float stripY = 16f;
        float framePreviewW = 54f;
        float framePreviewH = 54f;
        Animation<TextureRegion> curAnim = animation.getActiveAnimation();
        TextureRegion[] keyFrames = curAnim.getKeyFrames();
        int activeFrameIdx = animation.getCurrentFrameIndex();

        for (int i = 0; i < keyFrames.length; i++) {
            float fx = 280f + i * (framePreviewW + 8f);
            batch.setColor(i == activeFrameIdx ? Color.WHITE : new Color(0.6f, 0.6f, 0.6f, 0.6f));
            batch.draw(keyFrames[i], fx, stripY, framePreviewW, framePreviewH);
        }
        batch.setColor(Color.WHITE);

        // 4. Draw HUD Text
        float charCenterX = posX + spriteSize * 0.5f;
        float charCenterY = posY + spriteSize * 0.5f;
        float aimDeg = MathUtils.atan2(mousePos.y - charCenterY, mousePos.x - charCenterX) * MathUtils.radiansToDegrees;
        if (aimDeg < 0) aimDeg += 360f;

        font.setColor(Color.YELLOW);
        font.draw(batch, "COLORCALIBER - ISOMETRIC STAGE & 8-WAY ANIMATION TESTER", 30f, SCREEN_HEIGHT - 30f);

        font.setColor(Color.WHITE);
        String stateStr;
        if (animation.isMoving()) {
            stateStr = "WALKING (" + animation.getFacing().name() + ")";
        } else {
            if (animation.getFacing() == Direction.DOWN) {
                stateStr = "IDLE (FACING DOWN)";
            } else {
                stateStr = "STATIONARY (FACING " + animation.getFacing().name() + ")";
            }
        }

        // Get grid cell under player
        GridCell cell = arena.getCellAt(charCenterX, posY + 6f);
        String cellStr = cell != null ? cell.getCurrentType().name() + " (" + cell.getCol() + ", " + cell.getRow() + ")" : "OFF STAGE";

        font.draw(batch, String.format("Current Animation: %s %s", stateStr,
            (forceIdle ? "[FORCED IDLE]" : (forcedDirection != null ? "[FORCED KEY]" : "[DYNAMIC WASD + CURSOR]"))), 30f, SCREEN_HEIGHT - 65f);
        font.draw(batch, String.format("Tile Under Feet: %s | Aim: %.1f deg", cellStr, aimDeg), 30f, SCREEN_HEIGHT - 90f);
        font.draw(batch, String.format("Active Frame: %d / 12 | Walk Spd: %.3fs | Idle Spd: %.3fs",
            activeFrameIdx + 1, animation.getFrameDuration(), animation.getIdleFrameDuration()), 30f, SCREEN_HEIGHT - 115f);
        font.draw(batch, String.format("Scale: %.2fx (Size: %.0fx%.0f) | Active Bullets: %d",
            spriteScale, spriteSize, spriteSize, bullets.size()), 30f, SCREEN_HEIGHT - 140f);

        String dodgeInfo;
        if (isDodgeRolling) {
            dodgeInfo = String.format("DODGE ROLLING [1.5 TILES] (INVINCIBLE! i-frames active | Progress: %.0f%%)",
                (rollTimer / ROLL_DURATION) * 100f);
        } else if (rollCooldownTimer > 0f) {
            dodgeInfo = String.format("RECOVERING (%.2fs)", rollCooldownTimer);
        } else {
            dodgeInfo = "READY (Press Space to Dodge Roll 1.5 Tiles with i-frames)";
        }
        font.draw(batch, String.format("Dodge State: %s", dodgeInfo), 30f, SCREEN_HEIGHT - 165f);

        // Controls legend
        font.setColor(Color.CYAN);
        font.draw(batch, "CONTROLS:", 30f, SCREEN_HEIGHT - 195f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "• WASD / Arrows : Walk on Isometric Stage", 30f, SCREEN_HEIGHT - 218f);
        font.draw(batch, "• Stationary : Faces Cursor (Down=Idle, Others=Frame 0)", 30f, SCREEN_HEIGHT - 241f);
        font.draw(batch, "• SPACE : Dodge Roll (1.5 Tiles, Invincible to Bullets & Grids)", 30f, SCREEN_HEIGHT - 264f);
        font.draw(batch, "• Left Click : Shoot towards Cursor", 30f, SCREEN_HEIGHT - 287f);
        font.draw(batch, "• Shift : Sprint", 30f, SCREEN_HEIGHT - 310f);
        font.draw(batch, "• 1-6 : Force Walk (1=L, 2=R, 3=Down, 4=Up, 5=Up-Left, 6=Up-Right)", 30f, SCREEN_HEIGHT - 333f);
        font.draw(batch, "• 7 : Force Idle | 0 : Dynamic Mode", 30f, SCREEN_HEIGHT - 356f);
        font.draw(batch, "• [ / ] : Walk Speed (- / +) | - / + : Scale", 30f, SCREEN_HEIGHT - 379f);
        font.draw(batch, "• R : Reset Position", 30f, SCREEN_HEIGHT - 402f);

        font.setColor(Color.GOLD);
        font.draw(batch, animation.isMoving() ? "Active Walk Cycle:" : (animation.getFacing() == Direction.DOWN ? "Active Idle Cycle:" : "Stationary Pose:"), 80f, 50f);

        batch.end();

        // 5. Highlight box around active frame in the strip
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.YELLOW);
        float activeFx = 280f + activeFrameIdx * (framePreviewW + 8f);
        shapeRenderer.rect(activeFx - 2f, stripY - 2f, framePreviewW + 4f, framePreviewH + 4f);
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        if (crosshairTexture != null) {
            crosshairTexture.dispose();
        }
        if (animation != null) {
            animation.dispose();
        }
    }
}
