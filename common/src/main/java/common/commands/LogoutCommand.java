package common.commands;

import common.AuthenticatedCommand;

public class LogoutCommand extends AuthenticatedCommand {
    public LogoutCommand(String username, String password) {
        super(username, password);
    }
}