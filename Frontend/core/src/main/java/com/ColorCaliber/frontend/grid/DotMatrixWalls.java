package com.ColorCaliber.frontend.grid;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.HashMap;
import java.util.Map;

public class DotMatrixWalls {

    public static final int WALL_HEIGHT = 5;
    public static final float BLOCK_HEIGHT = 28f;

    private static final Color GOLD_LED = new Color(1.0f, 0.835f, 0.310f, 1f);
    private static final Color AMBER_SPARKLE = new Color(1.0f, 0.65f, 0.20f, 1f);
    private static final Color CRIMSON_LED = new Color(1.0f, 0.10f, 0.24f, 1f);
    private static final Color CRIMSON_BRIGHT = new Color(1.0f, 0.32f, 0.36f, 1f);
    private static final Color EMERALD_LED = new Color(0.08f, 0.94f, 0.50f, 1f);
    private static final Color CYAN_LED = new Color(0.05f, 0.92f, 1.0f, 1f);
    private static final Color SHIP_LEFT_HULL = new Color(0.05f, 0.94f, 1.0f, 1f);
    private static final Color SHIP_RIGHT_HULL = new Color(1.0f, 0.18f, 0.82f, 1f);
    private static final Color SHIP_GUN_CHARGE = new Color(1.0f, 0.30f, 0.12f, 1f);
    private static final Color SHIP_LASER_RED = new Color(1.0f, 0.12f, 0.22f, 1f);

    private static final Color BORDER_CHARCOAL = new Color(0.048f, 0.050f, 0.068f, 1f);
    private static final Color FACE_LEFT_MATTE = new Color(0.098f, 0.100f, 0.126f, 1f);
    private static final Color FACE_RIGHT_MATTE = new Color(0.080f, 0.082f, 0.105f, 1f);
    private static final Color FACE_TOP_MATTE = new Color(0.118f, 0.120f, 0.148f, 1f);
    private static final Color FACE_END_MATTE = new Color(0.065f, 0.068f, 0.088f, 1f);
    private static final Color PIXEL_SOCKET_DARK = new Color(0.068f, 0.070f, 0.092f, 1f);

    public enum AnimationMode {
        IDLE,
        LEVEL_UP,
        DEATH,
        VICTORY
    }

    public interface AttackListener {
        void onWallShipAttackLanded(boolean fromLeftWall, int laneIndex);
    }

    private final int cols;
    private final int rows;
    private final int totalWidth;
    private final float originX;
    private final float originY;
    private final float halfW;
    private final float halfH;

    private final float[][] ledR;
    private final float[][] ledG;
    private final float[][] ledB;
    private final float[][] ledIntensity;

    private AnimationMode mode;
    private float modeTimer;
    private String scrollText;
    private boolean[][] textBitmap;
    private int textWidth;
    private int pendingSpaceshipActivationLevel;

    private boolean spaceshipsActive;
    private final WallShip leftShip;
    private final WallShip rightShip;

    private final Color scratch = new Color();

    private static final Map<Character, String[]> FONT_3ROW = new HashMap<Character, String[]>();

    static {
        FONT_3ROW.put('L', new String[] {
            "#.",
            "#.",
            "##"
        });
        FONT_3ROW.put('V', new String[] {
            "#.#",
            "#.#",
            ".#."
        });
        FONT_3ROW.put('E', new String[] {
            "###",
            "##.",
            "###"
        });
        FONT_3ROW.put('W', new String[] {
            "#.#.#",
            "#.#.#",
            ".#.#."
        });
        FONT_3ROW.put('I', new String[] {
            "###",
            ".#.",
            "###"
        });
        FONT_3ROW.put('N', new String[] {
            "#..#",
            "##.#",
            "#.##"
        });
        FONT_3ROW.put(' ', new String[] {
            "..",
            "..",
            ".."
        });
        FONT_3ROW.put('0', new String[] {
            "###",
            "#.#",
            "###"
        });
        FONT_3ROW.put('1', new String[] {
            "##",
            ".#",
            ".#"
        });
        FONT_3ROW.put('2', new String[] {
            ".##",
            ".#.",
            "###"
        });
        FONT_3ROW.put('3', new String[] {
            "###",
            ".##",
            "###"
        });
        FONT_3ROW.put('4', new String[] {
            "#.#",
            "###",
            "..#"
        });
        FONT_3ROW.put('5', new String[] {
            "###",
            "##.",
            ".##"
        });
        FONT_3ROW.put('6', new String[] {
            "#..",
            "###",
            "###"
        });
        FONT_3ROW.put('7', new String[] {
            "###",
            "..#",
            ".#."
        });
        FONT_3ROW.put('8', new String[] {
            "###",
            "###",
            "###"
        });
        FONT_3ROW.put('9', new String[] {
            "###",
            "###",
            "..#"
        });
        FONT_3ROW.put('!', new String[] {
            "#",
            "#",
            "."
        });
    }

    private static class WallShip {
        final boolean isLeftWall;
        final int minU;
        final int maxU;
        int centerU;
        int moveDir;
        float stepTimer;
        float fireCooldown;
        float chargeTimer;
        boolean charging;

        boolean droppingPulse;
        int pulseU;
        int pulseRow;
        float pulseTimer;

        WallShip(boolean isLeftWall, int minU, int maxU, int startU, int startDir, float initialFireDelay) {
            this.isLeftWall = isLeftWall;
            this.minU = minU;
            this.maxU = maxU;
            this.centerU = startU;
            this.moveDir = startDir;
            this.stepTimer = 0f;
            this.fireCooldown = initialFireDelay;
            this.chargeTimer = 0f;
            this.charging = false;
            this.droppingPulse = false;
            this.pulseRow = -1;
            this.pulseTimer = 0f;
        }

        void reset(int startU, int startDir, float initialFireDelay) {
            this.centerU = startU;
            this.moveDir = startDir;
            this.stepTimer = 0f;
            this.fireCooldown = initialFireDelay;
            this.chargeTimer = 0f;
            this.charging = false;
            this.droppingPulse = false;
            this.pulseRow = -1;
            this.pulseTimer = 0f;
        }
    }

    public DotMatrixWalls(int cols, int rows, float originX, float originY, float tileWidth, float tileHeight) {
        this.cols = cols;
        this.rows = rows;
        this.totalWidth = cols + rows;
        this.originX = originX;
        this.originY = originY;
        this.halfW = tileWidth * 0.5f;
        this.halfH = tileHeight * 0.5f;

        this.ledR = new float[totalWidth][WALL_HEIGHT];
        this.ledG = new float[totalWidth][WALL_HEIGHT];
        this.ledB = new float[totalWidth][WALL_HEIGHT];
        this.ledIntensity = new float[totalWidth][WALL_HEIGHT];

        this.mode = AnimationMode.IDLE;
        this.modeTimer = 0f;
        this.scrollText = "";
        this.spaceshipsActive = false;
        this.pendingSpaceshipActivationLevel = -1;

        this.leftShip = new WallShip(true, 1, cols - 2, 5, 1, 1.3f);
        this.rightShip = new WallShip(false, cols + 1, totalWidth - 2, totalWidth - 6, -1, 2.5f);
    }

    public void reset() {
        this.mode = AnimationMode.IDLE;
        this.modeTimer = 0f;
        this.spaceshipsActive = false;
        this.pendingSpaceshipActivationLevel = -1;
        leftShip.reset(5, 1, 1.3f);
        rightShip.reset(totalWidth - 6, -1, 2.5f);
        clearLeds();
    }

    public void triggerLevelUp(int level, boolean activateSpaceshipsAfter) {
        this.mode = AnimationMode.LEVEL_UP;
        this.modeTimer = 0f;
        this.scrollText = "LVL " + level;
        buildTextBitmap(this.scrollText);
        this.spaceshipsActive = false;
        this.pendingSpaceshipActivationLevel = activateSpaceshipsAfter ? level : -1;
    }

    public void setSpaceshipsActive(boolean active) {
        this.spaceshipsActive = active;
        this.pendingSpaceshipActivationLevel = -1;
        if (active) {
            if (this.mode == AnimationMode.LEVEL_UP) {
                this.mode = AnimationMode.IDLE;
                this.modeTimer = 0f;
            }
            leftShip.reset(5, 1, 0.9f);
            rightShip.reset(totalWidth - 6, -1, 1.9f);
        }
    }

    public boolean areSpaceshipsActive() {
        return spaceshipsActive;
    }

    public void triggerPlayerDeath() {
        this.mode = AnimationMode.DEATH;
        this.modeTimer = 0f;
        this.spaceshipsActive = false;
        this.pendingSpaceshipActivationLevel = -1;
    }

    public void triggerVictory() {
        this.mode = AnimationMode.VICTORY;
        this.modeTimer = 0f;
        this.spaceshipsActive = false;
        this.pendingSpaceshipActivationLevel = -1;
    }

    public AnimationMode getAnimationMode() {
        return mode;
    }

    private void buildTextBitmap(String text) {
        String upper = text.toUpperCase();
        int totalCols = 0;
        for (int i = 0; i < upper.length(); i++) {
            String[] glyph = FONT_3ROW.get(upper.charAt(i));
            if (glyph == null) glyph = FONT_3ROW.get(' ');
            totalCols += glyph[0].length();
            if (i < upper.length() - 1) {
                totalCols += 1;
            }
        }
        this.textWidth = Math.max(1, totalCols);
        this.textBitmap = new boolean[this.textWidth][3];

        int cursor = 0;
        for (int i = 0; i < upper.length(); i++) {
            String[] glyph = FONT_3ROW.get(upper.charAt(i));
            if (glyph == null) glyph = FONT_3ROW.get(' ');
            int gw = glyph[0].length();
            for (int gx = 0; gx < gw; gx++) {
                textBitmap[cursor + gx][2] = (glyph[0].charAt(gx) == '#');
                textBitmap[cursor + gx][1] = (glyph[1].charAt(gx) == '#');
                textBitmap[cursor + gx][0] = (glyph[2].charAt(gx) == '#');
            }
            cursor += gw + 1;
        }
    }

    private void clearLeds() {
        for (int u = 0; u < totalWidth; u++) {
            for (int z = 0; z < WALL_HEIGHT; z++) {
                ledIntensity[u][z] = 0f;
            }
        }
    }

    private void setLed(int u, int z, Color color, float intensity) {
        if (u < 0 || u >= totalWidth || z < 0 || z >= WALL_HEIGHT) return;
        float clamped = MathUtils.clamp(intensity, 0f, 1f);
        if (clamped <= 0.001f) return;
        ledR[u][z] = color.r;
        ledG[u][z] = color.g;
        ledB[u][z] = color.b;
        ledIntensity[u][z] = clamped;
    }

    private void setLedRgb(int u, int z, float r, float g, float b, float intensity) {
        if (u < 0 || u >= totalWidth || z < 0 || z >= WALL_HEIGHT) return;
        float clamped = MathUtils.clamp(intensity, 0f, 1f);
        if (clamped <= 0.001f) return;
        ledR[u][z] = r;
        ledG[u][z] = g;
        ledB[u][z] = b;
        ledIntensity[u][z] = clamped;
    }

    public void update(float delta, int playerCol, int playerRow, AttackListener attackListener) {
        modeTimer += delta;
        clearLeds();

        if (mode == AnimationMode.LEVEL_UP) {
            updateLevelUpAnimation();
            return;
        }
        if (mode == AnimationMode.DEATH) {
            updateDeathAnimation();
            return;
        }
        if (mode == AnimationMode.VICTORY) {
            updateVictoryAnimation();
            return;
        }

        if (spaceshipsActive) {
            updateWallShip(leftShip, delta, playerCol, attackListener);
            updateWallShip(rightShip, delta, (playerRow >= 0) ? (totalWidth - 1 - playerRow) : -1, attackListener);
            drawWallShipToLeds(leftShip, SHIP_LEFT_HULL);
            drawWallShipToLeds(rightShip, SHIP_RIGHT_HULL);
        }
    }

    private void updateLevelUpAnimation() {
        float flashPhaseEnd = 0.44f;
        float scrollSpeedColsPerSec = 18.0f;
        int totalSteps = totalWidth + textWidth + 2;
        float scrollDuration = totalSteps / scrollSpeedColsPerSec;
        float scrollPhaseEnd = flashPhaseEnd + scrollDuration;
        float fadeOutDuration = 0.42f;
        float totalDuration = scrollPhaseEnd + fadeOutDuration;

        if (modeTimer >= totalDuration) {
            mode = AnimationMode.IDLE;
            modeTimer = 0f;
            if (pendingSpaceshipActivationLevel > 0) {
                setSpaceshipsActive(true);
            }
            return;
        }

        if (modeTimer < flashPhaseEnd) {
            float k = modeTimer / flashPhaseEnd;
            float pulse = Math.abs(MathUtils.sin(k * MathUtils.PI * 2f));
            float brightness = 0.30f + 0.70f * pulse;
            for (int u = 0; u < totalWidth; u++) {
                float wave = 0.85f + 0.15f * MathUtils.sin(u * 0.4f - modeTimer * 18f);
                for (int z = 0; z < WALL_HEIGHT; z++) {
                    float rowMult = (z >= 1 && z <= 3) ? 1.0f : 0.65f;
                    setLed(u, z, GOLD_LED, brightness * wave * rowMult);
                }
            }
            return;
        }

        if (modeTimer < scrollPhaseEnd) {
            float st = modeTimer - flashPhaseEnd;
            int step = (int) Math.floor(st * scrollSpeedColsPerSec);
            int textStartU = -textWidth + step;

            for (int u = 0; u < totalWidth; u++) {
                int distBeforeText = textStartU - u;
                int distAfterText = u - (textStartU + textWidth - 1);

                if (distAfterText >= 3 && distAfterText <= 11) {
                    float falloff = 1.0f - (distAfterText - 3f) / 9f;
                    int waveRow = 1 + ((u + step) % 3);
                    if ((u + step) % 2 == 0) {
                        setLed(u, waveRow, AMBER_SPARKLE, 0.55f * falloff);
                    }
                    if (distAfterText == 4 || distAfterText == 7) {
                        int altRow = 1 + ((u * 2 + step) % 3);
                        setLed(u, altRow, GOLD_LED, 0.72f * falloff);
                    }
                }

                if (distBeforeText >= 2 && distBeforeText <= 12) {
                    float falloff = 1.0f - (distBeforeText - 2f) / 11f;
                    int waveZ = 1 + Math.round(1.0f + MathUtils.sin((u * 0.55f) - st * 11f));
                    waveZ = MathUtils.clamp(waveZ, 1, 3);
                    setLed(u, waveZ, GOLD_LED, 0.68f * falloff);
                    if ((u + step) % 3 == 0) {
                        int sparkZ = 1 + ((u + step) % 3);
                        setLed(u, sparkZ, AMBER_SPARKLE, 0.45f * falloff);
                    }
                }
            }

            if (textBitmap != null) {
                float glowShimmer = 0.92f + 0.08f * MathUtils.sin(st * 14f);
                for (int tx = 0; tx < textWidth; tx++) {
                    int u = textStartU + tx;
                    if (u < 0 || u >= totalWidth) continue;
                    for (int rowIdx = 0; rowIdx < 3; rowIdx++) {
                        if (textBitmap[tx][rowIdx]) {
                            setLed(u, rowIdx + 1, GOLD_LED, glowShimmer);
                        }
                    }
                }
            }
            return;
        }

        float ft = (modeTimer - scrollPhaseEnd) / fadeOutDuration;
        float fade = Math.max(0f, 1.0f - ft);
        for (int u = 0; u < totalWidth; u++) {
            float shimmer = 0.55f + 0.45f * MathUtils.sin(u * 0.6f + modeTimer * 12f);
            for (int z = 1; z <= 3; z++) {
                setLed(u, z, GOLD_LED, fade * fade * shimmer * 0.65f);
            }
        }
    }

    private void updateDeathAnimation() {
        float initialBurst = Math.max(0f, 1.0f - modeTimer * 2.2f);
        float heartbeat = 0.42f + 0.58f * (float) Math.pow(0.5f + 0.5f * MathUtils.sin(modeTimer * 5.2f), 2.0);
        float centerU = (totalWidth - 1) * 0.5f;

        for (int u = 0; u < totalWidth; u++) {
            float distFromCorner = Math.abs(u - centerU);
            float shockwave = 0.5f + 0.5f * MathUtils.sin(distFromCorner * 0.52f - modeTimer * 9.5f);

            for (int z = 0; z < WALL_HEIGHT; z++) {
                float vertBar = (z == 0 || z == 4) ? 0.90f : (0.55f + 0.45f * shockwave);
                float intensity = Math.max(initialBurst, heartbeat * vertBar);
                if (z == 2 && ((int) (modeTimer * 18f) + u) % 7 == 0) {
                    intensity = Math.min(1f, intensity + 0.25f);
                }
                Color c = (intensity > 0.85f) ? CRIMSON_BRIGHT : CRIMSON_LED;
                setLed(u, z, c, intensity);
            }
        }
    }

    private void updateVictoryAnimation() {
        float centerU = (totalWidth - 1) * 0.5f;

        for (int u = 0; u < totalWidth; u++) {
            float distFromCorner = Math.abs(u - centerU);

            float waveAngle = distFromCorner * 0.42f - modeTimer * 7.2f;
            float waveVal = 0.5f + 0.5f * MathUtils.sin(waveAngle);
            float crestZ = 2.0f + 1.85f * MathUtils.sin(distFromCorner * 0.38f - modeTimer * 6.4f);

            float colorMix = 0.5f + 0.5f * MathUtils.sin(distFromCorner * 0.28f - modeTimer * 4.0f);
            float r = MathUtils.lerp(EMERALD_LED.r, CYAN_LED.r, colorMix);
            float g = MathUtils.lerp(EMERALD_LED.g, CYAN_LED.g, colorMix);
            float b = MathUtils.lerp(EMERALD_LED.b, CYAN_LED.b, colorMix);

            for (int z = 0; z < WALL_HEIGHT; z++) {
                float distToCrest = Math.abs(z - crestZ);
                float ribbon = Math.max(0f, 1.0f - distToCrest * 0.55f);
                float baseGlow = 0.24f + 0.34f * waveVal;
                float intensity = MathUtils.clamp(baseGlow + ribbon * 0.72f, 0f, 1f);

                if (ribbon > 0.75f && ((int) (modeTimer * 12f) + u + z) % 5 == 0) {
                    setLedRgb(u, z, 0.72f, 1.0f, 0.94f, 1.0f);
                } else {
                    setLedRgb(u, z, r, g, b, intensity);
                }
            }
        }
    }

    private void updateWallShip(WallShip ship, float delta, int targetU, AttackListener listener) {
        if (ship.droppingPulse) {
            ship.pulseTimer += delta;
            float stepTime = 0.085f;
            if (ship.pulseTimer >= stepTime) {
                ship.pulseTimer -= stepTime;
                ship.pulseRow--;
                if (ship.pulseRow < 0) {
                    ship.droppingPulse = false;
                    if (listener != null) {
                        int laneIndex = ship.isLeftWall ? ship.pulseU : (totalWidth - 1 - ship.pulseU);
                        listener.onWallShipAttackLanded(ship.isLeftWall, laneIndex);
                    }
                }
            }
        }

        if (ship.charging) {
            ship.chargeTimer -= delta;
            if (ship.chargeTimer <= 0f) {
                ship.charging = false;
                ship.droppingPulse = true;
                ship.pulseU = ship.centerU;
                ship.pulseRow = 2;
                ship.pulseTimer = 0f;
                ship.fireCooldown = MathUtils.random(2.15f, 2.75f);
            }
            return;
        }

        ship.stepTimer += delta;
        float patrolStepInterval = 0.21f;
        if (ship.stepTimer >= patrolStepInterval) {
            ship.stepTimer -= patrolStepInterval;
            ship.centerU += ship.moveDir;

            if (ship.centerU >= ship.maxU) {
                ship.centerU = ship.maxU;
                ship.moveDir = -1;
            } else if (ship.centerU <= ship.minU) {
                ship.centerU = ship.minU;
                ship.moveDir = 1;
            } else if (targetU >= ship.minU && targetU <= ship.maxU && MathUtils.random() < 0.09f) {
                ship.moveDir = (targetU > ship.centerU) ? 1 : -1;
            }
        }

        ship.fireCooldown -= delta;
        if (ship.fireCooldown <= 0f && !ship.droppingPulse) {
            ship.charging = true;
            ship.chargeTimer = 0.44f;
        }
    }

    private void drawWallShipToLeds(WallShip ship, Color hullColor) {
        int u = ship.centerU;

        float hullPulse = ship.charging ? (0.85f + 0.15f * MathUtils.sin(modeTimer * 28f)) : 0.96f;
        setLed(u - 1, 4, hullColor, hullPulse);
        setLed(u,     4, hullColor, 1.0f);
        setLed(u + 1, 4, hullColor, hullPulse);

        if (ship.charging) {
            float blink = 0.65f + 0.35f * MathUtils.sin(modeTimer * 36f);
            setLed(u, 3, SHIP_GUN_CHARGE, blink);
        } else if (ship.droppingPulse) {
            setLed(u, 3, SHIP_LASER_RED, 1.0f);
        } else {
            setLed(u, 3, hullColor, 0.92f);
        }

        if (ship.droppingPulse && ship.pulseRow >= 0 && ship.pulseRow <= 2) {
            setLed(ship.pulseU, ship.pulseRow, SHIP_LASER_RED, 1.0f);
            if (ship.pulseRow + 1 <= 2) {
                setLed(ship.pulseU, ship.pulseRow + 1, SHIP_GUN_CHARGE, 0.50f);
            }
        }
    }

    public void render(ShapeRenderer sr) {
        float cornerGlow = Math.max(ledIntensity[cols - 1][4], ledIntensity[cols][4]);
        float cornerR = (ledIntensity[cols - 1][4] >= ledIntensity[cols][4]) ? ledR[cols - 1][4] : ledR[cols][4];
        float cornerG = (ledIntensity[cols - 1][4] >= ledIntensity[cols][4]) ? ledG[cols - 1][4] : ledG[cols][4];
        float cornerB = (ledIntensity[cols - 1][4] >= ledIntensity[cols][4]) ? ledB[cols - 1][4] : ledB[cols][4];

        for (int z = 0; z < WALL_HEIGHT; z++) {
            boolean drawTop = (z == WALL_HEIGHT - 1);
            renderWallBlock(sr, cols, rows, z, false, false, false, false, drawTop,
                cornerR, cornerG, cornerB, cornerGlow * 0.45f);
        }

        for (int c = cols - 1; c >= 0; c--) {
            int u = c;
            boolean isOuterEnd = (c == 0);
            for (int z = 0; z < WALL_HEIGHT; z++) {
                boolean drawTop = (z == WALL_HEIGHT - 1);
                renderWallBlock(
                    sr, c, rows, z,
                    isOuterEnd, true, false, true, drawTop,
                    ledR[u][z], ledG[u][z], ledB[u][z], ledIntensity[u][z]
                );
            }
        }

        for (int r = rows - 1; r >= 0; r--) {
            int u = cols + (rows - 1 - r);
            boolean isOuterEnd = (r == 0);
            for (int z = 0; z < WALL_HEIGHT; z++) {
                boolean drawTop = (z == WALL_HEIGHT - 1);
                renderWallBlock(
                    sr, cols, r, z,
                    true, isOuterEnd, true, false, drawTop,
                    ledR[u][z], ledG[u][z], ledB[u][z], ledIntensity[u][z]
                );
            }
        }
    }

    private void renderWallBlock(ShapeRenderer sr, int c, int r, int z,
                                 boolean drawFrontLeft, boolean drawFrontRight,
                                 boolean ledOnFrontLeft, boolean ledOnFrontRight,
                                 boolean drawTop,
                                 float lr, float lg, float lb, float lInt) {
        float bx = originX + (c - r) * halfW;
        float by = originY + (c + r) * halfH + z * BLOCK_HEIGHT;
        float ty = by + BLOCK_HEIGHT;

        float bLeftX = bx - halfW,   bLeftY = by;
        float bBotX  = bx,           bBotY  = by - halfH;
        float bRightX = bx + halfW,  bRightY = by;

        float tTopX  = bx,           tTopY  = ty + halfH;
        float tLeftX = bx - halfW,   tLeftY = ty;
        float tBotX  = bx,           tBotY  = ty - halfH;
        float tRightX = bx + halfW,  tRightY = ty;

        if (drawFrontLeft) {
            if (ledOnFrontLeft) {
                renderDisplayFace(sr, bLeftX, bLeftY, bBotX, bBotY, tBotX, tBotY, tLeftX, tLeftY,
                    FACE_RIGHT_MATTE, lr, lg, lb, lInt);
            } else {
                drawQuad(sr, bLeftX, bLeftY, bBotX, bBotY, tBotX, tBotY, tLeftX, tLeftY, BORDER_CHARCOAL);
                drawInsetQuad(sr, bLeftX, bLeftY, bBotX, bBotY, tBotX, tBotY, tLeftX, tLeftY, 0.08f, FACE_END_MATTE);
            }
        }

        if (drawFrontRight) {
            if (ledOnFrontRight) {
                renderDisplayFace(sr, bBotX, bBotY, bRightX, bRightY, tRightX, tRightY, tBotX, tBotY,
                    FACE_LEFT_MATTE, lr, lg, lb, lInt);
            } else {
                drawQuad(sr, bBotX, bBotY, bRightX, bRightY, tRightX, tRightY, tBotX, tBotY, BORDER_CHARCOAL);
                drawInsetQuad(sr, bBotX, bBotY, bRightX, bRightY, tRightX, tRightY, tBotX, tBotY, 0.08f, FACE_END_MATTE);
            }
        }

        if (drawTop) {
            drawQuad(sr, tLeftX, tLeftY, tBotX, tBotY, tRightX, tRightY, tTopX, tTopY, BORDER_CHARCOAL);
            if (lInt > 0.02f) {
                float tint = lInt * 0.42f;
                scratch.set(
                    MathUtils.lerp(FACE_TOP_MATTE.r, lr, tint),
                    MathUtils.lerp(FACE_TOP_MATTE.g, lg, tint),
                    MathUtils.lerp(FACE_TOP_MATTE.b, lb, tint),
                    1f
                );
                drawInsetQuad(sr, tLeftX, tLeftY, tBotX, tBotY, tRightX, tRightY, tTopX, tTopY, 0.09f, scratch);
            } else {
                drawInsetQuad(sr, tLeftX, tLeftY, tBotX, tBotY, tRightX, tRightY, tTopX, tTopY, 0.09f, FACE_TOP_MATTE);
            }
        }
    }

    private void renderDisplayFace(ShapeRenderer sr,
                                   float x0, float y0, float x1, float y1,
                                   float x2, float y2, float x3, float y3,
                                   Color matteBase,
                                   float lr, float lg, float lb, float lInt) {
        drawQuad(sr, x0, y0, x1, y1, x2, y2, x3, y3, BORDER_CHARCOAL);
        drawInsetQuad(sr, x0, y0, x1, y1, x2, y2, x3, y3, 0.075f, matteBase);

        if (lInt <= 0.01f) {
            drawInsetQuad(sr, x0, y0, x1, y1, x2, y2, x3, y3, 0.19f, PIXEL_SOCKET_DARK);
            return;
        }

        scratch.set(lr, lg, lb, 0.30f * lInt);
        drawInsetQuad(sr, x0, y0, x1, y1, x2, y2, x3, y3, -0.05f, scratch);

        scratch.set(
            MathUtils.lerp(matteBase.r, lr, lInt),
            MathUtils.lerp(matteBase.g, lg, lInt),
            MathUtils.lerp(matteBase.b, lb, lInt),
            1f
        );
        drawInsetQuad(sr, x0, y0, x1, y1, x2, y2, x3, y3, 0.11f, scratch);

        float coreBoost = 0.45f * lInt;
        scratch.set(
            Math.min(1f, lr + coreBoost),
            Math.min(1f, lg + coreBoost),
            Math.min(1f, lb + coreBoost),
            0.92f * lInt
        );
        drawInsetQuad(sr, x0, y0, x1, y1, x2, y2, x3, y3, 0.27f, scratch);
    }

    private void drawQuad(ShapeRenderer sr,
                          float x0, float y0, float x1, float y1,
                          float x2, float y2, float x3, float y3, Color color) {
        sr.setColor(color);
        sr.triangle(x0, y0, x1, y1, x2, y2);
        sr.triangle(x0, y0, x2, y2, x3, y3);
    }

    private void drawInsetQuad(ShapeRenderer sr,
                               float x0, float y0, float x1, float y1,
                               float x2, float y2, float x3, float y3,
                               float m, Color color) {
        float ux = x1 - x0;
        float uy = y1 - y0;
        float vx = x3 - x0;
        float vy = y3 - y0;
        float s0 = m;
        float s1 = 1f - m;

        float px0 = x0 + ux * s0 + vx * s0;
        float py0 = y0 + uy * s0 + vy * s0;
        float px1 = x0 + ux * s1 + vx * s0;
        float py1 = y0 + uy * s1 + vy * s0;
        float px2 = x0 + ux * s1 + vx * s1;
        float py2 = y0 + uy * s1 + vy * s1;
        float px3 = x0 + ux * s0 + vx * s1;
        float py3 = y0 + uy * s0 + vy * s1;

        drawQuad(sr, px0, py0, px1, py1, px2, py2, px3, py3, color);
    }
}
