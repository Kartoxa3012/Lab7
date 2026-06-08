package common;

public abstract class AuthenticatedCommand implements Command {
    private static final long serialVersionUID = 1L;
    private final String username;
    private final String passwordHash;

    public AuthenticatedCommand(String username, String password) {
        this.username = username;
        this.passwordHash = PasswordUtil.hashMD2(password);
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
}
