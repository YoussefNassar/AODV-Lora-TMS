package protocol;

import lora.LoraController;
import protocol.message.MSG;
import protocol.message.RERR;
import protocol.message.RREP;
import protocol.message.RREQ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class ProtocolController {

    // Routing table of all valid routes
    private HashMap<Integer, Route> routingTable = new HashMap<>();
    private HashMap<Integer, Route> reverseRoutingTable = new HashMap<>();

    public static Queue<String> receivedMessage = new LinkedList();

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public void startProtocolController() throws IOException {
        System.out.println("begin protocol...");

        this.checkQueue();
        this.waitForInput();
    }

    private void checkQueue() {
        String decodedReceivedMessage = receivedMessage.poll();
        if (decodedReceivedMessage == null) {
            return;
        } else {
            analyseMessage(decodedReceivedMessage);
        }
    }

    private void waitForInput() throws IOException {
        int x = 3; // wait 3 seconds at most

        System.out.println("you have 3 seconds to inter a command/message");
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < x * 1000 && !bufferedReader.ready()) {
        }

        if (bufferedReader.ready()) {
            System.out.println("processing sending message");
            this.sendMessage();
        }
    }

    private void sendMessage() throws IOException {
        String message = bufferedReader.readLine();
        MSG messagePacket = new MSG((byte) 48, (byte) 2, (byte) 15, (byte) 4, (byte) 100, (byte) 4, message);
        String decodedMessage = Base64.getEncoder().withoutPadding().encodeToString(messagePacket.toMessage());
        byte[] messageBytes = decodedMessage.getBytes();
        LoraController.portOutputStream.write(messageBytes);
        LoraController.portOutputStream.flush();
    }

    private void analyseMessage(String decodedReceivedMessage) {
        byte[] decodedBytes = Base64.getDecoder().decode(decodedReceivedMessage);
        switch (decodedBytes[0]) {
            case 0:
                //route request
                handleRouteRequest(decodedBytes);
                break;
            case 16:
                //route reply
                handleRouteReply(decodedBytes);
                break;
            case 32:
                //route error
                handleRouteError(decodedBytes);
                break;
            case 48:
                //message
                handleMessage(decodedBytes);
                break;
            default:
                // the message is not valid
                break;
        }
    }

    private void handleRouteRequest(byte[] decodedBytes) {
        RREQ routeRequest = new RREQ(decodedBytes[0], decodedBytes[1], decodedBytes[2], decodedBytes[3],
                decodedBytes[4], decodedBytes[5], decodedBytes[6], decodedBytes[7], decodedBytes[8]);


    }

    private void handleRouteReply(byte[] decodedBytes) {
        RREP routeReply = new RREP(decodedBytes[0], decodedBytes[1], decodedBytes[2], decodedBytes[3], decodedBytes[4],
                decodedBytes[5], decodedBytes[6], decodedBytes[7], decodedBytes[8]);
    }

    private void handleRouteError(byte[] decodedBytes) {
        RERR error = new RERR(decodedBytes[0], decodedBytes[1], decodedBytes[2], decodedBytes[3]);

        for (int i = 4; i < decodedBytes.length; i += 2) {
            for (int j = 5; j < decodedBytes.length; j += 2) {
                RoutePath routePath = new RoutePath(decodedBytes[i], decodedBytes[j]);
                error.addPath(routePath);
            }
        }
    }

    private void handleMessage(byte[] decodedBytes) {
        byte[] messageByte = new byte[decodedBytes.length - 6];

        for (int i = 0; i < decodedBytes.length - 6; i++) {
            messageByte[i] = decodedBytes[i+6];
        }

        String messageString = new String(messageByte);

        MSG message = new MSG(decodedBytes[0], decodedBytes[1], decodedBytes[2], decodedBytes[3], decodedBytes[4],
                decodedBytes[5], messageString);
    }
}
