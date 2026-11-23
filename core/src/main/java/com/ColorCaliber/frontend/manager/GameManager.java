package com.ColorCaliber.frontend.manager;

import com.ColorCaliber.frontend.entity.Bullet;
import com.ColorCaliber.frontend.entity.Enemy;
import com.ColorCaliber.frontend.entity.Enemy.EnemyType;
import com.ColorCaliber.frontend.entity.Player;
import com.ColorCaliber.frontend.grid.ColourGridArena;
import com.ColorCaliber.frontend.pool.BulletPool;
import com.ColorCaliber.frontend.pool.EnemyPool;
import com.ColorCaliber.frontend.state.GameOverState;
import com.ColorCaliber.frontend.state.GameStateManager;
import com.ColorCaliber.frontend.state.PlayingState;
import com.ColorCaliber.frontend.state.VictoryState;
import com.badlogic.gdx.math.MathUtils;

import java.util.List;

/**
 * Singleton Design Pattern implementation.
 * Centrally manages the game state, round progression, Colour Grid arena,
 * Object Pools (BulletPool & EnemyPool), Blue Grid points, weapon upgrades,
 * and completion time scoring.
 */
public class GameManager implements ColourGridArena.BlueGridListener {

    public enum GameState {
        PLAYING,
        VICTORY,
        GAME_OVER
    }

    public static final float WORLD_WIDTH = 1200f;
    public static final float WORLD_HEIGHT = 800f;
    public static final float ARENA_WIDTH = 1200f;
    public static final float ARENA_HEIGHT = 800f;
    public static final int MAX_ROUNDS = 1;
    public static final int WEAPON_UPGRADE_THRESHOLD = 999;

    // Singleton instance
    private static GameManager instance;

    private final GameStateManager stateManager;
    private GameState gameState;
    private final Player player;
    private final ColourGridArena gridArena;
    private final BulletPool bulletPool;
    private final EnemyPool enemyPool;

    private int currentRound;
    private int blueGridPoints;
    private int nextLevelUpPointTarget;
    private boolean weaponChoiceAvailable;

    private float elapsedTime;
    private float bestCompletionTime;
    private int finalScore;
    private int highScore;

    private int enemiesRemainingToSpawn;
    private int spawnPhase;
    private float enemySpawnTimer;
    private float roundBannerTimer;
    private String statusNotification;
    private float notificationTimer;

    // Private constructor to enforce Singleton pattern
    private GameManager() {
        this.stateManager = new GameStateManager();
        this.bulletPool = new BulletPool();
        this.enemyPool = new EnemyPool();
        this.gridArena = new ColourGridArena(ARENA_WIDTH, ARENA_HEIGHT);
        this.player = new Player(ARENA_WIDTH * 0.5f - 16f, 220f, ARENA_WIDTH, ARENA_HEIGHT);
        this.bestCompletionTime = -1f;
        this.highScore = 0;

        startNewGame();
    }

    /**
     * Global access point for the Singleton instance.
     */
    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void startNewGame() {
        this.gameState = GameState.PLAYING;
        this.currentRound = 1;
        this.blueGridPoints = 0;
        this.nextLevelUpPointTarget = 999;
        this.weaponChoiceAvailable = false;
        this.elapsedTime = 0f;
        this.finalScore = 0;

        bulletPool.freeAll();
        enemyPool.freeAll();
        player.reset(ARENA_WIDTH * 0.5f - 16f, 220f);

        setupRound(currentRound);
        stateManager.setState(new PlayingState(this));
    }

    private void setupRound(int round) {
        this.currentRound = 1;
        this.roundBannerTimer = 0f;
        bulletPool.freeAll();
        enemyPool.freeAll();

        gridArena.generatePatternForRound(1, player.getCenterX(), player.getCenterY());

        this.enemiesRemainingToSpawn = 0;
        this.spawnPhase = 1;
        this.enemySpawnTimer = 0f;

        ensureThreeEnemies();
    }

    private void spawnFiveEnemies() {
        float[] spawnXs = new float[] {
            ARENA_WIDTH * 0.30f - 16f,
            ARENA_WIDTH * 0.40f - 16f,
            ARENA_WIDTH * 0.50f - 16f,
            ARENA_WIDTH * 0.60f - 16f,
            ARENA_WIDTH * 0.70f - 16f
        };
        float spawnY = 540f;

        for (float spawnX : spawnXs) {
            Enemy enemy = enemyPool.spawnEnemy(EnemyType.SHOOTER, spawnX, spawnY, 1, ARENA_WIDTH, ARENA_HEIGHT);
            if (enemy != null) {
                gridArena.clampToIsometricStage(enemy);
            }
        }
    }

    private void ensureThreeEnemies() {
        float[] spawnXs = new float[] {
            ARENA_WIDTH * 0.38f - 16f,
            ARENA_WIDTH * 0.50f - 16f,
            ARENA_WIDTH * 0.62f - 16f
        };
        float spawnY = 540f;

        int slotIdx = 0;
        while (enemyPool.getActiveCount() < 3) {
            float spawnX = spawnXs[slotIdx % spawnXs.length];
            slotIdx++;
            enemyPool.spawnEnemy(EnemyType.SHOOTER, spawnX, spawnY, 1, ARENA_WIDTH, ARENA_HEIGHT);
        }
    }

    private void spawnNextEnemy() {
        if (enemiesRemainingToSpawn <= 0) return;

        // Pick a spawn position along the edges of the arena away from the player
        float spawnX = 60f;
        float spawnY = 60f;
        for (int attempt = 0; attempt < 10; attempt++) {
            int edge = MathUtils.random(0, 3);
            if (edge == 0) { // Top edge
                spawnX = MathUtils.random(40f, ARENA_WIDTH - 70f);
                spawnY = ARENA_HEIGHT - 60f;
            } else if (edge == 1) { // Bottom edge
                spawnX = MathUtils.random(40f, ARENA_WIDTH - 70f);
                spawnY = 40f;
            } else if (edge == 2) { // Left edge
                spawnX = 40f;
                spawnY = MathUtils.random(40f, ARENA_HEIGHT - 70f);
            } else { // Right edge
                spawnX = ARENA_WIDTH - 70f;
                spawnY = MathUtils.random(40f, ARENA_HEIGHT - 70f);
            }

            float dx = spawnX - player.getCenterX();
            float dy = spawnY - player.getCenterY();
            if (dx * dx + dy * dy > 220f * 220f) {
                break;
            }
        }

        EnemyType type;
        if (currentRound == MAX_ROUNDS && enemiesRemainingToSpawn == 1) {
            type = EnemyType.BOSS;
        } else {
            int roll = MathUtils.random(0, 99);
            if (currentRound == 1) {
                type = (roll < 70) ? EnemyType.SHOOTER : EnemyType.DASHER;
            } else if (currentRound == 2) {
                if (roll < 45) type = EnemyType.SHOOTER;
                else if (roll < 75) type = EnemyType.SPINNER;
                else type = EnemyType.DASHER;
            } else {
                if (roll < 35) type = EnemyType.SHOOTER;
                else if (roll < 70) type = EnemyType.SPINNER;
                else type = EnemyType.DASHER;
            }
        }

        enemyPool.spawnEnemy(type, spawnX, spawnY, currentRound, ARENA_WIDTH, ARENA_HEIGHT);
        enemiesRemainingToSpawn--;
    }

    public void update(float delta) {
        if (notificationTimer > 0f) {
            notificationTimer -= delta;
        }
        if (roundBannerTimer > 0f) {
            roundBannerTimer -= delta;
        }

        stateManager.update(delta);
    }

    public void updateGameplay(float delta) {
        elapsedTime += delta;

        // 1. Update Colour Grid Arena & apply grid effects to Player
        gridArena.update(delta, player, this);

        // 2. Update Player & Weapon firing
        player.update(delta);
        player.handleShooting(bulletPool);

        if (player.isDead()) {
            onPlayerDeath();
            return;
        }

        // 3. Spawn remaining enemies for the round
        if (enemiesRemainingToSpawn > 0) {
            enemySpawnTimer -= delta;
            if (enemySpawnTimer <= 0f) {
                spawnNextEnemy();
                enemySpawnTimer = Math.max(1.2f, 2.6f - currentRound * 0.25f);
            }
        }

        // 4. Update active Enemies
        List<Enemy> activeEnemies = enemyPool.getActiveObjects();
        for (int i = 0; i < activeEnemies.size(); i++) {
            Enemy enemy = activeEnemies.get(i);
            if (enemy.isActive()) {
                enemy.updateAI(delta, player, bulletPool);
                gridArena.clampToIsometricStage(enemy);
            }
        }

        // Keep enemies from overlapping each other
        for (int i = 0; i < activeEnemies.size(); i++) {
            Enemy a = activeEnemies.get(i);
            if (!a.isActive()) continue;
            for (int j = i + 1; j < activeEnemies.size(); j++) {
                Enemy b = activeEnemies.get(j);
                if (!b.isActive()) continue;
                float dx = b.getCenterX() - a.getCenterX();
                float dy = b.getCenterY() - a.getCenterY();
                float minDist = 36f;
                float distSq = dx * dx + dy * dy;
                if (distSq > 0.0001f && distSq < minDist * minDist) {
                    float dist = (float) Math.sqrt(distSq);
                    float push = (minDist - dist) * 0.5f;
                    float nx = dx / dist;
                    float ny = dy / dist;
                    a.setPosition(a.getX() - nx * push, a.getY() - ny * push);
                    b.setPosition(b.getX() + nx * push, b.getY() + ny * push);
                    gridArena.clampToIsometricStage(a);
                    gridArena.clampToIsometricStage(b);
                }
            }
        }

        // 5. Update active Bullets
        List<Bullet> activeBullets = bulletPool.getActiveObjects();
        for (int i = 0; i < activeBullets.size(); i++) {
            Bullet bullet = activeBullets.get(i);
            if (bullet.isActive()) {
                bullet.update(delta);
            }
        }

        // 6. Check Collisions
        handleCollisions();

        // 7. Recycle inactive objects back to Object Pools
        bulletPool.cleanupInactive();
        enemyPool.cleanupInactive();

        // 8. Phase transition: once all 3 Phase 1 enemies die, immediately spawn 5 enemies for Phase 2
        if (spawnPhase == 1 && enemyPool.getActiveCount() == 0) {
            spawnPhase = 2;
            spawnFiveEnemies();
        } else if (spawnPhase == 2 && enemyPool.getActiveCount() == 0) {
            onVictory();
        }
    }

    private void handleCollisions() {
        List<Bullet> activeBullets = bulletPool.getActiveObjects();
        List<Enemy> activeEnemies = enemyPool.getActiveObjects();

        for (int i = 0; i < activeBullets.size(); i++) {
            Bullet bullet = activeBullets.get(i);
            if (!bullet.isActive()) continue;

            if (bullet.isFromPlayer()) {
                // Check against enemies
                for (int j = 0; j < activeEnemies.size(); j++) {
                    Enemy enemy = activeEnemies.get(j);
                    if (!enemy.isActive()) continue;

                    if (bullet.getBounds().overlaps(enemy.getBounds())) {
                        enemy.takeDamage(bullet.getDamage());
                        bullet.setActive(false);
                        break;
                    }
                }
            } else {
                // Enemy bullet -> check against player (invincible during dodge roll)
                if (bullet.getBounds().overlaps(player.getBounds())) {
                    if (!player.isDodgeRolling()) {
                        player.takeDamage(bullet.getDamage());
                        bullet.setActive(false);
                    }
                }
            }
        }

        // Direct Enemy-Player contact damage (invincible during dodge roll)
        for (int j = 0; j < activeEnemies.size(); j++) {
            Enemy enemy = activeEnemies.get(j);
            if (enemy.isActive() && enemy.getBounds().overlaps(player.getBounds())) {
                if (!player.isDodgeRolling()) {
                    player.applyRedGridDamage(12f);
                }
            }
        }
    }

    @Override
    public void onBlueGridCollected() {
        blueGridPoints++;

        if (!player.isWeaponUpgraded() && blueGridPoints >= WEAPON_UPGRADE_THRESHOLD) {
            weaponChoiceAvailable = true;
            showNotification("BLUE GRID COLLECTED! Press [1] Shotgun or [2] SMG to Upgrade!", 4.0f);
        } else if (player.isWeaponUpgraded() && blueGridPoints >= nextLevelUpPointTarget) {
            player.upgradeCurrentWeaponLevel();
            nextLevelUpPointTarget += WEAPON_UPGRADE_THRESHOLD;
            showNotification("WEAPON UPGRADED TO " + player.getWeapon().getName() + " (+HP)!", 2.5f);
        } else {
            showNotification("+1 Blue Point! (" + blueGridPoints + " pts)", 1.2f);
        }
    }

    public void selectWeaponUpgrade(int option) {
        if (!weaponChoiceAvailable || player.isWeaponUpgraded()) return;

        if (option == 1) {
            player.chooseShotgun();
            weaponChoiceAvailable = false;
            showNotification("Equipped SHOTGUN (9 Pellets / Semi-Auto)!", 3.0f);
        } else if (option == 2) {
            player.chooseSmg();
            weaponChoiceAvailable = false;
            showNotification("Equipped RAPID SMG (Auto-Fire / Reduced Bullet DMG)!", 3.0f);
        }
    }

    public void onVictory() {
        this.gameState = GameState.VICTORY;

        // Score is recorded based on how fast all rounds are completed + Blue Grid points
        int speedScore = Math.max(1000, (int) (12000 - elapsedTime * 65f));
        int pointBonus = blueGridPoints * 250;
        this.finalScore = speedScore + pointBonus;

        if (bestCompletionTime < 0f || elapsedTime < bestCompletionTime) {
            bestCompletionTime = elapsedTime;
        }
        if (finalScore > highScore) {
            highScore = finalScore;
        }

        stateManager.setState(new VictoryState(this));
    }

    public void onPlayerDeath() {
        this.gameState = GameState.GAME_OVER;
        int roundBonus = (currentRound - 1) * 800;
        int pointBonus = blueGridPoints * 200;
        this.finalScore = roundBonus + pointBonus;
        if (finalScore > highScore) {
            highScore = finalScore;
        }

        stateManager.setState(new GameOverState(this));
    }

    private void showNotification(String message, float duration) {
        this.statusNotification = message;
        this.notificationTimer = duration;
    }

    public GameStateManager getStateManager() {
        return stateManager;
    }

    public GameState getGameState() {
        if (stateManager != null) {
            if (stateManager.getCurrentState() instanceof PlayingState) return GameState.PLAYING;
            if (stateManager.getCurrentState() instanceof GameOverState) return GameState.GAME_OVER;
            if (stateManager.getCurrentState() instanceof VictoryState) return GameState.VICTORY;
        }
        return gameState;
    }

    public Player getPlayer() {
        return player;
    }

    public ColourGridArena getGridArena() {
        return gridArena;
    }

    public BulletPool getBulletPool() {
        return bulletPool;
    }

    public EnemyPool getEnemyPool() {
        return enemyPool;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getBlueGridPoints() {
        return blueGridPoints;
    }

    public boolean isWeaponChoiceAvailable() {
        return weaponChoiceAvailable;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public float getBestCompletionTime() {
        return bestCompletionTime;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public int getHighScore() {
        return highScore;
    }

    public int getTotalEnemiesLeftInRound() {
        return enemiesRemainingToSpawn + enemyPool.getActiveCount();
    }

    public String getStatusNotification() {
        return notificationTimer > 0f ? statusNotification : null;
    }
}
