package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Handler implements Runnable{

    private String dir = "server_dir";
    private final byte[] buffer;
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public Handler(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        buffer = new byte[1024];
    }

    @Override
    public void run() {

        try {
            while(true){
                String fileName = inputStream.readUTF();
                long size = inputStream.readLong();
                try(FileOutputStream fileOutputStream = new FileOutputStream(dir + "/" + fileName)) {
                    for (int i = 0; i <(size + 1023) / 1024; i++) {
                        int read = inputStream.read(buffer);
                        fileOutputStream.write(buffer, 0, read);
                    }
                }
                outputStream.writeUTF("File: " + fileName + " success");
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
