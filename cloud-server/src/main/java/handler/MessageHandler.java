package handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    private Path currentPath;

    public MessageHandler() throws IOException {
        currentPath = Paths.get("serverDir");
        if (!Files.exists(currentPath)) {
            Files.createDirectory(currentPath);
        }
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
                log.info(msg.toString());
                channelHandlerContext.writeAndFlush(msg);
                break;
            case PATH_UP:
                if (currentPath.getParent() != null) {
                    currentPath = currentPath.getParent();
                }
                channelHandlerContext.writeAndFlush(new PathUpResponse(currentPath.toString()));
                channelHandlerContext.writeAndFlush(new ListResponse(currentPath));
                break;
            case PATH_IN_REQUEST:
                PathInRequest request = (PathInRequest) command;
                Path newPath = currentPath.resolve(request.getDir());
                if (Files.isDirectory(newPath)) {
                    currentPath = newPath;
                    channelHandlerContext.writeAndFlush(new PathUpResponse(currentPath.toString()));
                    channelHandlerContext.writeAndFlush(new ListResponse(currentPath));
                }
                break;
            case DELETE_REQUEST:
                FileDeleteRequest fileDelete = (FileDeleteRequest) command;
                Files.delete(currentPath.resolve(fileDelete.getName()));
                channelHandlerContext.writeAndFlush(new ListResponse(currentPath));
                break;
            case RENAME_REQUEST:
                FileRenameRequest fileRename = (FileRenameRequest) command;
                File file = new File(String.valueOf(currentPath.resolve(fileRename.getName())));
                file.renameTo(new File(String.valueOf(currentPath.resolve(fileRename.getNewName()))));
                channelHandlerContext.writeAndFlush(new ListResponse(currentPath));
                break;
            case NEW_PATH_REQUEST:
                NewPathRequest newPathReq = (NewPathRequest) command;
                Path newPathName = currentPath.resolve(newPathReq.getPathName());
                if (Files.notExists(Paths.get(String.valueOf(newPathName)))) {
                    Files.createDirectory(Paths.get(String.valueOf(newPathName)));
                }
                channelHandlerContext.writeAndFlush(new ListResponse(currentPath));
                break;
            case NEW_FILE_REQUEST:
                NewFileRequest newFile = (NewFileRequest) command;
                Path newFileName = currentPath.resolve(newFile.getNewFileName());
                if (Files.notExists(Paths.get(String.valueOf(newFileName)))) {
                    Files.write(newFileName, "".getBytes(StandardCharsets.UTF_8));
                    channelHandlerContext.writeAndFlush(new ListResponse(currentPath));
                }
                break;
        }
    }
}
