package com.ColorCaliber.frontend.pool;

import com.ColorCaliber.frontend.entity.Enemy;
import com.ColorCaliber.frontend.entity.Enemy.EnemyType;

/**
 * Concrete Object Pool for {@link Enemy} instances.
 */
public class EnemyPool extends ObjectPool<Enemy> {

    public EnemyPool() {
        super(20, 100);
    }

    @Override
    protected Enemy createObject() {
        return new Enemy();
    }

    public Enemy spawnEnemy(EnemyType type, float x, float y, int roundNumber,
                            float arenaWidth, float arenaHeight) {
        Enemy enemy = obtain();
        enemy.init(type, x, y, roundNumber, arenaWidth, arenaHeight);
        return enemy;
    }
}
