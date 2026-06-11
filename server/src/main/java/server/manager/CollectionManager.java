package server.manager;

import common.model.SpaceMarine;
import server.dao.SpaceMarineDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.*;

public class CollectionManager {
    private static final Logger logger = LogManager.getLogger(CollectionManager.class);

    private final Map<String, SpaceMarine> cache = Collections.synchronizedMap(new HashMap<>());
    private final SpaceMarineDao dao = new SpaceMarineDao();
    private final LocalDateTime initDate = LocalDateTime.now();

    public void loadFromDatabase() {
        logger.info("Загрузка коллекции из базы данных");
        try {
            List<SpaceMarine> list = dao.loadAll();
            cache.clear();
            for (SpaceMarine marine : list) {
                cache.put(marine.getKey(), marine);
            }
            logger.info("Коллекция загружена. Загружено {} элементов", cache.size());
        } catch (Exception e) {
            logger.error("Ошибка загрузки коллекции: {}", e.getMessage());
        }
    }

    public void addToCache(SpaceMarine marine) {
        cache.put(marine.getKey(), marine);
        logger.debug("Элемент добавлен в кэш: key={}", marine.getKey());
    }

    public void removeFromCache(String key) {
        cache.remove(key);
        logger.debug("Элемент удалён из кэша: key={}", key);
    }

    public SpaceMarine getFromCache(String key) {
        return cache.get(key);
    }

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public List<SpaceMarine> getSortedValues() {
        synchronized (cache) {
            List<SpaceMarine> list = new ArrayList<>(cache.values());
            Collections.sort(list);
            return list;
        }
    }

    public int size() {
        return cache.size();
    }

    public LocalDateTime getInitDate() {
        return initDate;
    }

    public Map<String, SpaceMarine> getCollection() {
        synchronized (cache) {
            return new HashMap<>(cache);
        }
    }
}