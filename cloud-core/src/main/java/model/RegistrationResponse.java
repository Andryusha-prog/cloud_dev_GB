package model;

public class RegistrationResponse extends AbstractCommand {

    private final String message;

    public RegistrationResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public CommandType getType() {
        return CommandType.REGISTRATION_RESPONSE;
    }
}
