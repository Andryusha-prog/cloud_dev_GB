package model;

public class NewFileRequest extends AbstractCommand{

    private final String newFileName;

    public String getNewFileName() {
        return newFileName;
    }

    public NewFileRequest(String newFileName) {
        this.newFileName = newFileName;
    }

    @Override
    public CommandType getType() {
        return CommandType.NEW_FILE_REQUEST;
    }
}
