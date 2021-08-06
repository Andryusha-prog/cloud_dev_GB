package model;

public class FileDeleteRequest extends AbstractCommand{

    private final String name;

    public String getName() {
        return name;
    }

    public FileDeleteRequest(String name) {
        this.name = name;
    }

    @Override
    public CommandType getType() {
        return CommandType.DELETE_REQUEST;
    }
}
