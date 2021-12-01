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

    public void setUpTheModule() {
        atRST();
    }

    private void atRST() {
        String reset = LoraCommand.AT_RST.CODE;
        String command = reset + "\r\n";
        byte[] commandByte = command.getBytes();
        try {
            portOutputStream.write(commandByte);
            portOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String receivedMessage = LoraController.receivedMessage.poll();
        //checkReplyCode()//todo: react to the reply
    }


    private void atSetAddress() throws InterruptedException {
        String setAddress = LoraCommand.AT_ADDR_SET.CODE + "07";
        String command = setAddress + "\r\n";
        byte[] commandByte = command.getBytes();
        try {
            portOutputStream.write(commandByte);
            portOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //checkReplyCode(Lora.valueOfCode(replyQueue.take()), Lora.REPLY_OK);
    }

    private boolean checkReplyCode(LoraCommand actualReplyCode, LoraCommand expectedReplyCode) {
        return true;
    }
}
