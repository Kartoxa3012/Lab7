package common.commands;
import common.AuthenticatedCommand;
import common.Command;
import common.model.AstartesCategory;

import java.io.Serializable;

/**
 * Команда фильтрации элементов по категории.
 * Требует авторизации.
 *
 * @author Kovalenko Vlad, 504673
 * @see AuthenticatedCommand
 * @see AstartesCategory
 */
public class FilterByCategoryCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;
    private final AstartesCategory category;

    /**
     * Конструктор команды фильтрации по категории.
     *
     * @param username логин пользователя
     * @param password пароль пользователя
     * @param category категория для фильтрации
     */
    public FilterByCategoryCommand(String username, String password, AstartesCategory category) {
        super(username, password);
        this.category = category;
    }

    /**
     * Возвращает категорию для фильтрации.
     *
     * @return категория
     */
    public AstartesCategory getCategory() { return category; }
}