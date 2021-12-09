package lora;

import com.fazecast.jSerialComm.SerialPort;
import lora.exception.SetupException;

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

    int retry = 0;

    public void setUpBluetoothConnection() {
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
    }

    public void setUpTheModule() throws InterruptedException {
        try {
            testAt();
            atSetAddress();
            atSetConfiguration();
        } catch (SetupException e) {
            System.out.println("setup module failed");
            System.exit(0);
        }
    }

    private void testAt() throws InterruptedException, SetupException {
        String at = LoraCommand.AT.CODE;
        String command = at + "\r\n";
        if (this.sendCommandAndCheckReply(command, LoraCommand.REPLY_OK) && retry < 4) {
            System.out.println("testAt() failed");
            System.out.println("retry");
            retry++;
            testAt();
        } else if (retry == 4) {
            System.out.println("testAt failed 3 times...");
            retry = 0;
            throw new SetupException();
        }
    }

    private void atSetAddress() throws InterruptedException, SetupException {
        String setAddress = LoraCommand.AT_ADDR_SET.CODE + "07";
        String command = setAddress + "\r\n";
        if (this.sendCommandAndCheckReply(command, LoraCommand.REPLY_OK) && retry < 4) {
            System.out.println("atSetAddress() failed");
            System.out.println("retry");
            retry++;
            atSetAddress();
        } else if (retry == 4) {
            System.out.println("atSetAddress failed 3 times...");
            retry = 0;
            throw new SetupException();
        }
    }

    private void atSetConfiguration() throws InterruptedException, SetupException {
        String reset = LoraCommand.AT_CFG.CODE;
        String command = reset + "433000000,5,6,12,4,1,0,0,0,0,3000,8,8\r\n";
        if (this.sendCommandAndCheckReply(command, LoraCommand.REPLY_OK) && retry < 4) {
            System.out.println("atSetConfiguration() failed");
            System.out.println("retry");
            retry++;
            atSetConfiguration();
        } else if (retry == 4) {
            System.out.println("atSetConfiguration failed 3 times...");
            retry = 0;
            throw new SetupException();
        }
    }

    private boolean sendCommandAndCheckReply(String command, LoraCommand expectedLoraReply) throws InterruptedException {
        byte[] commandByte = command.getBytes();
        try {
            portOutputStream.write(commandByte);
            portOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread.sleep(4000);
        //todo: recheck this logic
        String receivedMessage = LoraController.receivedMessage.poll();

        if (receivedMessage == null) {
            System.out.println("nothing in the queue");
            return false;
        }

        if (!checkReplyCode(LoraCommand.valueOfCode(receivedMessage), expectedLoraReply)) {  //&& retry < 4) {
            System.out.println("wrong response: " + expectedLoraReply.CODE);
            return false;
//            sendCommandAndCheckReply(command, expectedLoraReply);
        } else {
            System.out.println("success: " + expectedLoraReply.CODE);
            return true;
        }
    }

    private boolean checkReplyCode(LoraCommand actualReplyCode, LoraCommand expectedReplyCode) {
        return actualReplyCode.equals(expectedReplyCode);
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
