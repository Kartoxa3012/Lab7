package common;

public class RegisterCommand extends AuthenticatedCommand {
    private static final long serialVersionUID = 1L;

    public RegisterCommand(String username, String password) {
        super(username, password);
    }
}
