package com.ColorCaliber.frontend.grid;

import com.ColorCaliber.frontend.entity.Entity;
import com.ColorCaliber.frontend.entity.Player;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ColourGridArena {
    public static final float TILE_WIDTH = 64f;
    public static final float TILE_HEIGHT = 32f;
    public static final float PLATFORM_DEPTH = 22f;
    public static final int DEFAULT_COLS = 19;
    public static final int DEFAULT_ROWS = 19;
    public static final int TOTAL_DIAMONDS_PER_ROUND = 20;

    private static final float SWEEP_BPM = 74f;
    private static final float BEATS_PER_SWEEP = 4f;
    private static final float SWEEP_HALF_WIDTH = 0.5f;
    private static final float SWEEP_DISTANCE = (DEFAULT_COLS - 1) + (DEFAULT_ROWS - 1) + 2f * SWEEP_HALF_WIDTH;
    private static final float SWEEP_SPEED = SWEEP_DISTANCE / (BEATS_PER_SWEEP * (60f / SWEEP_BPM));

    private static final float FALLING_ATTACK_SPEED = 10.5f;
    private static final int FALLING_ATTACK_TRAIL_LENGTH = 3;

    private final int cols;
    private final int rows;
    private final float halfW;
    private final float halfH;
    private final float originX;
    private final float originY;
    private final float arenaWidth;
    private final float arenaHeight;
    private final GridCell[][] cells;
    private final DotMatrixWalls dotMatrixWalls;
    private final List<FallingRedAttack> fallingRedAttacks;

    private int currentRound;
    private String currentPatternName;

    private boolean verticalSweepTurn;
    private float sweepPosition;
    private float sweepSpeedMultiplier;

    private int diamondsCollected;
    private GridCell activeDiamondCell;

    public interface BlueGridListener {
        void onBlueGridCollected();
        void onDiamondCollected(int collectedCount, int totalCount);
    }

    private static class FallingRedAttack {
        final boolean fromLeftWall;
        final int laneIndex;
        float headPos;
        final int trailLength;

        FallingRedAttack(boolean fromLeftWall, int laneIndex, float startPos, int trailLength) {
            this.fromLeftWall = fromLeftWall;
            this.laneIndex = laneIndex;
            this.headPos = startPos;
            this.trailLength = trailLength;
        }
    }

    private final DotMatrixWalls.AttackListener wallAttackListener = new DotMatrixWalls.AttackListener() {
        @Override
        public void onWallShipAttackLanded(boolean fromLeftWall, int laneIndex) {
            int maxLane = fromLeftWall ? (cols - 1) : (rows - 1);
            int clampedLane = MathUtils.clamp(laneIndex, 0, maxLane);
            float startPos = fromLeftWall ? (rows - 1f) : (cols - 1f);
            fallingRedAttacks.add(new FallingRedAttack(fromLeftWall, clampedLane, startPos, FALLING_ATTACK_TRAIL_LENGTH));
        }
    };

    public ColourGridArena(float arenaWidth, float arenaHeight) {
        this.arenaWidth = arenaWidth;
        this.arenaHeight = arenaHeight;
        this.cols = DEFAULT_COLS;
        this.rows = DEFAULT_ROWS;
        this.halfW = TILE_WIDTH * 0.5f;
        this.halfH = TILE_HEIGHT * 0.5f;

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

        this.dotMatrixWalls = new DotMatrixWalls(cols, rows, originX, originY, TILE_WIDTH, TILE_HEIGHT);
        this.fallingRedAttacks = new ArrayList<FallingRedAttack>();

        this.currentRound = 1;
        this.sweepSpeedMultiplier = 1.0f;
        this.currentPatternName = "Isometric Stage: Alternating Red Sweeps";
        resetSweepState();
    }

    private void resetSweepState() {
        this.verticalSweepTurn = true;
        this.sweepPosition = -(rows - 1) - SWEEP_HALF_WIDTH;
    }

    public void generatePatternForRound(int round, float playerCenterX, float playerCenterY) {
        this.currentRound = Math.max(1, round);
        this.fallingRedAttacks.clear();

        if (this.currentRound == 2) {
            this.sweepSpeedMultiplier = 1.15f;
            this.currentPatternName = "Level 2: Accelerated Red Sweeps";
        } else if (this.currentRound >= 3) {
            this.sweepSpeedMultiplier = 1.0f;
            this.currentPatternName = "Level 3: Wall Invaders & Falling Red Attacks";
        } else {
            this.sweepSpeedMultiplier = 1.0f;
            this.currentPatternName = "Level 1: Alternating Red Sweeps";
        }

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                cells[c][r].setPatternType(GridType.NORMAL);
            }
        }

        setRegion(1, 1, 2, 2, GridType.GREEN);
        setRegion(cols - 3, 1, 2, 2, GridType.GREEN);
        setRegion(1, rows - 3, 2, 2, GridType.GREEN);
        setRegion(cols - 3, rows - 3, 2, 2, GridType.GREEN);

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
        clampToIsometricStage(player);

        GridCell feetCell = getCellAt(player.getCenterX(), player.getY() + 4f);
        int playerCol = (feetCell != null) ? feetCell.getCol() : -1;
        int playerRow = (feetCell != null) ? feetCell.getRow() : -1;

        dotMatrixWalls.update(delta, playerCol, playerRow, wallAttackListener);
        updateFallingRedAttacks(delta);
        updateRedSweep(delta);

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                cells[c][r].update(delta);
            }
        }

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

    public void updateWallsOnly(float delta) {
        dotMatrixWalls.update(delta, -1, -1, null);
    }

    private void updateFallingRedAttacks(float delta) {
        Iterator<FallingRedAttack> it = fallingRedAttacks.iterator();
        while (it.hasNext()) {
            FallingRedAttack attack = it.next();
            attack.headPos -= FALLING_ATTACK_SPEED * delta;
            if (attack.headPos + attack.trailLength < -0.5f) {
                it.remove();
            }
        }
    }

    private void updateRedSweep(float delta) {
        float effectiveSpeed = SWEEP_SPEED * sweepSpeedMultiplier;

        if (verticalSweepTurn) {
            sweepPosition += effectiveSpeed * delta;
            float maxDiagX = (cols - 1) + SWEEP_HALF_WIDTH;
            if (sweepPosition > maxDiagX) {
                float overshoot = sweepPosition - maxDiagX;
                verticalSweepTurn = false;
                sweepPosition = (cols - 1) + (rows - 1) + SWEEP_HALF_WIDTH - overshoot;
            }
        } else {
            sweepPosition -= effectiveSpeed * delta;
            float minDiagY = -SWEEP_HALF_WIDTH;
            if (sweepPosition < minDiagY) {
                float overshoot = minDiagY - sweepPosition;
                verticalSweepTurn = true;
                sweepPosition = -(rows - 1) - SWEEP_HALF_WIDTH + overshoot;
            }
        }

        applyActiveRedSweep();
    }

    private void applyActiveRedSweep() {
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows; r++) {
                GridCell cell = cells[c][r];
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

        for (int i = 0; i < fallingRedAttacks.size(); i++) {
            FallingRedAttack attack = fallingRedAttacks.get(i);
            int minTile = (int) Math.floor(attack.headPos);
            int maxTile = minTile + attack.trailLength - 1;

            for (int pos = minTile; pos <= maxTile; pos++) {
                int c = attack.fromLeftWall ? attack.laneIndex : pos;
                int r = attack.fromLeftWall ? pos : attack.laneIndex;
                if (c < 0 || c >= cols || r < 0 || r >= rows) continue;

                GridCell cell = cells[c][r];
                if (cell.getBaseType() == GridType.GREEN || cell.getBaseType() == GridType.DIAMOND) {
                    continue;
                }
                cell.setCurrentType(GridType.RED);
            }
        }
    }

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

    public void render(ShapeRenderer shapeRenderer) {
        dotMatrixWalls.render(shapeRenderer);

        shapeRenderer.setColor(0.085f, 0.095f, 0.145f, 1f);
        {
            float wbx = originX + (0 - rows) * halfW;
            float wby = originY + (0 + rows) * halfH;
            shapeRenderer.triangle(wbx - halfW, wby, wbx, wby - halfH, wbx, wby - halfH - PLATFORM_DEPTH);
            shapeRenderer.triangle(wbx - halfW, wby, wbx, wby - halfH - PLATFORM_DEPTH, wbx - halfW, wby - PLATFORM_DEPTH);
        }
        for (int r = 0; r < rows; r++) {
            GridCell cell = cells[0][r];
            float cx = cell.getIsoX();
            float cy = cell.getIsoY();
            shapeRenderer.triangle(cx - halfW, cy, cx, cy - halfH, cx, cy - halfH - PLATFORM_DEPTH);
            shapeRenderer.triangle(cx - halfW, cy, cx, cy - halfH - PLATFORM_DEPTH, cx - halfW, cy - PLATFORM_DEPTH);
        }

        shapeRenderer.setColor(0.115f, 0.125f, 0.185f, 1f);
        {
            float wbx = originX + (cols - 0) * halfW;
            float wby = originY + (cols + 0) * halfH;
            shapeRenderer.triangle(wbx, wby - halfH, wbx + halfW, wby, wbx + halfW, wby - PLATFORM_DEPTH);
            shapeRenderer.triangle(wbx, wby - halfH, wbx + halfW, wby - PLATFORM_DEPTH, wbx, wby - halfH - PLATFORM_DEPTH);
        }
        for (int c = 0; c < cols; c++) {
            GridCell cell = cells[c][0];
            float cx = cell.getIsoX();
            float cy = cell.getIsoY();
            shapeRenderer.triangle(cx, cy - halfH, cx + halfW, cy, cx + halfW, cy - PLATFORM_DEPTH);
            shapeRenderer.triangle(cx, cy - halfH, cx + halfW, cy - PLATFORM_DEPTH, cx, cy - halfH - PLATFORM_DEPTH);
        }

        // Solid dark base diamond underneath all cells so the background never bleeds through tile gaps
        float floorBottomX = originX;
        float floorBottomY = originY - halfH;
        float floorRightX = originX + cols * halfW;
        float floorRightY = originY + (cols - 1) * halfH;
        float floorTopX = originX + (cols - rows) * halfW;
        float floorTopY = originY + (cols + rows - 1) * halfH;
        float floorLeftX = originX - rows * halfW;
        float floorLeftY = originY + (rows - 1) * halfH;
        shapeRenderer.setColor(0.06f, 0.07f, 0.11f, 1f);
        shapeRenderer.triangle(floorBottomX, floorBottomY, floorRightX, floorRightY, floorTopX, floorTopY);
        shapeRenderer.triangle(floorBottomX, floorBottomY, floorTopX, floorTopY, floorLeftX, floorLeftY);

        for (int sum = (cols - 1) + (rows - 1); sum >= 0; sum--) {
            for (int c = 0; c < cols; c++) {
                int r = sum - c;
                if (r >= 0 && r < rows) {
                    cells[c][r].render(shapeRenderer);
                }
            }
        }

        for (int i = 0; i < fallingRedAttacks.size(); i++) {
            FallingRedAttack attack = fallingRedAttacks.get(i);
            int headIdx = (int) Math.floor(attack.headPos);
            int c = attack.fromLeftWall ? attack.laneIndex : headIdx;
            int r = attack.fromLeftWall ? headIdx : attack.laneIndex;
            if (c < 0 || c >= cols || r < 0 || r >= rows) continue;

            GridCell cell = cells[c][r];
            if (cell.getBaseType() == GridType.GREEN || cell.getBaseType() == GridType.DIAMOND) continue;

            float cx = cell.getIsoX();
            float cy = cell.getIsoY();
            float fhW = halfW * 0.72f;
            float fhH = halfH * 0.72f;

            shapeRenderer.setColor(1.0f, 0.25f, 0.12f, 0.55f);
            shapeRenderer.triangle(cx, cy + fhH, cx - fhW, cy, cx + fhW, cy);
            shapeRenderer.triangle(cx, cy - fhH, cx - fhW, cy, cx + fhW, cy);

            float coreW = halfW * 0.36f;
            float coreH = halfH * 0.36f;
            shapeRenderer.setColor(1.0f, 0.85f, 0.55f, 0.92f);
            shapeRenderer.triangle(cx, cy + coreH, cx - coreW, cy, cx + coreW, cy);
            shapeRenderer.triangle(cx, cy - coreH, cx - coreW, cy, cx + coreW, cy);
        }
    }

    public DotMatrixWalls getDotMatrixWalls() {
        return dotMatrixWalls;
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

    public int getCurrentRound() {
        return currentRound;
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
