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

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Loader implements Initializable {

    public ListView<String> listView;
    public Label output;
    public ListView<String> listView1;
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
            Socket socket = new Socket("localhost", 8189);
            outputStream = new ObjectEncoderOutputStream(socket.getOutputStream());
            inputStream = new ObjectDecoderInputStream(socket.getInputStream());

            outputStream.writeObject(new ListRequest());
            refreshClientList();
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
                                Files.write(Paths.get("fileDir/" + message.getName()), message.getData());
                                refreshClientList();
                                break;
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
        List<String> names = Files.list(Paths.get("fileDir")).map(p -> p.getFileName().toString())
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
}
