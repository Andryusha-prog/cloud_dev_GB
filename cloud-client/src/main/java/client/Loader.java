package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Loader implements Initializable {

    private Path currentClientDir;
    private String LoginPath;
    public ListView<String> listView;//clientView
    public Label output;
    public Label outputReg;
    public ListView<String> listView1;//SererView
    public TextField clientPath;
    public TextField serverPath;
    public TextField loginReg;
    public PasswordField passwordReg;
    public TextField loginAut;
    public PasswordField passwordAut;
    private ObjectEncoderOutputStream outputStream;
    private ObjectDecoderInputStream inputStream;
    //invisible variable
    public Label text;
    public TextField nameFile;
    public Button ok;
    public Button cancel;
    public Button butCancelReg;
    public Button btnEnter, btnSignUp;
    private short clientCommand = 0;
    private short spaceVar = 0;
    private boolean autorizedClient = false;

    public void sendClick(ActionEvent actionEvent) throws IOException {
        String filename = listView.getSelectionModel().getSelectedItem();
        outputStream.writeObject(new FileMessage(Paths.get(String.valueOf(currentClientDir.resolve(filename)))));
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
            Socket socket = new Socket("localhost", 8181);
            outputStream = new ObjectEncoderOutputStream(socket.getOutputStream());
            inputStream = new ObjectDecoderInputStream(socket.getInputStream());

            if(autorizedClient) {
                outputStream.writeObject(new ListRequest());
                refreshClientList();
                addNavigationListener();
                checkSpace();
            }
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
                                Files.write(Paths.get(String.valueOf(currentClientDir.resolve(message.getName()))), message.getData());
                                refreshClientList();
                                break;
                            case PATH_RESPONSE:
                                PathUpResponse pathResponse = (PathUpResponse) command;
                                String path = pathResponse.getPath();
                                Platform.runLater(() -> serverPath.setText(path));
                                break;
                            case REGISTRATION_RESPONSE:
                                RegistrationResponse regresponce = (RegistrationResponse) command;
                                String mess = regresponce.getMessage();
                                if (mess.equals("successfully")) {
                                    refreshOutputReg("Registration " + mess);
                                }
                                break;
                            case AUTORIZATION_RESPONSE:
                                AutorizationResponse autResp = (AutorizationResponse) command;
                                autorizedClient = autResp.isResult();
                                LoginPath = autResp.getLogin();
                                if (autorizedClient) {
                                    Platform.runLater(() -> {
                                        try {
                                            Parent newScene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("window.fxml")));
                                            Stage window = (Stage) btnEnter.getScene().getWindow();
                                            window.setScene(new Scene(newScene));
                                            refreshAll();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });

                                }

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

    private void refreshAll() throws IOException {
        currentClientDir = Paths.get("fileDir/" + LoginPath).toAbsolutePath();
        if (Files.notExists(currentClientDir)) Files.createDirectory(currentClientDir);
        outputStream.writeObject(new ListRequest());

        refreshClientList();
        addNavigationListener();
        checkSpace();

    }

    public void refreshOutputReg(String message) {
        Platform.runLater(() -> {
            outputReg.setText(message);
            outputReg.setVisible(true);
        });

    }


    public void pressReg(ActionEvent actionEvent) throws IOException {
        outputStream.writeObject(new RegistrationRequest(loginReg.getText(), passwordReg.getText()));
        outputStream.flush();
    }

    public void cancelReg(ActionEvent actionEvent) throws IOException {
        Parent registerScene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("StartWindow.fxml")));
        Stage window = (Stage) butCancelReg.getScene().getWindow();
        window.setScene(new Scene(registerScene));
    }

    public void pressEnter(ActionEvent actionEvent) throws IOException {
        outputStream.writeObject(new AutorizationRequest(loginAut.getText(), passwordAut.getText()));
        outputStream.flush();
    }

    public void pressSignUp(ActionEvent actionEvent) throws IOException {
        Parent registerScene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("RegisterWindow.fxml")));
        Stage window = (Stage) btnSignUp.getScene().getWindow();
        window.setScene(new Scene(registerScene));
    }

    private void checkSpace() { //Проверяю, какая из областей является активной
        listView.setOnMousePressed(e -> {
            spaceVar = 1;
        });
        listView1.setOnMouseEntered(e -> {
            spaceVar = 2;
        });
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
            if (e.getClickCount() == 2) {
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
            if (e.getClickCount() == 2) {
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

    public void delFile() throws IOException {
        if (spaceVar == 1) {
            File file = new File(String.valueOf(currentClientDir.resolve(listView.getSelectionModel().getSelectedItem())));
            if (file.delete()) {
                output.setText("File was deleted");
            } else {
                output.setText("File not delete");
            }
            refreshClientList();
        } else if (spaceVar == 2) {
            String filename = listView1.getSelectionModel().getSelectedItem();
            outputStream.writeObject(new FileDeleteRequest(filename));
            outputStream.flush();
        }
    }

    public void renameFile() {
        clientCommand = 1;
        visibleFieldName();
    }

    public void makeDir() {
        clientCommand = 2;
        visibleFieldName();
    }

    public void makeFile() {
        clientCommand = 3;
        visibleFieldName();
    }

    public void okButton(ActionEvent actionEvent) throws IOException {

        if (spaceVar == 1) { //Проверяю, какая из областей была выделена, чтоб в ней проводить операции
            switch (clientCommand) {
                case 1:
                    File file = new File(String.valueOf(currentClientDir.resolve(listView.getSelectionModel().getSelectedItem())));
                    File newFile = new File(String.valueOf(currentClientDir.resolve(nameFile.getText())));
                    if (file.renameTo(newFile)) {
                        output.setText("File rename!");
                    } else output.setText("file not rename");
                    break;
                case 2:
                    if (Files.notExists(Paths.get(String.valueOf(currentClientDir.resolve(nameFile.getText()))))) {
                        Files.createDirectory(Paths.get(String.valueOf(currentClientDir.resolve(nameFile.getText()))));
                    } else {
                        output.setText("directory with the same name already exists! %n");
                    }
                    break;
                case 3:
                    if (Files.notExists(Paths.get(String.valueOf(currentClientDir.resolve(nameFile.getText()))))) {
                        Files.write(Paths.get(String.valueOf(currentClientDir.resolve(nameFile.getText()))), "".getBytes(StandardCharsets.UTF_8));
                    } else {
                        output.setText("File with the same name already exists! %n");
                    }
                    break;
            }
        } else if (spaceVar == 2) {
            switch (clientCommand) {
                case 1:
                    String filename = listView1.getSelectionModel().getSelectedItem();
                    outputStream.writeObject(new FileRenameRequest(filename, nameFile.getText()));
                    outputStream.flush();
                    break;
                case 2:
                    outputStream.writeObject(new NewPathRequest(nameFile.getText()));
                    outputStream.flush();
                    break;
                case 3:
                    outputStream.writeObject(new NewFileRequest(nameFile.getText()));
                    outputStream.flush();
                    break;
            }
        }
        nameFile.clear();
        invisibleFieldName();
        refreshClientList();
    }

    public void cancelButton() {
        nameFile.clear();
        invisibleFieldName();
    }

    private void visibleFieldName() {
        text.setVisible(true);
        nameFile.setVisible(true);
        ok.setVisible(true);
        cancel.setVisible(true);
    }

    private void invisibleFieldName() {
        text.setVisible(false);
        nameFile.setVisible(false);
        ok.setVisible(false);
        cancel.setVisible(false);
    }
}
