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
import com.ColorCaliber.frontend.visual.SynthwaveBackground;
import com.badlogic.gdx.math.MathUtils;

import java.util.List;

public class GameManager implements ColourGridArena.BlueGridListener {

    public enum GameState {
        PLAYING,
        VICTORY,
        GAME_OVER
    }

    public static final float WORLD_WIDTH = 1280f;
    public static final float WORLD_HEIGHT = 720f;
    public static final float ARENA_WIDTH = 1200f;
    public static final float ARENA_HEIGHT = 800f;
    public static final int MAX_ROUNDS = 3;
    public static final int WEAPON_UPGRADE_THRESHOLD = 999;

    private static GameManager instance;

    private final GameStateManager stateManager;
    private GameState gameState;
    private final Player player;
    private final ColourGridArena gridArena;
    private final SynthwaveBackground synthwaveBackground;
    private final BulletPool bulletPool;
    private final EnemyPool enemyPool;

    private int currentRound;
    private int blueGridPoints;
    private int diamondsCollected;
    private int score;
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

    private GameManager() {
        this.stateManager = new GameStateManager();
        this.bulletPool = new BulletPool();
        this.enemyPool = new EnemyPool();
        this.gridArena = new ColourGridArena(ARENA_WIDTH, ARENA_HEIGHT);
        this.synthwaveBackground = new SynthwaveBackground();
        this.player = new Player(ARENA_WIDTH * 0.5f - 16f, 220f, ARENA_WIDTH, ARENA_HEIGHT);
        this.bestCompletionTime = -1f;
        this.highScore = 0;

        startNewGame();
    }

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
        this.diamondsCollected = 0;
        this.score = 0;
        this.nextLevelUpPointTarget = 999;
        this.weaponChoiceAvailable = false;
        this.elapsedTime = 0f;
        this.finalScore = 0;
        this.statusNotification = null;
        this.notificationTimer = 0f;

        bulletPool.freeAll();
        enemyPool.freeAll();
        player.reset(ARENA_WIDTH * 0.5f - 16f, 220f);
        gridArena.getDotMatrixWalls().reset();

        setupRound(currentRound, false);
        stateManager.setState(new PlayingState(this));
    }

    private void setupRound(int round, boolean triggerWallLevelBanner) {
        this.currentRound = MathUtils.clamp(round, 1, MAX_ROUNDS);
        this.diamondsCollected = 0;
        this.roundBannerTimer = 0f;
        bulletPool.freeAll();
        enemyPool.freeAll();

        gridArena.generatePatternForRound(this.currentRound, player.getCenterX(), player.getCenterY());

        if (triggerWallLevelBanner) {
            boolean activateWallShips = (this.currentRound >= 3);
            gridArena.getDotMatrixWalls().triggerLevelUp(this.currentRound, activateWallShips);
        } else if (this.currentRound >= 3) {
            gridArena.getDotMatrixWalls().setSpaceshipsActive(true);
        } else {
            gridArena.getDotMatrixWalls().reset();
        }

        this.enemiesRemainingToSpawn = 0;
        this.spawnPhase = 1;
        this.enemySpawnTimer = 0f;

        ensureThreeEnemies();
    }

    public void advanceToNextRound() {
        if (currentRound >= MAX_ROUNDS) {
            onVictory();
            return;
        }
        int nextRound = currentRound + 1;
        player.heal(100f);
        setupRound(nextRound, true);
        AudioManager.getInstance().playUpgradeSound();
        if (nextRound == 3) {
            showNotification("LEVEL 3! INCOMING WALL INVADERS!", 3.2f);
        } else {
            showNotification("LEVEL " + nextRound + "!", 2.5f);
        }
    }

    public void jumpToLevel(int targetRound) {
        int clamped = MathUtils.clamp(targetRound, 1, MAX_ROUNDS);
        if (getGameState() != GameState.PLAYING) {
            this.gameState = GameState.PLAYING;
            player.reset(ARENA_WIDTH * 0.5f - 16f, 220f);
            stateManager.setState(new PlayingState(this));
        }
        boolean showBanner = (clamped >= 2);
        setupRound(clamped, showBanner);
        if (clamped == 3) {
            showNotification("LEVEL 3! WALL INVADERS DEPLOYING!", 3.0f);
        } else if (clamped == 2) {
            showNotification("LEVEL 2!", 2.2f);
        } else {
            showNotification("LEVEL 1", 1.8f);
        }
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
            Enemy enemy = enemyPool.spawnEnemy(EnemyType.SHOOTER, spawnX, spawnY, currentRound, ARENA_WIDTH, ARENA_HEIGHT);
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
            Enemy enemy = enemyPool.spawnEnemy(EnemyType.SHOOTER, spawnX, spawnY, currentRound, ARENA_WIDTH, ARENA_HEIGHT);
            if (enemy != null) {
                gridArena.clampToIsometricStage(enemy);
            }
        }
    }

    private void spawnNextEnemy() {
        if (enemiesRemainingToSpawn <= 0) return;

        float spawnX = 60f;
        float spawnY = 60f;
        for (int attempt = 0; attempt < 10; attempt++) {
            int edge = MathUtils.random(0, 3);
            if (edge == 0) {
                spawnX = MathUtils.random(40f, ARENA_WIDTH - 70f);
                spawnY = ARENA_HEIGHT - 60f;
            } else if (edge == 1) {
                spawnX = MathUtils.random(40f, ARENA_WIDTH - 70f);
                spawnY = 40f;
            } else if (edge == 2) {
                spawnX = 40f;
                spawnY = MathUtils.random(40f, ARENA_HEIGHT - 70f);
            } else {
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

        synthwaveBackground.update(delta);
        stateManager.update(delta);
    }

    public void updateVisualEffects(float delta) {
        gridArena.updateWallsOnly(delta);
    }

    public void updateGameplay(float delta) {
        elapsedTime += delta;

        gridArena.update(delta, player, this);
        if (getGameState() != GameState.PLAYING) {
            return;
        }

        player.update(delta);
        player.handleShooting(bulletPool);

        if (player.isDead()) {
            onPlayerDeath();
            return;
        }

        if (enemiesRemainingToSpawn > 0) {
            enemySpawnTimer -= delta;
            if (enemySpawnTimer <= 0f) {
                spawnNextEnemy();
                enemySpawnTimer = Math.max(1.2f, 2.6f - currentRound * 0.25f);
            }
        }

        List<Enemy> activeEnemies = enemyPool.getActiveObjects();
        for (int i = 0; i < activeEnemies.size(); i++) {
            Enemy enemy = activeEnemies.get(i);
            if (enemy.isActive()) {
                enemy.updateAI(delta, player, bulletPool);
                gridArena.clampToIsometricStage(enemy);
            }
        }

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

        List<Bullet> activeBullets = bulletPool.getActiveObjects();
        for (int i = 0; i < activeBullets.size(); i++) {
            Bullet bullet = activeBullets.get(i);
            if (bullet.isActive()) {
                bullet.update(delta);
            }
        }

        handleCollisions();

        bulletPool.cleanupInactive();
        enemyPool.cleanupInactive();

        if (spawnPhase == 1 && enemyPool.getActiveCount() == 0) {
            spawnPhase = 2;
            spawnFiveEnemies();
        }
    }

    private void handleCollisions() {
        List<Bullet> activeBullets = bulletPool.getActiveObjects();
        List<Enemy> activeEnemies = enemyPool.getActiveObjects();

        for (int i = 0; i < activeBullets.size(); i++) {
            Bullet bullet = activeBullets.get(i);
            if (!bullet.isActive()) continue;

            if (bullet.isFromPlayer()) {
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
                if (bullet.getBounds().overlaps(player.getBounds())) {
                    if (!player.isDodgeRolling()) {
                        player.takeDamage(bullet.getDamage());
                        bullet.setActive(false);
                    }
                }
            }
        }

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

    @Override
    public void onDiamondCollected(int collectedCount, int totalCount) {
        this.diamondsCollected = collectedCount;
        this.score += 100;
        AudioManager.getInstance().playUpgradeSound();

        if (collectedCount >= totalCount) {
            if (currentRound < MAX_ROUNDS) {
                advanceToNextRound();
            } else {
                onVictory();
            }
        } else {
            showNotification("+100! Diamonds: " + collectedCount + " / " + totalCount, 1.5f);
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
        this.finalScore = this.score;

        if (bestCompletionTime < 0f || elapsedTime < bestCompletionTime) {
            bestCompletionTime = elapsedTime;
        }
        if (finalScore > highScore) {
            highScore = finalScore;
        }

        gridArena.getDotMatrixWalls().triggerVictory();
        stateManager.setState(new VictoryState(this));
    }

    public void onPlayerDeath() {
        this.gameState = GameState.GAME_OVER;
        this.finalScore = this.score;
        if (finalScore > highScore) {
            highScore = finalScore;
        }

        gridArena.getDotMatrixWalls().triggerPlayerDeath();
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

    public SynthwaveBackground getSynthwaveBackground() {
        return synthwaveBackground;
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

    public int getDiamondsCollected() {
        return diamondsCollected;
    }

    public int getTotalDiamonds() {
        return ColourGridArena.TOTAL_DIAMONDS_PER_ROUND;
    }

    public int getScore() {
        return score;
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
