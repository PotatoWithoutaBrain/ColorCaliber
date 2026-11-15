package com.ColorCaliber.frontend.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

/**
 * Handles 6-way directional facing/walking animations and idle animation.
 *
 * Sprite Sheet layout (64x64 per frame, 768x448 total):
 * - Row 0: Walk Left / Down-Left (12 frames)
 * - Row 1: Walk Right / Down-Right (12 frames)
 * - Row 2: Walk Down (12 frames)
 * - Row 3: Walk Up (12 frames)
 * - Row 4: Walk Up-Right (12 frames)
 * - Row 5: Walk Up-Left (12 frames)
 * - Row 6: Idle animation (12 frames, used when facing Down)
 */
public class DirectionalAnimation implements Disposable {

    public enum Direction {
        LEFT,
        RIGHT,
        DOWN,
        UP,
        UP_LEFT,
        UP_RIGHT
    }

    private final Texture texture;
    private final Animation<TextureRegion> animLeft;
    private final Animation<TextureRegion> animRight;
    private final Animation<TextureRegion> animDown;
    private final Animation<TextureRegion> animUp;
    private final Animation<TextureRegion> animUpRight; // Row 4 (Up-Right)
    private final Animation<TextureRegion> animUpLeft;  // Row 5 (Up-Left)
    private final Animation<TextureRegion> animIdle;    // Row 6 (Idle facing down)

    private Direction facing = Direction.DOWN;
    private boolean isMoving = false;
    private float walkStateTime = 0f;
    private float idleStateTime = 0f;
    private float walkFrameDuration;
    private float idleFrameDuration;

    public DirectionalAnimation(String internalPath, int frameWidth, int frameHeight,
                                float defaultWalkDuration, float defaultIdleDuration) {
        this.walkFrameDuration = defaultWalkDuration;
        this.idleFrameDuration = defaultIdleDuration;
        this.texture = new Texture(Gdx.files.internal(internalPath));
        this.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        TextureRegion[][] grid = TextureRegion.split(texture, frameWidth, frameHeight);

        // Map rows:
        // Row 0 = Left, Row 1 = Right, Row 2 = Down, Row 3 = Up
        // Row 4 = Up-Right, Row 5 = Up-Left
        // Row 6 = Idle
        this.animLeft = new Animation<TextureRegion>(walkFrameDuration, grid[0]);
        this.animRight = new Animation<TextureRegion>(walkFrameDuration, grid[1]);
        this.animDown = new Animation<TextureRegion>(walkFrameDuration, grid[2]);
        this.animUp = new Animation<TextureRegion>(walkFrameDuration, grid[3]);
        this.animUpRight = new Animation<TextureRegion>(walkFrameDuration, grid[4]); // Row 4 is Up-Right
        this.animUpLeft = new Animation<TextureRegion>(walkFrameDuration, grid[5]);  // Row 5 is Up-Left
        this.animIdle = new Animation<TextureRegion>(idleFrameDuration, grid[6]);

        this.animLeft.setPlayMode(Animation.PlayMode.LOOP);
        this.animRight.setPlayMode(Animation.PlayMode.LOOP);
        this.animDown.setPlayMode(Animation.PlayMode.LOOP);
        this.animUp.setPlayMode(Animation.PlayMode.LOOP);
        this.animUpRight.setPlayMode(Animation.PlayMode.LOOP);
        this.animUpLeft.setPlayMode(Animation.PlayMode.LOOP);
        this.animIdle.setPlayMode(Animation.PlayMode.LOOP);
    }

    public DirectionalAnimation(String internalPath, int frameWidth, int frameHeight, float defaultFrameDuration) {
        this(internalPath, frameWidth, frameHeight, defaultFrameDuration, defaultFrameDuration);
    }

    /**
     * Updates movement state, facing direction, and animation timer.
     * The character faces the direction of the crosshair cursor (aimDx, aimDy) across 6 directions:
     * - UP_RIGHT (22.5° to 67.5°)
     * - UP (67.5° to 112.5°)
     * - UP_LEFT (112.5° to 157.5°)
     * - LEFT (157.5° to 225.0°)
     * - DOWN (225.0° to 315.0°)
     * - RIGHT (315.0° to 360° and 0° to 22.5°)
     */
    public void update(float delta, float moveX, float moveY, float aimDx, float aimDy) {
        boolean moving = (Math.abs(moveX) > 0.01f || Math.abs(moveY) > 0.01f);
        this.isMoving = moving;

        if (moving) {
            walkStateTime += delta;
        } else {
            walkStateTime = 0f;
            idleStateTime += delta;
        }

        // Determine facing direction from crosshair offset (or movement if aim vector is zero)
        float dx = aimDx;
        float dy = aimDy;
        if (Math.abs(dx) < 0.001f && Math.abs(dy) < 0.001f) {
            dx = moveX;
            dy = moveY;
        }

        if (Math.abs(dx) > 0.001f || Math.abs(dy) > 0.001f) {
            float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
            if (angle < 0f) {
                angle += 360f;
            }

            // 6-directional sector mapping
            if (angle >= 22.5f && angle < 67.5f) {
                facing = Direction.UP_RIGHT;
            } else if (angle >= 67.5f && angle < 112.5f) {
                facing = Direction.UP;
            } else if (angle >= 112.5f && angle < 157.5f) {
                facing = Direction.UP_LEFT;
            } else if (angle >= 157.5f && angle < 225.0f) {
                facing = Direction.LEFT;
            } else if (angle >= 225.0f && angle < 315.0f) {
                facing = Direction.DOWN;
            } else {
                facing = Direction.RIGHT;
            }
        }
    }

    public void update(float delta, float moveX, float moveY) {
        update(delta, moveX, moveY, 0f, -1f);
    }

    public TextureRegion getCurrentFrame() {
        if (isMoving) {
            return getWalkAnimation(facing).getKeyFrame(walkStateTime, true);
        } else {
            // Idle animation is only used when facing down (for now)
            if (facing == Direction.DOWN) {
                return animIdle.getKeyFrame(idleStateTime, true);
            } else {
                // When facing Left, Right, Up, Up-Left, or Up-Right while stationary, show frame 0
                return getWalkAnimation(facing).getKeyFrames()[0];
            }
        }
    }

    public int getCurrentFrameIndex() {
        if (isMoving) {
            return getWalkAnimation(facing).getKeyFrameIndex(walkStateTime);
        } else {
            if (facing == Direction.DOWN) {
                return animIdle.getKeyFrameIndex(idleStateTime);
            } else {
                return 0;
            }
        }
    }

    public Animation<TextureRegion> getWalkAnimation(Direction dir) {
        switch (dir) {
            case LEFT:
                return animLeft;
            case RIGHT:
                return animRight;
            case UP:
                return animUp;
            case UP_LEFT:
                return animUpLeft;
            case UP_RIGHT:
                return animUpRight;
            case DOWN:
            default:
                return animDown;
        }
    }

    public Animation<TextureRegion> getIdleAnimation() {
        return animIdle;
    }

    public Animation<TextureRegion> getActiveAnimation() {
        if (isMoving) {
            return getWalkAnimation(facing);
        } else {
            if (facing == Direction.DOWN) {
                return animIdle;
            } else {
                return getWalkAnimation(facing);
            }
        }
    }

    public void draw(SpriteBatch batch, float x, float y, float width, float height) {
        TextureRegion frame = getCurrentFrame();
        if (frame != null) {
            batch.draw(frame, x, y, width, height);
        }
    }

    public Direction getFacing() {
        return facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        this.isMoving = moving;
    }

    public float getWalkStateTime() {
        return walkStateTime;
    }

    public void setWalkStateTime(float walkStateTime) {
        this.walkStateTime = walkStateTime;
    }

    public float getIdleStateTime() {
        return idleStateTime;
    }

    public void setIdleStateTime(float idleStateTime) {
        this.idleStateTime = idleStateTime;
    }

    public float getFrameDuration() {
        return walkFrameDuration;
    }

    public void setFrameDuration(float duration) {
        this.walkFrameDuration = Math.max(0.01f, duration);
        this.animLeft.setFrameDuration(this.walkFrameDuration);
        this.animRight.setFrameDuration(this.walkFrameDuration);
        this.animDown.setFrameDuration(this.walkFrameDuration);
        this.animUp.setFrameDuration(this.walkFrameDuration);
        this.animUpRight.setFrameDuration(this.walkFrameDuration);
        this.animUpLeft.setFrameDuration(this.walkFrameDuration);
    }

    public float getIdleFrameDuration() {
        return idleFrameDuration;
    }

    public void setIdleFrameDuration(float duration) {
        this.idleFrameDuration = Math.max(0.01f, duration);
        this.animIdle.setFrameDuration(this.idleFrameDuration);
    }

    public Texture getTexture() {
        return texture;
    }

    @Override
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
