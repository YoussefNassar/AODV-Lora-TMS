package lora;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

public class LoraController {

    public static Queue<String> receivedMessage = new LinkedList(); //todo: read about raw type (the warning)
    private static SerialPort port;
    InputStream portInputStream;
    OutputStream portOutputStream;

    String command = "";

    public void setUpCommunication() {
        SerialPort[] allAvailableComPorts = SerialPort.getCommPorts();

        if (port == null) {
            for (SerialPort serialPort : allAvailableComPorts) {
                System.out.println("List of all available serial ports: " + serialPort.getDescriptivePortName());

                //always take the outgoing port
                if (serialPort.getDescriptivePortName().contains("COM18")) {
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


        portInputStream = port.getInputStream();
        portOutputStream = port.getOutputStream();

        CommandListener listener = new CommandListener();
        port.addDataListener(listener);

        //to clean the input stream
        //portInputStream.skip(portInputStream.available());
//        command = command + "\r\n";
//        byte[] commandByte = command.getBytes();
//        portOutputStream.write(commandByte);
//        portOutputStream.flush();
//        Thread.sleep(1000);
//        System.out.println("ready");

    }

    public void setUpTheModule() throws InterruptedException {
        atSendAt();
        //atRST();
        //atSetConfiguration();
    }

    private void atSendAt() throws InterruptedException {
        String reset = LoraCommand.AT.CODE;
        String command = reset + "\r\n";
        this.sendCommandAndCheckReply(command, LoraCommand.REPLY_OK);
    }

    private void atRST() throws InterruptedException {
        String reset = LoraCommand.AT_RST.CODE;
        String command = reset + "\r\n";
        sendCommandAndCheckReply(command, LoraCommand.REPLY_OK);
    }

    private void atSetAddress() throws InterruptedException {
        String setAddress = LoraCommand.AT_ADDR_SET.CODE + "07";
        String command = setAddress + "\r\n";
        sendCommandAndCheckReply(command, LoraCommand.REPLY_OK);
    }

    private void atSetConfiguration() throws InterruptedException {
        String reset = LoraCommand.AT_CFG.CODE;
        String command = reset + "433000000,5,6,12,4,1,0,0,0,0,3000,8,8\r\n";
        sendCommandAndCheckReply(command, LoraCommand.REPLY_OK);
    }

    private void sendCommandAndCheckReply(String command, LoraCommand expectedLoraReply) throws InterruptedException {
        byte[] commandByte = command.getBytes();
        try {
            portOutputStream.write(commandByte);
            portOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread.sleep(4000);
        String receivedMessage = LoraController.receivedMessage.poll();
        checkReplyCode(LoraCommand.valueOfCode(receivedMessage), LoraCommand.REPLY_OK);
    }

    private boolean checkReplyCode(LoraCommand actualReplyCode, LoraCommand expectedReplyCode) {
        return actualReplyCode.equals(expectedReplyCode);
    }

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
