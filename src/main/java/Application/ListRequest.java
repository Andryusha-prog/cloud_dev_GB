package Application;

import Server.AbstractCommand;
import Server.CommandType;

public class ListRequest extends AbstractCommand {
    @Override
    public CommandType getType() {
        return CommandType.LIST_REQUEST;
    }
}
