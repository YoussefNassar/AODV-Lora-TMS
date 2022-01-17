package lora;

import TCPConnection.TCPConnection;
import com.fazecast.jSerialComm.SerialPort;
import lora.exception.SetupException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class LoraController {

    public static Queue<String> receivedMessage = new LinkedList<>();
    public static OutputStream portOutputStream;
    private static SerialPort port;

    TCPConnection tcpConnection;

    String command = "";

    int retry = 0;

    public void setUpBluetoothConnection() {
        SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();

        if (port == null) {
            for (SerialPort serialPort : allAvailableComPorts) {
                System.out.println("List of all available serial ports: " + serialPort.getDescriptivePortName());

                //always take the outgoing port
                if (serialPort.getDescriptivePortName().contains("COM8")) {
                    port = serialPort;
                }
            }
        }
        if (port == null) {
            System.out.println("port was not found");
            return;
        }

        if (!port.isOpen()) {
            port.openPort();
        }

        //check the name
        System.out.println("Opened port: " + port.getDescriptivePortName());

        portOutputStream = port.getOutputStream();

        CommandListener listener = new CommandListener();
        port.addDataListener(listener);
    }

    public void setTCPConnection() {
        tcpConnection = new TCPConnection();
        tcpConnection.setUpTCPConnection();
        portOutputStream = tcpConnection.getPortOutputStream();
    }

    public void setUpTheModule() throws InterruptedException {
        try {
            testAt();
            atSetAddress();
            atSetConfiguration();
            sendRandomMessage();
        } catch (SetupException e) {
            System.out.println("SetupException was thrown");
        }
        System.out.println("setup completed");
    }

    private void testAt() throws InterruptedException, SetupException {
        String at = LoraCommand.AT.CODE;
        String command = at + "\r\n";
        if (!this.sendCommandAndCheckReply(command, LoraCommand.REPLY_OK) && retry < 4) {
            System.out.println("testAt() failed");
            System.out.println("retry");
            retry++;
            testAt();
        } else if (retry == 4) {
            System.out.println("testAt failed 3 times...");
            retry = 0;
            throw new SetupException();
        }
        retry = 0;
        System.out.println("testAt worked");
    }

    private void atSetAddress() throws InterruptedException, SetupException {
        String setAddress = LoraCommand.AT_ADDR_SET.CODE + "07";
        String command = setAddress + "\r\n";
        if (!this.sendCommandAndCheckReply(command, LoraCommand.REPLY_OK) && retry < 4) {
            System.out.println("atSetAddress() failed");
            System.out.println("retry");
            retry++;
            atSetAddress();
        } else if (retry == 4) {
            System.out.println("atSetAddress failed 3 times...");
            retry = 0;
            throw new SetupException();
        }
        retry = 0;
        System.out.println("atSetAddress worked");
    }

    private void atSetConfiguration() throws InterruptedException, SetupException {
        String reset = LoraCommand.AT_CFG.CODE;
        //String command = reset + "433000000,5,6,12,4,1,0,0,0,0,3000,8,8\r\n";
        String command = reset + "433920000,5,6,12,4,1,0,0,0,0,3000,8,8\r\n";
        //String command = reset + "434920000,5,6,12,4,1,0,0,0,0,3000,8,8\r\n"; the last i used
        //String command = reset + "433920000,5,6,12,4,1,0,0,0,0,3000,8,8\r\n";
        if (!this.sendCommandAndCheckReply(command, LoraCommand.REPLY_OK) && retry < 4) {
            System.out.println("atSetConfiguration() failed");
            System.out.println("retry");
            retry++;
            atSetConfiguration();
        } else if (retry == 4) {
            System.out.println("atSetConfiguration failed 3 times...");
            retry = 0;
            throw new SetupException();
        }
        retry = 0;
        System.out.println("atSetConfiguration worked");
    }

    private void sendRandomMessage() throws SetupException, InterruptedException {
        sendAtSend();
        sendFirstMessage();
    }

    private void sendAtSend() throws InterruptedException, SetupException {
        String at = LoraCommand.AT_SEND.CODE + "17";
        String command = at + "\r\n";
        if (!this.sendCommandAndCheckReply(command, LoraCommand.REPLY_OK) && retry < 4) {
            System.out.println("sendAtSend failed");
            System.out.println("retry");
            retry++;
            sendRandomMessage();
        } else if (retry == 4) {
            System.out.println("sendAtSend failed 3 times...");
            retry = 0;
            throw new SetupException();
        }
        retry = 0;
        System.out.println("sendAtSend worked");
    }

    private void sendFirstMessage() throws InterruptedException, SetupException {
        String at = "hello from Node 7";
        String command = at + "\r\n";
        if (!this.sendCommandAndCheckReply(command, LoraCommand.REPLY_SENDING) && retry < 4) {
            System.out.println("sendFirstMessage failed");
            System.out.println("retry");
            retry++;
            sendRandomMessage();
        } else if (retry == 4) {
            System.out.println("sendFirstMessage failed 3 times...");
            retry = 0;
            throw new SetupException();
        }
        retry = 0;
        System.out.println("sendFirstMessage worked");
        receivedMessage.poll();
    }



    public boolean sendCommandAndCheckReply(String command, LoraCommand expectedLoraReply) throws InterruptedException {
        byte[] commandByte = command.getBytes();
        try {
            portOutputStream.write(commandByte);
            portOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread.sleep(4000);
        String receivedMessage = LoraController.receivedMessage.poll();

        if (receivedMessage == null) {
            System.out.println("nothing in the queue");
            return true;
        }

        return true;

//        //remove the new line at the end
//        receivedMessage = receivedMessage.substring(0, receivedMessage.length()-2);
//
//        if (!checkReplyCode(LoraCommand.valueOfCode(receivedMessage), expectedLoraReply)) {  //&& retry < 4) {
//            System.out.println("wrong response: " + expectedLoraReply.CODE);
//            return true;
////            sendCommandAndCheckReply(command, expectedLoraReply);
//        } else {
//            System.out.println("success: " + expectedLoraReply.CODE);
//            return true;
//        }
    }

    private boolean checkReplyCode(LoraCommand actualReplyCode, LoraCommand expectedReplyCode) {
        return actualReplyCode.equals(expectedReplyCode);
    }

    // this was called sendRouteRequestMessage()
    public boolean sendMessage(byte[] messageBytes) throws InterruptedException {
        String command = LoraCommand.AT_SEND.CODE + messageBytes.length + "\r\n";
        byte[] commandByte = command.getBytes();
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(3000, 6000 + 1));
            portOutputStream.write(commandByte);
            portOutputStream.flush();
            Thread.sleep(3000);
            portOutputStream.write(messageBytes);
            portOutputStream.flush();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Thread.sleep(3000);

        //tcpConnection.sendMessage(messageBytes);

        //String receivedMessage = LoraController.receivedMessage.poll();

        if (receivedMessage == null) {
            System.out.println("nothing in the queue");
            return true;
        }

        return true;
    }


    //this is used when you want to test if the connection is working fine with the module
    public void testConnectionInLab() {
        command = "AT+ADDR?" + "\r\n";
        byte[] commandByte = command.getBytes();
        try {
            portOutputStream.write(commandByte);
            portOutputStream.flush();
            Thread.sleep(4000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        ///////////////////////////////////////////////
        command = "AT+CFG=433000000,5,6,12,4,1,0,0,0,0,3000,8,8" + "\r\n";
        commandByte = command.getBytes();
        try {
            portOutputStream.write(commandByte);
            portOutputStream.flush();
            Thread.sleep(4000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        //////////////////////////////////
        command = "AT+SEND=8" + "\r\n";
        commandByte = command.getBytes();
        try {
            portOutputStream.write(commandByte);
            portOutputStream.flush();
            Thread.sleep(4000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        //////////////////////////////////
        command = "jo jo jo" + "\r\n";
        commandByte = command.getBytes();
        try {
            portOutputStream.write(commandByte);
            portOutputStream.flush();
            Thread.sleep(7000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
