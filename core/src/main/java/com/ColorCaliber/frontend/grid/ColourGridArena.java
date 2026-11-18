package com.ColorCaliber.frontend.grid;

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
    public static final int DEFAULT_COLS = 16;
    public static final int DEFAULT_ROWS = 16;

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

    public interface BlueGridListener {
        void onBlueGridCollected();
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
        this.currentPatternName = "Isometric Stage: Green & Red Grids";
    }

    /**
     * Generates a balanced isometric arena pattern with Red hazard clusters
     * and Green heal zones.
     */
    public void generatePatternForRound(int round, float playerCenterX, float playerCenterY) {
        this.currentRound = 1;
        this.currentPatternName = "Isometric Stage: Green & Red Grids";

        // Reset all tiles to NORMAL floor
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                cells[c][r].setPatternType(GridType.NORMAL);
            }
        }

        // Place Red Hazard clusters symmetrically on the isometric stage
        setRegion(3, 4, 3, 2, GridType.RED);
        setRegion(10, 4, 3, 2, GridType.RED);
        setRegion(3, 10, 3, 2, GridType.RED);
        setRegion(10, 10, 3, 2, GridType.RED);
        setRegion(7, 7, 2, 2, GridType.RED);

        // Place Green Heal zones on the perimeter corners
        setRegion(1, 1, 2, 2, GridType.GREEN);
        setRegion(cols - 3, 1, 2, 2, GridType.GREEN);
        setRegion(1, rows - 3, 2, 2, GridType.GREEN);
        setRegion(cols - 3, rows - 3, 2, 2, GridType.GREEN);

        // Ensure safe zone around player starting position
        GridCell playerCell = getCellAt(playerCenterX, playerCenterY);
        if (playerCell != null) {
            int pc = playerCell.getCol();
            int pr = playerCell.getRow();
            for (int dc = -1; dc <= 1; dc++) {
                for (int dr = -1; dr <= 1; dr++) {
                    int nc = pc + dc;
                    int nr = pr + dr;
                    if (nc >= 0 && nc < cols && nr >= 0 && nr < rows) {
                        if (cells[nc][nr].getCurrentType() == GridType.RED) {
                            cells[nc][nr].setPatternType(GridType.NORMAL);
                        }
                    }
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

        // Update cell visual timers
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                cells[c][r].update(delta);
            }
        }

        // Apply effect of the grid cell directly under player's feet (only when not dodge rolling / invincible)
        if (!player.isDodgeRolling() && !player.isAirborne()) {
            GridCell playerCell = getCellAt(player.getCenterX(), player.getY() + 4f);
            if (playerCell != null) {
                GridType type = playerCell.getCurrentType();
                if (type == GridType.RED) {
                    player.applyRedGridDamage(10f);
                } else if (type == GridType.GREEN) {
                    player.heal(12f * delta);
                }
            }
        }
    }

    /**
     * Prevents the player from walking off the floating isometric diamond stage.
     */
    public void clampToIsometricStage(Player player) {
        float relX = player.getCenterX() - originX;
        float relY = (player.getY() + 4f) - originY;

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
            player.setPosition(newCenterX - player.getWidth() * 0.5f, newFeetY - 4f);
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
