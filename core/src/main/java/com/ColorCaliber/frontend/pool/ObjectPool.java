package com.ColorCaliber.frontend.pool;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Generic Object Pool Design Pattern implementation.
 * Reuses pre-allocated or recycled objects to avoid frequent garbage collection
 * during intense bullet-hell gameplay.
 *
 * @param <T> Type of pooled object implementing {@link Poolable}
 */
public abstract class ObjectPool<T extends Poolable> {
    private final Deque<T> freeObjects;
    private final List<T> activeObjects;
    private final int maxCapacity;

    public ObjectPool(int initialCapacity, int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.freeObjects = new ArrayDeque<T>(initialCapacity);
        this.activeObjects = new ArrayList<T>(initialCapacity);

        for (int i = 0; i < initialCapacity; i++) {
            T obj = createObject();
            obj.setActive(false);
            freeObjects.push(obj);
        }
    }

    /**
     * Factory method to instantiate a new object when the pool is empty.
     */
    protected abstract T createObject();

    /**
     * Obtains an object from the pool, creating a new one if none are free.
     */
    public T obtain() {
        T obj = freeObjects.isEmpty() ? createObject() : freeObjects.pop();
        obj.reset();
        obj.setActive(true);
        activeObjects.add(obj);
        return obj;
    }

    /**
     * Returns an object back to the pool.
     */
    public void free(T obj) {
        if (obj == null) return;
        obj.reset();
        obj.setActive(false);
        activeObjects.remove(obj);
        if (freeObjects.size() < maxCapacity) {
            freeObjects.push(obj);
        }
    }

    /**
     * Sweeps through active objects and returns any inactive ones to the free pool.
     */
    public void cleanupInactive() {
        Iterator<T> iterator = activeObjects.iterator();
        while (iterator.hasNext()) {
            T obj = iterator.next();
            if (!obj.isActive()) {
                obj.reset();
                iterator.remove();
                if (freeObjects.size() < maxCapacity) {
                    freeObjects.push(obj);
                }
            }
        }
    }

    /**
     * Frees all active objects and returns them to the pool.
     */
    public void freeAll() {
        for (T obj : activeObjects) {
            obj.reset();
            obj.setActive(false);
            if (freeObjects.size() < maxCapacity) {
                freeObjects.push(obj);
            }
        }
        activeObjects.clear();
    }

    public List<T> getActiveObjects() {
        return activeObjects;
    }

    public int getFreeCount() {
        return freeObjects.size();
    }

    public int getActiveCount() {
        return activeObjects.size();
    }
}
