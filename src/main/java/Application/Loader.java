package Application;

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
import java.nio.file.Paths;
import java.util.ResourceBundle;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            outputStream = new ObjectEncoderOutputStream(socket.getOutputStream());
            inputStream = new ObjectDecoderInputStream(socket.getInputStream());
            File dir = new File("fileDir");
            listView.getItems().addAll(dir.list());
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        Message message = (Message) inputStream.readObject();
                        Platform.runLater(() -> output.setText(message.toString()));
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
}
