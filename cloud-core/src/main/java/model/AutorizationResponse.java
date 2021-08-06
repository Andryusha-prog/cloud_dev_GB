package model;

public class AutorizationResponse extends AbstractCommand{

    private final String Login;
    private final boolean result;

    public AutorizationResponse(String login, boolean result) {
        Login = login;
        this.result = result;
    }

    public String getLogin() {
        return Login;
    }

    public boolean isResult() {
        return result;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTORIZATION_RESPONSE;
    }
}
