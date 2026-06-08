package server.dao;

import common.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object для работы с таблицей space_marines.
 *
 * @author Kovalenko Vlad, 504673
 */
public class SpaceMarineDao {
    private static final Logger logger = LogManager.getLogger(SpaceMarineDao.class);

    /**
     * Вставка нового элемента.
     *
     * @param marine  объект SpaceMarine (ключ должен быть установлен)
     * @param ownerId id владельца
     * @return true если вставка успешна
     * @throws SQLException если ошибка базы данных
     */
    public boolean insert(SpaceMarine marine, int ownerId) throws SQLException {
        logger.debug("Вставка элемента: key={}, ownerId={}", marine.getKey(), ownerId);

        String sql = "INSERT INTO space_marines (key, name, coord_x, coord_y, creation_date, " +
                "health, category, weapon_type, melee_weapon, chapter_name, " +
                "chapter_marines_count, chapter_world, owner_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, marine.getKey());
            stmt.setString(2, marine.getName());
            stmt.setFloat(3, marine.getCoordinates().getX());
            stmt.setFloat(4, marine.getCoordinates().getY());
            stmt.setTimestamp(5, Timestamp.valueOf(marine.getCreationDate()));
            stmt.setFloat(6, marine.getHealth());
            stmt.setString(7, marine.getCategory() == null ? null : marine.getCategory().name());
            stmt.setString(8, marine.getWeaponType().name());
            stmt.setString(9, marine.getMeleeWeapon() == null ? null : marine.getMeleeWeapon().name());

            Chapter ch = marine.getChapter();
            if (ch == null) {
                stmt.setNull(10, Types.VARCHAR);
                stmt.setNull(11, Types.INTEGER);
                stmt.setNull(12, Types.VARCHAR);
            } else {
                stmt.setString(10, ch.getName());
                stmt.setInt(11, ch.getMarinesCount());
                stmt.setString(12, ch.getWorld());
            }
            stmt.setInt(13, ownerId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        marine.setId(rs.getInt(1));
                        logger.info("Элемент вставлен: key={}, id={}, ownerId={}", marine.getKey(), marine.getId(), ownerId);
                    }
                }
                return true;
            }

            logger.warn("Элемент не вставлен: key={}", marine.getKey());
            return false;
        } catch (SQLException e) {
            logger.error("Ошибка БД при вставке key={}: {}", marine.getKey(), e.getMessage());
            throw e;
        }
    }

    /**
     * Обновляет существующий элемент в базе данных.
     *
     * @param marine  объект SpaceMarine с новыми данными (ключ должен быть установлен)
     * @param ownerId id владельца (для проверки прав)
     * @return true если обновление выполнено успешно, false если элемент не найден или не принадлежит пользователю
     * @throws SQLException если ошибка базы данных
     */
    public boolean update(SpaceMarine marine, int ownerId) throws SQLException {
        logger.debug("Обновление элемента: key={}, ownerId={}", marine.getKey(), ownerId);

        if (!exists(marine.getKey(), ownerId)) {
            logger.warn("Элемент с ключом '{}' не найден или не принадлежит пользователю {}", marine.getKey(), ownerId);
            return false;
        }

        String sql = "UPDATE space_marines SET name=?, coord_x=?, coord_y=?, health=?, " +
                "category=?, weapon_type=?, melee_weapon=?, chapter_name=?, " +
                "chapter_marines_count=?, chapter_world=? WHERE key=? AND owner_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, marine.getName());
            stmt.setFloat(2, marine.getCoordinates().getX());
            stmt.setFloat(3, marine.getCoordinates().getY());
            stmt.setFloat(4, marine.getHealth());
            stmt.setString(5, marine.getCategory() == null ? null : marine.getCategory().name());
            stmt.setString(6, marine.getWeaponType().name());
            stmt.setString(7, marine.getMeleeWeapon() == null ? null : marine.getMeleeWeapon().name());

            Chapter ch = marine.getChapter();
            if (ch == null) {
                stmt.setNull(8, Types.VARCHAR);
                stmt.setNull(9, Types.INTEGER);
                stmt.setNull(10, Types.VARCHAR);
            } else {
                stmt.setString(8, ch.getName());
                stmt.setInt(9, ch.getMarinesCount());
                stmt.setString(10, ch.getWorld());
            }

            stmt.setString(11, marine.getKey());
            stmt.setInt(12, ownerId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Элемент обновлён: key={}, ownerId={}", marine.getKey(), ownerId);
                return true;
            }

            logger.warn("Элемент не обновлён: key={}, ownerId={}", marine.getKey(), ownerId);
            return false;
        } catch (SQLException e) {
            logger.error("Ошибка БД при обновлении key={}: {}", marine.getKey(), e.getMessage());
            throw e;
        }
    }

    /**
     * Удаление элемента по ключу.
     *
     * @param key     ключ элемента
     * @param ownerId id владельца
     * @return true если удаление успешно
     * @throws SQLException если ошибка базы данных
     */
    public boolean delete(String key, int ownerId) throws SQLException {
        logger.debug("Удаление элемента: key={}, ownerId={}", key, ownerId);

        if (!exists(key, ownerId)) {
            logger.warn("Элемент с ключом '{}' не найден или не принадлежит пользователю {}", key, ownerId);
            return false;
        }

        String sql = "DELETE FROM space_marines WHERE key=? AND owner_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setInt(2, ownerId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Элемент удалён: key={}, ownerId={}", key, ownerId);
                return true;
            }

            logger.warn("Элемент не удалён: key={}, ownerId={}", key, ownerId);
            return false;
        }
    }

    /**
     * Удаление всех элементов пользователя.
     *
     * @param ownerId id владельца
     * @return количество удалённых элементов
     * @throws SQLException если ошибка базы данных
     */
    public int deleteAllByOwner(int ownerId) throws SQLException {
        logger.debug("Удаление всех элементов пользователя: ownerId={}", ownerId);

        String sql = "DELETE FROM space_marines WHERE owner_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ownerId);
            int affectedRows = stmt.executeUpdate();
            logger.info("Удалено {} элементов пользователя ownerId={}", affectedRows, ownerId);
            return affectedRows;
        }
    }

    /**
     * Загрузка всех элементов из БД.
     *
     * @return список всех SpaceMarine
     * @throws SQLException если ошибка базы данных
     */
    public List<SpaceMarine> loadAll() throws SQLException {
        logger.debug("Загрузка всех элементов из БД");

        List<SpaceMarine> list = new ArrayList<>();
        String sql = "SELECT * FROM space_marines ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToSpaceMarine(rs));
            }
            logger.info("Загружено {} элементов из БД", list.size());
        }
        return list;
    }

    /**
     * Загрузка элементов пользователя.
     *
     * @param ownerId id владельца
     * @return список SpaceMarine пользователя
     * @throws SQLException если ошибка базы данных
     */
    public List<SpaceMarine> loadByOwner(int ownerId) throws SQLException {
        logger.debug("Загрузка элементов пользователя: ownerId={}", ownerId);

        List<SpaceMarine> list = new ArrayList<>();
        String sql = "SELECT * FROM space_marines WHERE owner_id = ? ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ownerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRowToSpaceMarine(rs));
            }
            logger.info("Загружено {} элементов пользователя ownerId={}", list.size(), ownerId);
        }
        return list;
    }

    /**
     * Проверка существования элемента и принадлежности пользователю.
     *
     * @param key     ключ элемента
     * @param ownerId id владельца
     * @return true если элемент существует и принадлежит пользователю
     * @throws SQLException если ошибка базы данных
     */
    private boolean exists(String key, int ownerId) throws SQLException {
        String sql = "SELECT 1 FROM space_marines WHERE key = ? AND owner_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setInt(2, ownerId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * Получение элемента по ключу.
     *
     * @param key ключ элемента
     * @return SpaceMarine или null
     * @throws SQLException если ошибка базы данных
     */
    public SpaceMarine findByKey(String key) throws SQLException {
        logger.debug("Поиск элемента по ключу: {}", key);

        String sql = "SELECT * FROM space_marines WHERE key = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRowToSpaceMarine(rs);
            }
            logger.debug("Элемент с ключом {} не найден", key);
            return null;
        }
    }

    /**
     * Преобразование ResultSet в SpaceMarine.
     */
    private SpaceMarine mapRowToSpaceMarine(ResultSet rs) throws SQLException {
        Coordinates coords = new Coordinates(rs.getFloat("coord_x"), rs.getFloat("coord_y"));

        Chapter chapter = null;
        String chapterName = rs.getString("chapter_name");
        if (chapterName != null && !chapterName.isEmpty()) {
            chapter = new Chapter(
                    chapterName,
                    rs.getInt("chapter_marines_count"),
                    rs.getString("chapter_world")
            );
        }

        return new SpaceMarine(
                rs.getInt("id"),
                rs.getString("key"),
                rs.getString("name"),
                coords,
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getFloat("health"),
                rs.getString("category") == null ? null : AstartesCategory.valueOf(rs.getString("category")),
                Weapon.valueOf(rs.getString("weapon_type")),
                rs.getString("melee_weapon") == null ? null : MeleeWeapon.valueOf(rs.getString("melee_weapon")),
                chapter
        );
    }
}