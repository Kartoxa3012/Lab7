package common.commands;
import common.Command;
import common.model.AstartesCategory;

import java.io.Serializable;

/**
 * Команда {@code filter_by_category} – запрос на фильтрацию элементов коллекции по категории.
 * Содержит категорию {@link AstartesCategory}, по которой сервер должен отфильтровать коллекцию.
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 * @see AstartesCategory
 */
public class FilterByCategoryCommand implements Command, Serializable {
    private static final long serialVersionUID = 1L;
    private final AstartesCategory category;

    /**
     * Создаёт команду фильтрации по категории.
     *
     * @param category категория для фильтрации (не может быть null)
     */
    public FilterByCategoryCommand(AstartesCategory category) {
        this.category = category;
    }

    /**
     * Возвращает категорию для фильтрации.
     *
     * @return категория
     */
    public AstartesCategory getCategory() {
        return category;
    }
}