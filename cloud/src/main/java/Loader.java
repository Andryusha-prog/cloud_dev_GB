import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import jdk.jfr.internal.PlatformEventType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class Loader implements Initializable {
    public ListView<String> listView;
    public Label output;
    public ListView<String> listView1;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    public void sendClick(ActionEvent actionEvent) throws IOException {
        String fileName = listView.getSelectionModel().getSelectedItem();
        File file = new File("fileDir/" + fileName);
        long size = file.length();
        outputStream.writeUTF(fileName);
        outputStream.writeLong(size);
        Files.copy(file.toPath(), outputStream);
        output.setText("File: " + fileName + " , was sending!");

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            Socket socket = new Socket("localhost", 8080);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            File dir = new File("fileDir");
            listView.getItems().addAll(dir.list());
            File server_dir = new File("server_dir");
            listView1.getItems().addAll(server_dir.list());
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String status = inputStream.readUTF();
                        Platform.runLater(() -> output.setText(status));
                        System.out.println(status);
                    }
                } catch (Exception e)
                {
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
