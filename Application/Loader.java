package Application;

import Server.AbstractCommand;
import Server.FileMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Loader implements Initializable {

    private Path currentClientDir;
    public ListView<String> listView;//clientView
    public Label output;
    public ListView<String> listView1;//SererView
    public TextField clientPath;
    public TextField serverPath;
    private ObjectEncoderOutputStream outputStream;
    private ObjectDecoderInputStream inputStream;

    public void sendClick(ActionEvent actionEvent) throws IOException {
        String filename = listView.getSelectionModel().getSelectedItem();
        outputStream.writeObject(new FileMessage(Paths.get("fileDir/" + filename)));
        outputStream.flush();
    }
    public void download(ActionEvent actionEvent) throws IOException {
        String filename = listView1.getSelectionModel().getSelectedItem();
        outputStream.writeObject(new FileRequest(filename));
        outputStream.flush();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            currentClientDir = Paths.get("fileDir").toAbsolutePath();
            Socket socket = new Socket("localhost", 8189);
            outputStream = new ObjectEncoderOutputStream(socket.getOutputStream());
            inputStream = new ObjectDecoderInputStream(socket.getInputStream());

            outputStream.writeObject(new ListRequest());
            refreshClientList();
            addNavigationListener();

            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        AbstractCommand command = (AbstractCommand) inputStream.readObject();
                        switch (command.getType()) {
                            case LIST_MESSAGE:
                                ListResponse response = (ListResponse) command;
                                List<String> names = response.getNames();
                                refreshServerList(names);
                                break;
                            case FILE_MESSAGE:
                                FileMessage message = (FileMessage) command;
                                Files.write(Paths.get(currentClientDir + message.getName()), message.getData());
                                refreshClientList();
                                break;
                            case PATH_RESPONSE:
                                PathUpResponse pathResponse= (PathUpResponse) command;
                                String path = pathResponse.getPath();
                                Platform.runLater(() -> serverPath.setText(path));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshClientList() throws IOException {
        clientPath.setText(currentClientDir.toString());
        List<String> names = Files.list(currentClientDir).map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        Platform.runLater(() -> {
            listView.getItems().clear();
            listView.getItems().addAll(names);
        });
    }

    private void refreshServerList(List<String> names) {
        Platform.runLater(() -> {
            listView1.getItems().clear();
            listView1.getItems().addAll(names);
        });

    }

    public void clientPathUp(ActionEvent actionEvent) throws IOException {
        currentClientDir = currentClientDir.getParent();
        clientPath.setText(currentClientDir.toString());
        refreshClientList();
    }

    public void serverPathUp(ActionEvent actionEvent) throws IOException {
        outputStream.writeObject(new PathUpRequest());
        outputStream.flush();
    }

    private void addNavigationListener() {

        listView.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2) {
                String item = listView.getSelectionModel().getSelectedItem();
                Path newPath = currentClientDir.resolve(item);
                if (Files.isDirectory(newPath)) {
                    currentClientDir = newPath;
                    try {
                        refreshClientList();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });

        listView1.setOnMouseClicked(e -> {
            if(e.getClickCount() == 2) {
                String item = listView1.getSelectionModel().getSelectedItem();
                try {
                    outputStream.writeObject(new PathInRequest(item));
                    outputStream.flush();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }
}
