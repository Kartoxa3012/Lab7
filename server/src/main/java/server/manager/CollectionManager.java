package server.manager;

import common.model.SpaceMarine;
import server.dao.SpaceMarineDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CollectionManager {
    private static final Logger logger = LogManager.getLogger(CollectionManager.class);

    private final Map<String, SpaceMarine> cache = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final SpaceMarineDao dao = new SpaceMarineDao();
    private final LocalDateTime initDate = LocalDateTime.now();

    public void loadFromDatabase() {
        logger.info("Загрузка коллекции из базы данных...");
        try {
            List<SpaceMarine> list = dao.loadAll();
            lock.writeLock().lock();
            try {
                cache.clear();
                for (SpaceMarine marine : list) {
                    cache.put(marine.getKey(), marine);
                }
            } finally {
                lock.writeLock().unlock();
            }
            logger.info("Коллекция загружена. Загружено {} элементов", cache.size());
        } catch (Exception e) {
            logger.error("Ошибка загрузки коллекции: {}", e.getMessage());
        }
    }

    public void addToCache(SpaceMarine marine) {
        lock.writeLock().lock();
        try {
            cache.put(marine.getKey(), marine);
            logger.debug("Элемент добавлен в кэш: key={}", marine.getKey());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeFromCache(String key) {
        lock.writeLock().lock();
        try {
            cache.remove(key);
            logger.debug("Элемент удалён из кэша: key={}", key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public SpaceMarine getFromCache(String key) {
        lock.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean containsKey(String key) {
        lock.readLock().lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<SpaceMarine> getSortedValues() {
        lock.readLock().lock();
        try {
            List<SpaceMarine> list = new ArrayList<>(cache.values());
            Collections.sort(list);
            logger.trace("Возвращено {} отсортированных элементов", list.size());
            return list;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<String, SpaceMarine> getCollection() {
        lock.readLock().lock();
        try {
            return new HashMap<>(cache);
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public LocalDateTime getInitDate() {
        return initDate;
    }
}