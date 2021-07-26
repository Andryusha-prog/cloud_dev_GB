package Server;

import Application.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListResponse(currentPath));
        ctx.writeAndFlush(new PathUpResponse(currentPath.toString()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractCommand command) throws Exception {
        log.debug("received: {}", command.getType());
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
                break;
            case PATH_UP:
                if(currentPath.getParent() != null) {
                    currentPath = currentPath.getParent();
                }
                channelHandlerContext.writeAndFlush(new PathUpResponse(currentPath.toString()));
                channelHandlerContext.writeAndFlush(new ListResponse(currentPath));
                break;
            case PATH_IN_REQUEST:
                PathInRequest request = (PathInRequest) command;
                Path newPath = currentPath.resolve(request.getDir());
                if(Files.isDirectory(newPath)) {
                    currentPath = newPath;
                    channelHandlerContext.writeAndFlush(new PathUpResponse(currentPath.toString()));
                    channelHandlerContext.writeAndFlush(new ListResponse(currentPath));
                }
                break;
        }
    }
}
