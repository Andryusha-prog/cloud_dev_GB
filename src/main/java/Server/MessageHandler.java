package Server;

import Application.FileRequest;
import Application.ListResponse;
import Application.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    private Path currentPath;

    public MessageHandler() {
        currentPath = Paths.get("serverDir");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractCommand command) throws Exception {
        log.debug("received: {}", command);
        switch (command.getType()) {
            case FILE_MESSAGE:
                FileMessage message = (FileMessage) command;
                Files.write(currentPath.resolve(message.getName()), message.getData());
                channelHandlerContext.writeAndFlush(new ListResponse(currentPath));
                break;
            case LIST_REQUEST:
                channelHandlerContext.writeAndFlush(new ListResponse(currentPath));
                break;
            case FILE_REQUEST:
                FileRequest fileRequest = (FileRequest) command;
                FileMessage msg = new FileMessage(currentPath.resolve(fileRequest.getName()));
                channelHandlerContext.writeAndFlush(msg);
        }
    }
}
