package Application;

import Server.AbstractCommand;
import Server.CommandType;

public class PathUpRequest extends AbstractCommand {
    @Override
    public CommandType getType() {
        return CommandType.PATH_UP;
    }
}
