package TCPConnection;

import lora.LoraController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {
    Socket socket;
    OutputStream portOutputStream;
    InputStream inputStream;

    private byte[] buffer = new byte[256];
    String receivedMessageBuffer = "";

    public TCPConnection() {
        try {
            socket = new Socket("87.123.155.102", 45400);
            String adresse = "A5\r\n";
            socket.getOutputStream().write(adresse.getBytes(StandardCharsets.US_ASCII));
            portOutputStream = new ByteArrayOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUpTCPConnection() {
        new Thread(this::receiveLoop).start();
    }

    public void sendMessage(byte[] message) {
        try {
            socket.getOutputStream().write(77);
            socket.getOutputStream().write(message);
            socket.getOutputStream().write(13);
            socket.getOutputStream().write(10);

            socket.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String receiveMessageFromSocket() throws IOException {
        int index;
        while ((index = receivedMessageBuffer.indexOf("\r\n")) == -1) {
            int length = this.socket.getInputStream().read(buffer);

            if (length <= 0) {
                throw new IOException("Unexpected end of stream");
            }
            String chunk = new String(buffer, 0, length, StandardCharsets.US_ASCII);
            receivedMessageBuffer += chunk;
        }
        String command = receivedMessageBuffer.substring(0, index);
        receivedMessageBuffer = receivedMessageBuffer.substring(index + 2);
        System.out.println(command);
        return command;
    }

    private void receiveLoop() {
        try {
            while (true) {
                String receivedMessage = receiveMessageFromSocket();
                if (receivedMessage.startsWith("M")) {
                    String substring = receivedMessage.substring(1);
                    substring = substring + "\r\n";
                    LoraController.receivedMessage.add(substring);
                } else if (receivedMessage.startsWith("E")) {
                    System.out.println("ERROR: Address already in use!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OutputStream getPortOutputStream() {
        return portOutputStream;
    }
}
