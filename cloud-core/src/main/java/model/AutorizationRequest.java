package model;

public class AutorizationRequest extends AbstractCommand{

    private final String Login;
    private final String Password;

    public AutorizationRequest(String login, String password) {
        Login = login;
        Password = password;
    }

    public String getLogin() {
        return Login;
    }

    public String getPassword() {
        return Password;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTORIZATION_REQUEST;
    }
}
