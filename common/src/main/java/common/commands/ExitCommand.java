
package common.commands;
import common.AuthenticatedCommand;
import common.Command;

import java.io.Serializable;

/**
 * Команда {@code exit} – запрос на завершение работы клиента.
 * Данная команда обрабатывается только на клиенте и не передаётся на сервер.
 *
 * @author Kovalenko Vlad, 504673
 * @see Command
 */
public class ExitCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;

    public ExitCommand(String username, String password) {
        super(username, password);
    }
}