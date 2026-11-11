package com.ColorCaliber.frontend.manager;

import com.ColorCaliber.frontend.entity.Bullet;
import com.ColorCaliber.frontend.entity.Enemy;
import com.ColorCaliber.frontend.entity.Enemy.EnemyType;
import com.ColorCaliber.frontend.entity.Player;
import com.ColorCaliber.frontend.grid.ColourGridArena;
import com.ColorCaliber.frontend.pool.BulletPool;
import com.ColorCaliber.frontend.pool.EnemyPool;
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
    private float enemySpawnTimer;
    private float roundBannerTimer;
    private String statusNotification;
    private float notificationTimer;

    // Private constructor to enforce Singleton pattern
    private GameManager() {
        this.bulletPool = new BulletPool();
        this.enemyPool = new EnemyPool();
        this.gridArena = new ColourGridArena(ARENA_WIDTH, ARENA_HEIGHT);
        this.player = new Player(ARENA_WIDTH * 0.5f - 14f, 80f, ARENA_WIDTH, ARENA_HEIGHT);
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
        player.reset(ARENA_WIDTH * 0.5f - 14f, 80f);

        setupRound(currentRound);
    }

    private void setupRound(int round) {
        this.currentRound = 1;
        this.roundBannerTimer = 0f;
        bulletPool.freeAll();
        enemyPool.freeAll();

        gridArena.generatePatternForRound(1, player.getCenterX(), player.getCenterY());

        this.enemiesRemainingToSpawn = 0;
        this.enemySpawnTimer = 0f;

        ensureThreeEnemies();
    }

    private void ensureThreeEnemies() {
        float[] spawnXs = new float[] {
            ARENA_WIDTH * 0.25f - 16f,
            ARENA_WIDTH * 0.50f - 16f,
            ARENA_WIDTH * 0.75f - 16f
        };
        float spawnY = 620f;

        List<Enemy> activeEnemies = enemyPool.getActiveObjects();
        for (float spawnX : spawnXs) {
            boolean slotOccupied = false;
            for (int i = 0; i < activeEnemies.size(); i++) {
                Enemy e = activeEnemies.get(i);
                if (e.isActive() && Math.abs(e.getX() - spawnX) < 30f) {
                    slotOccupied = true;
                    break;
                }
            }
            if (!slotOccupied) {
                enemyPool.spawnEnemy(EnemyType.SHOOTER, spawnX, spawnY, 1, ARENA_WIDTH, ARENA_HEIGHT);
            }
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

        if (gameState != GameState.PLAYING) {
            return;
        }

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

        // 8. Keep running continuously: ensure 3 enemies remain active, no game over or victory screens
        ensureThreeEnemies();
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
                // Enemy bullet -> check against player
                if (bullet.getBounds().overlaps(player.getBounds())) {
                    player.takeDamage(bullet.getDamage());
                    bullet.setActive(false);
                }
            }
        }

        // Direct Enemy-Player contact damage
        for (int j = 0; j < activeEnemies.size(); j++) {
            Enemy enemy = activeEnemies.get(j);
            if (enemy.isActive() && enemy.getBounds().overlaps(player.getBounds())) {
                player.applyRedGridDamage(12f);
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

    private void onVictory() {
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
    }

    private void onPlayerDeath() {
        this.gameState = GameState.GAME_OVER;
        int roundBonus = (currentRound - 1) * 800;
        int pointBonus = blueGridPoints * 200;
        this.finalScore = roundBonus + pointBonus;
        if (finalScore > highScore) {
            highScore = finalScore;
        }
    }

    private void showNotification(String message, float duration) {
        this.statusNotification = message;
        this.notificationTimer = duration;
    }

    public GameState getGameState() {
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
