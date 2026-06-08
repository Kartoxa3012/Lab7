package common.commands;

import common.AuthenticatedCommand;
import common.Command;
import java.io.Serializable;

public class ServerSaveCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;

    public ServerSaveCommand(String username, String password) {
        super(username, password);
    }
}
