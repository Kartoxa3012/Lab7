package common;

public class LoginCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;

    public LoginCommand(String username, String password) {
        super(username, password);
    }
}
