package model;

public class NewPathRequest extends AbstractCommand{

    private final String pathName;

    public String getPathName() {
        return pathName;
    }

    public NewPathRequest(String pathName) {
        this.pathName = pathName;
    }

    @Override
    public CommandType getType() {
        return CommandType.NEW_PATH_REQUEST;
    }
}
