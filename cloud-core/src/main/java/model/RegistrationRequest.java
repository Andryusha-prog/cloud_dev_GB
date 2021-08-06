package model;

public class RegistrationRequest extends  AbstractCommand{

    private final String Login;
    private final String Password;

    public RegistrationRequest(String login, String password) {
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
        return CommandType.REGISTRATION_REQUEST;
    }
}
