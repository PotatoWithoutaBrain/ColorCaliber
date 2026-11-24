package com.ColorCaliber.frontend.grid;

import com.ColorCaliber.frontend.entity.Entity;
import com.ColorCaliber.frontend.entity.Player;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Manages the Isometric Colour Grid arena (2:1 projection).
 * Designed for 8-way isometric character walking animations.
 *
 * Provides:
 * - 2:1 Isometric diamond grid projection
 * - 3D floating platform skirt/base
 * - Symmetrical Red Hazard & Green Heal zones
 * - Dynamic stage boundary clamping so character stays on the floating stage
 */
public class ColourGridArena {
    public static final float TILE_WIDTH = 64f;
    public static final float TILE_HEIGHT = 32f;
    public static final int DEFAULT_COLS = 19;
    public static final int DEFAULT_ROWS = 19;
    public static final int TOTAL_DIAMONDS_PER_ROUND = 20;

    private static final float SWEEP_SPEED = 7.5f;
    private static final float SWEEP_HALF_WIDTH = 0.5f;

    private final int cols;
    private final int rows;
    private final float halfW;
    private final float halfH;
    private final float originX;
    private final float originY;
    private final float arenaWidth;
    private final float arenaHeight;
    private final GridCell[][] cells;

    private int currentRound;
    private String currentPatternName;

    // Alternating red sweep state: true = vertical (left -> right), false = horizontal (top -> bottom)
    private boolean verticalSweepTurn;
    private float sweepPosition;

    // One-by-one collectible diamond tile state
    private int diamondsCollected;
    private GridCell activeDiamondCell;

    public interface BlueGridListener {
        void onBlueGridCollected();
        void onDiamondCollected(int collectedCount, int totalCount);
    }

    public ColourGridArena(float arenaWidth, float arenaHeight) {
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.cols = DEFAULT_COLS;
        this.rows = DEFAULT_ROWS;
        this.halfW = TILE_WIDTH * 0.5f;   // 32f
        this.halfH = TILE_HEIGHT * 0.5f;  // 16f

        // Center the isometric diamond grid within the world
        this.originX = arenaWidth * 0.5f;
        this.originY = arenaHeight * 0.5f - ((cols - 1) + (rows - 1)) * 0.5f * halfH;

        this.cells = new GridCell[cols][rows];

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                float isoX = originX + (c - r) * halfW;
                float isoY = originY + (c + r) * halfH;
                cells[c][r] = new GridCell(c, r, isoX, isoY, TILE_WIDTH, TILE_HEIGHT);
            }
        }

        this.currentRound = 1;
        this.currentPatternName = "Isometric Stage: Alternating Red Sweeps";
        resetSweepState();
    }

    private void resetSweepState() {
        this.verticalSweepTurn = true;
        this.sweepPosition = -(rows - 1) - SWEEP_HALF_WIDTH;
    }

    /**
     * Generates the isometric arena pattern with Green heal zones
     * and initializes the alternating vertical/horizontal Red hazard sweeps.
     */
    public void generatePatternForRound(int round, float playerCenterX, float playerCenterY) {
        this.currentRound = 1;
        this.currentPatternName = "Isometric Stage: Alternating Red Sweeps";

        // Reset all tiles to NORMAL floor
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                cells[c][r].setPatternType(GridType.NORMAL);
            }
        }

        // Place Green Heal zones on the perimeter corners
        setRegion(1, 1, 2, 2, GridType.GREEN);
        setRegion(cols - 3, 1, 2, 2, GridType.GREEN);
        setRegion(1, rows - 3, 2, 2, GridType.GREEN);
        setRegion(cols - 3, rows - 3, 2, 2, GridType.GREEN);

        // Start with the vertical sweep from left to right
        resetSweepState();
        this.diamondsCollected = 0;
        this.activeDiamondCell = null;
        spawnNextDiamond(playerCenterX, playerCenterY);
        applyActiveRedSweep();
    }

    private void spawnNextDiamond(float avoidWorldX, float avoidWorldY) {
        if (diamondsCollected >= TOTAL_DIAMONDS_PER_ROUND) {
            activeDiamondCell = null;
            return;
        }

        GridCell avoidCell = getCellAt(avoidWorldX, avoidWorldY);

        for (int attempt = 0; attempt < 100; attempt++) {
            int c = MathUtils.random(0, cols - 1);
            int r = MathUtils.random(0, rows - 1);
            GridCell candidate = cells[c][r];
            if (candidate.getBaseType() == GridType.GREEN) {
                continue;
            }
            if (avoidCell != null && candidate == avoidCell) {
                continue;
            }
            candidate.setPatternType(GridType.DIAMOND);
            activeDiamondCell = candidate;
            return;
        }

        // Fallback deterministic scan if random attempts collided
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                GridCell candidate = cells[c][r];
                if (candidate.getBaseType() != GridType.GREEN && candidate != avoidCell) {
                    candidate.setPatternType(GridType.DIAMOND);
                    activeDiamondCell = candidate;
                    return;
                }
            }
        }
    }

    private void setRegion(int startCol, int startRow, int widthCols, int heightRows, GridType type) {
        for (int c = startCol; c < startCol + widthCols && c < cols; c++) {
            for (int r = startRow; r < startRow + heightRows && r < rows; r++) {
                if (c >= 0 && r >= 0) {
                    cells[c][r].setPatternType(type);
                }
            }
        }
    }

    public void update(float delta, Player player, BlueGridListener listener) {
        // Clamp player smoothly to the isometric diamond stage
        clampToIsometricStage(player);

        // Advance alternating vertical/horizontal red sweep
        updateRedSweep(delta);

        // Update cell visual timers
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                cells[c][r].update(delta);
            }
        }

        // Check diamond tile collection under player's feet
        GridCell feetCell = getCellAt(player.getCenterX(), player.getY() + 4f);
        if (feetCell != null && feetCell == activeDiamondCell) {
            activeDiamondCell.setPatternType(GridType.NORMAL);
            activeDiamondCell = null;
            diamondsCollected++;
            if (listener != null) {
                listener.onDiamondCollected(diamondsCollected, TOTAL_DIAMONDS_PER_ROUND);
            }
            if (diamondsCollected < TOTAL_DIAMONDS_PER_ROUND) {
                spawnNextDiamond(player.getCenterX(), player.getY() + 4f);
            }
            applyActiveRedSweep();
        }

        // Apply effect of the grid cell directly under player's feet (only when not dodge rolling / invincible)
        if (!player.isDodgeRolling() && !player.isAirborne()) {
            if (feetCell != null) {
                GridType type = feetCell.getCurrentType();
                if (type == GridType.RED) {
                    player.applyRedGridDamage(10f);
                } else if (type == GridType.GREEN) {
                    player.heal(12f * delta);
                }
            }
        }
    }

    private void updateRedSweep(float delta) {
        if (verticalSweepTurn) {
            // Vertical column moves smoothly left -> right along isoX (c - r)
            sweepPosition += SWEEP_SPEED * delta;
            float maxDiagX = (cols - 1) + SWEEP_HALF_WIDTH;
            if (sweepPosition > maxDiagX) {
                verticalSweepTurn = false;
                sweepPosition = (cols - 1) + (rows - 1) + SWEEP_HALF_WIDTH;
            }
        } else {
            // Horizontal column moves smoothly top -> bottom along isoY (c + r)
            sweepPosition -= SWEEP_SPEED * delta;
            float minDiagY = -SWEEP_HALF_WIDTH;
            if (sweepPosition < minDiagY) {
                verticalSweepTurn = true;
                sweepPosition = -(rows - 1) - SWEEP_HALF_WIDTH;
            }
        }

        applyActiveRedSweep();
    }

    private void applyActiveRedSweep() {
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                GridCell cell = cells[c][r];
                // Green heal zones and active diamond tiles never change to red when a sweep passes over them
                if (cell.getBaseType() == GridType.GREEN || cell.getBaseType() == GridType.DIAMOND) {
                    cell.revertToBaseType();
                    continue;
                }

                float cellCoord = verticalSweepTurn ? (c - r) : (c + r);
                if (Math.abs(cellCoord - sweepPosition) <= SWEEP_HALF_WIDTH) {
                    cell.setCurrentType(GridType.RED);
                } else {
                    cell.revertToBaseType();
                }
            }
        }
    }

    /**
     * Prevents an entity from walking off the floating isometric diamond stage.
     */
    public void clampToIsometricStage(Entity entity) {
        float relX = entity.getCenterX() - originX;
        float relY = (entity.getY() + 4f) - originY;

        float colF = (relY / halfH + relX / halfW) * 0.5f;
        float rowF = (relY / halfH - relX / halfW) * 0.5f;

        float margin = 0.35f;
        float clampedCol = MathUtils.clamp(colF, margin, cols - 1f - margin);
        float clampedRow = MathUtils.clamp(rowF, margin, rows - 1f - margin);

        if (clampedCol != colF || clampedRow != rowF) {
            float newRelX = (clampedCol - clampedRow) * halfW;
            float newRelY = (clampedCol + clampedRow) * halfH;
            float newCenterX = originX + newRelX;
            float newFeetY = originY + newRelY;
            entity.setPosition(newCenterX - entity.getWidth() * 0.5f, newFeetY - 4f);
        }
    }

    /**
     * Inverse isometric projection: converts 2D world coordinates to the grid cell.
     */
    public GridCell getCellAt(float worldX, float worldY) {
        float relX = worldX - originX;
        float relY = worldY - originY;
        float colF = (relY / halfH + relX / halfW) * 0.5f;
        float rowF = (relY / halfH - relX / halfW) * 0.5f;
        int c = (int) Math.floor(colF + 0.5f);
        int r = (int) Math.floor(rowF + 0.5f);
        if (c < 0 || c >= cols || r < 0 || r >= rows) {
            return null;
        }
        return cells[c][r];
    }

    /**
     * Renders the 3D floating platform sides and all depth-sorted isometric diamond tiles.
     */
    public void render(ShapeRenderer shapeRenderer) {
        float platformDepth = 22f;

        // 1. Draw 3D platform sides along the front edges of the arena
        // Front-left skirt: r = 0, c goes from 0 to cols - 1
        shapeRenderer.setColor(0.10f, 0.12f, 0.16f, 1f);
        for (int c = 0; c < cols; c++) {
            GridCell cell = cells[c][0];
            float cx = cell.getIsoX();
            float cy = cell.getIsoY();
            shapeRenderer.triangle(cx, cy - halfH, cx - halfW, cy, cx - halfW, cy - platformDepth);
            shapeRenderer.triangle(cx, cy - halfH, cx - halfW, cy - platformDepth, cx, cy - halfH - platformDepth);
        }

        // Front-right skirt: c = 0, r goes from 0 to rows - 1
        shapeRenderer.setColor(0.13f, 0.15f, 0.20f, 1f);
        for (int r = 0; r < rows; r++) {
            GridCell cell = cells[0][r];
            float cx = cell.getIsoX();
            float cy = cell.getIsoY();
            shapeRenderer.triangle(cx, cy - halfH, cx + halfW, cy, cx + halfW, cy - platformDepth);
            shapeRenderer.triangle(cx, cy - halfH, cx + halfW, cy - platformDepth, cx, cy - halfH - platformDepth);
        }

        // 2. Depth-sorted isometric floor tile rendering (from back/top to front/bottom)
        for (int sum = (cols - 1) + (rows - 1); sum >= 0; sum--) {
            for (int c = 0; c < cols; c++) {
                int r = sum - c;
                if (r >= 0 && r < rows) {
                    cells[c][r].render(shapeRenderer);
                }
            }
        }
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public float getOriginX() {
        return originX;
    }

    public float getOriginY() {
        return originY;
    }

    public String getCurrentPatternName() {
        return currentPatternName;
    }

    public float getArenaWidth() {
        return arenaWidth;
    }

    public float getArenaHeight() {
        return arenaHeight;
    }
}
