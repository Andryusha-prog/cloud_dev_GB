package handler;

import db.DBConnector;
import db.Users;
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
import java.sql.*;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    private Path currentPath;
    private String login;
    boolean autorizedClient = false;

    public MessageHandler() throws IOException {
        if(autorizedClient) {
            currentPath = Paths.get("serverDir/" + login);
            if (!Files.exists(currentPath)) {
                Files.createDirectory(currentPath);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(autorizedClient) {
            ctx.writeAndFlush(new ListResponse(currentPath));
            ctx.writeAndFlush(new PathUpResponse(currentPath.toString()));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractCommand command) throws Exception {
        if(autorizedClient)
            commandForAutorized(channelHandlerContext, command);
        else {

            switch(command.getType()) {
                case REGISTRATION_REQUEST:
                    RegistrationRequest regRequest = (RegistrationRequest) command;
                    try (Connection dbConnection = DBConnector.getConnection()){
                        String sqlStat = "INSERT INTO userstable (Login, Password) VALUES (?, ?)";
                        PreparedStatement preparedStat = dbConnection.prepareStatement(sqlStat);
                        preparedStat.setString(1, regRequest.getLogin());
                        preparedStat.setString(2, regRequest.getPassword());

                        int rows = preparedStat.executeUpdate();
                        if(rows == 1) channelHandlerContext.writeAndFlush(new RegistrationResponse("successfully"));
                        else channelHandlerContext.writeAndFlush(new RegistrationResponse("unsuccessfully"));


                    } catch (SQLException throwables) {
                        throw new RuntimeException(throwables);
                    }

                    break;
                case AUTORIZATION_REQUEST:
                    AutorizationRequest ar = (AutorizationRequest) command;
                    try(Connection dbConnection = DBConnector.getConnection()) {
                        String SQLReq = "SELECT idUsersTable, Login FROM userstable WHERE Login = ? AND Password = ?";
                        PreparedStatement ps = dbConnection.prepareStatement(SQLReq);
                        ps.setString(1, ar.getLogin());
                        ps.setString(2, ar.getPassword());

                        ResultSet resultSet = ps.executeQuery();
                        if(resultSet.next()){
                            login = resultSet.getString("Login");
                            autorizedClient = true;
                        }
                        channelHandlerContext.writeAndFlush(new AutorizationResponse(login, autorizedClient));

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    break;
            }
        }

    }

    private void commandForAutorized(ChannelHandlerContext channelHandlerContext, AbstractCommand command) throws IOException {
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
