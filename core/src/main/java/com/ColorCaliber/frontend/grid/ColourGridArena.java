package com.ColorCaliber.frontend.grid;

import com.ColorCaliber.frontend.entity.Player;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Manages the 2D Colour Grid arena, round-specific grid patterns,
 * random Blue Grid spawning, and tile effects applied to the Player.
 */
public class ColourGridArena {
    public static final float CELL_SIZE = 40f;
    private static final int MAX_ACTIVE_BLUE_GRIDS = 5;
    private static final float BLUE_SPAWN_INTERVAL = 2.2f;

    private final int cols;
    private final int rows;
    private final float arenaWidth;
    private final float arenaHeight;
    private final GridCell[][] cells;

    private float blueSpawnTimer;
    private float dynamicPatternTimer;
    private boolean dynamicPhaseToggle;
    private int currentRound;
    private String currentPatternName;

    public interface BlueGridListener {
        void onBlueGridCollected();
    }

    public ColourGridArena(float arenaWidth, float arenaHeight) {
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.cols = (int) (arenaWidth / CELL_SIZE);
        this.rows = (int) (arenaHeight / CELL_SIZE);
        this.cells = new GridCell[cols][rows];

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                cells[c][r] = new GridCell(c, r, c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE);
            }
        }

        this.blueSpawnTimer = 0f;
        this.currentRound = 1;
        this.currentPatternName = "Green & Red Arena";
    }

    /**
     * Barebones arena pattern containing ONLY Green boxes and Red boxes.
     */
    public void generatePatternForRound(int round, float playerCenterX, float playerCenterY) {
        this.currentRound = 1;
        this.currentPatternName = "Stage 1: Green & Red Boxes";

        // Reset all tiles to NORMAL floor
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                cells[c][r].setPatternType(GridType.NORMAL);
            }
        }

        // Place Red Boxes (Hazard Zones)
        setRegion(5, 7, 3, 2, GridType.RED);
        setRegion(13, 9, 4, 2, GridType.RED);
        setRegion(22, 7, 3, 2, GridType.RED);
        setRegion(8, 12, 3, 2, GridType.RED);
        setRegion(19, 12, 3, 2, GridType.RED);

        // Place Green Boxes (Heal Zones)
        setRegion(3, 3, 2, 2, GridType.GREEN);
        setRegion(cols - 5, 3, 2, 2, GridType.GREEN);
        setRegion(cols / 2 - 1, 3, 2, 2, GridType.GREEN);
        setRegion(cols / 2 - 1, rows - 5, 2, 2, GridType.GREEN);

        // Ensure safe zone around player position
        int playerCol = MathUtils.clamp((int) (playerCenterX / CELL_SIZE), 0, cols - 1);
        int playerRow = MathUtils.clamp((int) (playerCenterY / CELL_SIZE), 0, rows - 1);
        for (int dc = -1; dc <= 1; dc++) {
            for (int dr = -1; dr <= 1; dr++) {
                int nc = playerCol + dc;
                int nr = playerRow + dr;
                if (nc >= 0 && nc < cols && nr >= 0 && nr < rows) {
                    if (cells[nc][nr].getCurrentType() == GridType.RED) {
                        cells[nc][nr].setPatternType(GridType.NORMAL);
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
        // Update cell visual timers
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                cells[c][r].update(delta);
            }
        }

        // Apply effect of the grid cell under the player's center (Red Hazard / Green Heal)
        GridCell playerCell = getCellAt(player.getCenterX(), player.getCenterY());
        if (playerCell != null) {
            GridType type = playerCell.getCurrentType();
            if (type == GridType.RED) {
                player.applyRedGridDamage(10f);
            } else if (type == GridType.GREEN) {
                player.heal(12f * delta);
            }
        }
    }

    private void toggleDynamicHazards(boolean phase) {
        int midC = cols / 2;
        int midR = rows / 2;
        for (int c = midC - 6; c <= midC + 6; c += 3) {
            for (int r = midR - 4; r <= midR + 4; r += 2) {
                if (c >= 1 && c < cols - 1 && r >= 1 && r < rows - 1) {
                    if (cells[c][r].getCurrentType() != GridType.BLUE) {
                        boolean makeRed = ((c + r) % 2 == 0) == phase;
                        cells[c][r].setCurrentType(makeRed ? GridType.RED : GridType.NORMAL);
                    }
                }
            }
        }
    }

    private void spawnRandomBlueGrid() {
        for (int attempt = 0; attempt < 40; attempt++) {
            int c = MathUtils.random(1, cols - 2);
            int r = MathUtils.random(1, rows - 2);
            GridCell cell = cells[c][r];
            if (cell.getCurrentType() == GridType.NORMAL || cell.getCurrentType() == GridType.YELLOW) {
                cell.setCurrentType(GridType.BLUE);
                return;
            }
        }
    }

    private int countActiveBlueGrids() {
        int count = 0;
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                if (cells[c][r].getCurrentType() == GridType.BLUE) {
                    count++;
                }
            }
        }
        return count;
    }

    public GridCell getCellAt(float worldX, float worldY) {
        int c = (int) (worldX / CELL_SIZE);
        int r = (int) (worldY / CELL_SIZE);
        if (c < 0 || c >= cols || r < 0 || r >= rows) {
            return null;
        }
        return cells[c][r];
    }

    public void render(ShapeRenderer shapeRenderer) {
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                cells[c][r].render(shapeRenderer);
            }
        }
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
