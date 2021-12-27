package protocol;

import lora.LoraController;
import protocol.message.MSG;
import protocol.message.RERR;
import protocol.message.RREP;
import protocol.message.RREQ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ProtocolController {

    LoraController loraController;
    byte nodeAddress = 7;
    private byte sequenceNumber = 0;

    // Routing table of all valid routes
//    private HashMap<Integer, Route> routingTable = new HashMap<>();
//    private HashMap<Integer, Route> reverseRoutingTable = new HashMap<>();
    private List<Route> routingTable = new ArrayList<>();
    private List<ReverseRoute> reverseRoutingTable = new ArrayList<>();

//    public static Queue<String> receivedMessage = new LinkedList();

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public ProtocolController(LoraController loraController) {
        this.loraController = loraController;
        this.addSelfToRoutingTable();
    }


    public void startProtocolController() throws IOException {
        this.checkQueue();
        this.waitForInput();
    }

    private void addSelfToRoutingTable() {
        Route route = new Route((byte) 7, (byte) 7, null, (byte) 5, (byte) 1, true);
        routingTable.add(route);
    }

    private void checkQueue() {
        String decodedReceivedMessage = LoraController.receivedMessage.poll();
        if (decodedReceivedMessage == null) {
            return;
        } else {
            int indexOfLastComma = decodedReceivedMessage.indexOf(",", 8);

            if (indexOfLastComma == -1) {
                System.out.println("not a valid message");
                return;
            }
            //delete the first info about sender and character number
            decodedReceivedMessage = decodedReceivedMessage.substring(indexOfLastComma + 1);
            //delete new line at the end
            decodedReceivedMessage = decodedReceivedMessage.substring(0, decodedReceivedMessage.length() - 2);
            analyseMessage(decodedReceivedMessage);
        }
    }

    private void waitForInput() throws IOException {
        int x = 3; // wait 3 seconds at most
        byte destinationAddress;

//        System.out.println("you have 3 seconds to inter a command/message");
        System.out.println("you have 3 seconds to enter destination address ");
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < x * 1000 && !bufferedReader.ready()) {
        }

        if (bufferedReader.ready()) {
            System.out.println("processing sending message");
            this.handelInputToSendMessage();
        }
    }

    private void handelInputToSendMessage() throws IOException {
        int address = readAddress();
        String message;

        if (address == -1) {
            return;
        }

        System.out.println("you have 5 seconds to enter your message :");
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 5 * 1000 && !bufferedReader.ready()) {
        }

        if (bufferedReader.ready()) {
            message = bufferedReader.readLine();
        } else {
            System.out.println("timeout wait for the next cycle");
            return;
        }

        Route route = checkRoutingTable((byte) address);

        if (route == null) {
            RREQ routeRequest = new RREQ((byte) 0, (byte) 0, (byte) 0, (byte) 1,
                    (byte) address, (byte) 0, (byte) 0, this.nodeAddress, (byte) 10);
            broadCastRouteRequest(createRouteRequest());
        } else if (!route.isValid()) {
            broadCastRouteRequest(createRouteRequest());
        }

        MSG messagePacket = new MSG((byte) 48, (byte) 2, (byte) 15, (byte) address, (byte) 100, (byte) 4, message);
        String decodedMessage = Base64.getEncoder().withoutPadding().encodeToString(messagePacket.toMessage());
        byte[] messageBytes = decodedMessage.getBytes();

        try {
            loraController.sendMessage(messageBytes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int readAddress() throws IOException {
        int addressInt;
        String address = bufferedReader.readLine();

        try {
            addressInt = Integer.parseInt(address);
            return addressInt;
        } catch (NumberFormatException numberFormatException) {
            System.out.println("you didn't enter a valid address, the address needs to be a number");
            return -1;
        }
    }

    private void analyseMessage(String decodedReceivedMessage) {
        try {
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
                    handleMSG(decodedBytes);
                    break;
                default:
                    // the message is not valid
                    break;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            System.out.println("the string cannot be decoded with base64");
        }
    }

    private void handleRouteRequest(byte[] decodedBytes) {
        RREQ routeRequest = new RREQ(decodedBytes[0], decodedBytes[1], decodedBytes[2], decodedBytes[3],
                decodedBytes[4], decodedBytes[5], decodedBytes[6], decodedBytes[7], decodedBytes[8]);

        Route route = checkRoutingTable(decodedBytes[4]);

        if (route == null) {
            routeRequest.setHopCount((byte) (routeRequest.getHopCount() + 1));  //increase hop count
            writeToReversRoutingTable(routeRequest);
            routeRequest.setTTL((byte) (routeRequest.getTTL() - 1));
            broadCastRouteRequest(routeRequest);
        } else if (!route.isValid()) {
            //todo: react to invalid route ?
        } else {
            sendRouteReply(routeRequest);
        }

    }

    private void handleRouteReply(byte[] decodedBytes) {
        if (decodedBytes[1] == nodeAddress) {
            RREP routeReply = new RREP(decodedBytes[0], decodedBytes[1], decodedBytes[2], decodedBytes[3], decodedBytes[4],
                    decodedBytes[5], decodedBytes[6], decodedBytes[7], decodedBytes[8]);
            updateRoutingTable(routeReply);

            // I think this should never be null!! not sure tho
            ReverseRoute reverseRoute = checkReverseRoutingTable(decodedBytes[4]);
            createRouteReply(reverseRoute, routeReply);
        }
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

    private void handleMSG(byte[] decodedBytes) {
        byte[] messageByte = new byte[decodedBytes.length - 6];

        for (int i = 0; i < decodedBytes.length - 6; i++) {
            messageByte[i] = decodedBytes[i + 6];
        }

        String messageString = new String(messageByte);

        MSG message = new MSG(decodedBytes[0], decodedBytes[1], decodedBytes[2], decodedBytes[3], decodedBytes[4],
                decodedBytes[5], messageString);
    }

    private Route checkRoutingTable(byte destinationAddress) {
        return routingTable.stream()
                .filter(route -> route.getDestinationAddress() == destinationAddress)
                .findAny()
                .orElse(null);
    }

    private ReverseRoute checkReverseRoutingTable(byte destinationAddress) {
        return reverseRoutingTable.stream()
                .filter(reverseRoute -> reverseRoute.getDestination() == destinationAddress)
                .findAny()
                .orElse(null);
    }

    private void writeToReversRoutingTable(RREQ routeRequest) {
        ReverseRoute reverseRoute = new ReverseRoute(routeRequest.getDestinationAddress(),
                routeRequest.getOriginatorAddress(), routeRequest.getRequestId(), routeRequest.getHopCount(),
                routeRequest.getPrevHopAddress());
        reverseRoutingTable.add(reverseRoute);
        System.out.println("added to reverse routing table");
    }

    private void broadCastRouteRequest(RREQ routeRequest) {
        byte[] routeRequestBytes = routeRequest.toMessage();

        try {
            if (!loraController.sendMessage(routeRequestBytes)) {
                System.out.println("sending route request failed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //todo: not sure about the fields

    private void sendRouteReply(RREQ routeRequest) {
        RREP routeReplay = new RREP((byte) 16, routeRequest.getPrevHopAddress(), nodeAddress,
                routeRequest.getRequestId(), (byte) 0, routeRequest.getDestinationSequence(),
                (byte) 0, routeRequest.getOriginatorAddress(), routeRequest.getTTL());

        byte[] routeReplyByte = routeReplay.toMessage();

        try {
            loraController.sendMessage(routeReplyByte);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //todo: check the parameter of the route

    private void updateRoutingTable(RREP routeReply) {
        Route route1 = checkRoutingTable(routeReply.getOriginatorAddress());
        if (route1 == null) {
            Route route = new Route(routeReply.getOriginatorAddress(), routeReply.getPrevHopAddress(),
                    routeReply.getHopCount(), routeReply.getDestinationSequence(), true);
        }

        routingTable.add(route1);
    }

    private void createRouteReply(ReverseRoute reverseRoute, RREP routeReply) {

    }


    private RREQ createRouteRequest() {
        return null;
    }
}
