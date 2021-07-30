package model;

public class FileRenameRequest extends AbstractCommand{

    private final String name;
    private final String newName;

    public String getName() {
        return name;
    }

    public String getNewName() {
        return newName;
    }

    public FileRenameRequest(String name, String newName) {
        this.name = name;
        this.newName = newName;
    }

    @Override
    public CommandType getType() {
        return CommandType.RENAME_REQUEST;
    }
}
