package protocol;

import lora.LoraController;
import org.jetbrains.annotations.NotNull;
import protocol.message.ACK;
import protocol.message.MSG;
import protocol.message.RERR;
import protocol.message.RREP;
import protocol.message.RREQ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProtocolController {

    LoraController loraController;
    byte nodeAddress = 7;
    private byte sequenceNumber = 1;
    private byte requestId = 0;
    private List<Route> routingTable = new ArrayList<>();
    private List<ReverseRoute> reverseRoutingTable = new ArrayList<>();

    private boolean waitingForAcknowledgement = false;
    private int acknowledgmentReties = 0;
    private long startTimeWaitingForAcknowledgment;
    private byte[] savedMessageForRetries;

    private boolean waitingForRouteReply = false;
    private int routeRequestRetries = 0;
    private long startTimeWaitingForRouteResponse;
    private RREQ savedRouteRequestForRetries;

    Map<Integer, Integer> routesRequestEchoCheck = new HashMap<>();
    Map<Integer, Integer> MessageEchoCheck = new HashMap<>();



    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    private String savedMessageAfterRouteRequest;

    public ProtocolController(LoraController loraController) {
        this.loraController = loraController;
        this.addSelfToRoutingTable();
    }


    private void addSelfToRoutingTable() {
        Route route = new Route(this.nodeAddress, this.nodeAddress, null, (byte) 0, (byte) 0, true);
        routingTable.add(route);
    }

    public void startProtocolController() throws IOException, InterruptedException {
        this.checkQueue();
        this.waitForInput();
        this.checkWaitedAcknowledgment();
        this.checkWaitedRouteReply();
    }

    private void checkWaitedAcknowledgment() throws InterruptedException {
        if (this.waitingForAcknowledgement && this.acknowledgmentReties < 3 &&
                (this.startTimeWaitingForAcknowledgment - System.currentTimeMillis() < 120000)) {
            this.startTimeWaitingForAcknowledgment = System.currentTimeMillis();
            this.acknowledgmentReties++;
            System.out.println("no acknowledgment yet");
            System.out.println("resend message");
            sendMessagePacket(this.savedMessageForRetries);
        } else if (this.waitingForAcknowledgement && this.acknowledgmentReties == 3 &&
                (this.startTimeWaitingForAcknowledgment - System.currentTimeMillis() < 120000)) {
            System.out.println("send message 3 times and no acknowledgement was received");
            this.waitingForAcknowledgement = false;
            this.acknowledgmentReties = 0;
            //start route errors
            List<Route> allRoutesWithUnreachableHopAddress = findUnreachableRoutes(this.savedMessageForRetries[1]);
            invalidRoutes(allRoutesWithUnreachableHopAddress);
            sendRouteErrors(allRoutesWithUnreachableHopAddress);
        }
    }

    private List<Route> findUnreachableRoutes(byte unreachableHopAddress) {
        return getAllRoutesWithHopAddress(unreachableHopAddress);
    }

    private void invalidRoutes(List<Route> allRoutesWithUnreachableHopAddress) {
        allRoutesWithUnreachableHopAddress.forEach(route -> route.setValid(false));
    }

    private void sendRouteErrors(List<Route> allRoutesWithUnreachableHopAddress) {
        List<RoutePath> routePaths = new ArrayList<>();
        List<Byte> precursors = new ArrayList<>();
        int pathCount = allRoutesWithUnreachableHopAddress.size();

        //get all precursors
        for (Route route : allRoutesWithUnreachableHopAddress) {
            for (Byte precursor : route.getPrecursors()) {
                if (!(precursors.contains(precursor))) {
                    precursors.add(precursor);
                }
            }
        }

        //remove duplicates
        precursors = precursors.stream()
                .distinct()
                .collect(Collectors.toList());

        for (Route route : allRoutesWithUnreachableHopAddress) {
            RoutePath routePath = new RoutePath(route.getDestinationAddress(), route.getDestinationSequenceNumber());
            routePaths.add(routePath);
        }

        for (Byte precursor : precursors) {
            sendRouteErrorMessage(precursor, pathCount, routePaths);
        }
    }

    private void sendRouteErrorMessage(Byte precursor, int pathCount, List<RoutePath> destinationAddresses) {
        RERR routeError = new RERR((byte) 32, precursor, this.nodeAddress, (byte) pathCount, destinationAddresses);
        byte[] routeErrorBytes = routeError.toMessage();
        try {
            loraController.sendMessage(routeErrorBytes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkWaitedRouteReply() {
        if (this.waitingForRouteReply && this.routeRequestRetries < 3 &&
                (this.startTimeWaitingForRouteResponse - System.currentTimeMillis() < 300000)) {
            this.startTimeWaitingForRouteResponse = System.currentTimeMillis();
            this.routeRequestRetries++;
            System.out.println("no route response yet");
            System.out.println("resending route request");
            this.requestId++;
            this.sequenceNumber++;
            this.savedRouteRequestForRetries.setRequestId(this.requestId);
            this.savedRouteRequestForRetries.setDestinationSequence(this.sequenceNumber);
            broadCastRouteRequest(this.savedRouteRequestForRetries);
        } else if (this.waitingForRouteReply && this.routeRequestRetries == 3) {
            System.out.println("send route request 3 times and no route response was received");
            this.waitingForRouteReply = false;
            this.routeRequestRetries = 0;
        }
    }

    private void checkQueue() {
        String decodedReceivedMessage = LoraController.receivedMessage.poll();
        if (decodedReceivedMessage != null) {
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
        int x = 5; // wait 5 seconds at most

        System.out.println("you have 5 seconds to enter destination address ");
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < x * 1000 && !bufferedReader.ready()) {
        }

        if (bufferedReader.ready()) {
            System.out.println("processing sending message");
            this.handelInputToSendMessage();
        }
    }

    private void handelInputToSendMessage() throws IOException {
        int destinationAddress = readAddress();
        String message;

        if (destinationAddress == -1) {
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

        Route route = checkRoutingTable((byte) destinationAddress);

        if (route == null || !route.isValid()) {
            //no route found in the routing table, creating a route request...
            RREQ routeRequest = new RREQ((byte) 0, (byte) 255, this.nodeAddress, ++this.requestId,
                    (byte) destinationAddress, (byte) 0, (byte) 0, this.nodeAddress, ++this.sequenceNumber);
            broadCastRouteRequest(routeRequest);
            //wait for route reply
            this.waitingForRouteReply = true;
            this.startTimeWaitingForRouteResponse = System.currentTimeMillis();
            this.savedRouteRequestForRetries = routeRequest;
            this.savedMessageAfterRouteRequest = message;
        } else {
            MSG messagePacket = new MSG((byte) 48, route.getNextHop(), this.nodeAddress, (byte) destinationAddress,
                    this.nodeAddress, route.getHopCount(), message);
            String decodedMessage = Base64.getEncoder().withoutPadding().encodeToString(messagePacket.toMessage());
            byte[] messageBytes = decodedMessage.getBytes();

            try {
                sendMessagePacket(messageBytes);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessagePacket(byte[] messageBytes) throws InterruptedException {
        if (acknowledgmentReties == 0) {
            //save the message in case we need to send it again
            this.savedMessageForRetries = messageBytes;
        }

        loraController.sendMessage(messageBytes);
        this.waitingForAcknowledgement = true;
        this.startTimeWaitingForAcknowledgment = System.currentTimeMillis();
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
                    handleMessage(decodedBytes, decodedReceivedMessage);
                    break;
                case 64:
                    //acknowledgement
                    handleAcknowledgement(decodedBytes);
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

        //todo: this need to be checked against the protocol
        if (decodedBytes[2] == this.nodeAddress) {
            System.out.println("route request package is an echo, return...");
            return;
        }

        //check if this node is the destination
        if (decodedBytes[4] == this.nodeAddress) {
            System.out.println("this node is the destination, sending route reply");
            RREP routeReply = new RREP((byte) 16, decodedBytes[2], this.nodeAddress, decodedBytes[3],
                    decodedBytes[7], decodedBytes[5], (byte) 0, this.nodeAddress);
            byte[] routeReplyBytes = routeReply.toMessage();
            sendRouteReply(routeReplyBytes);
        }

        RREQ routeRequest = new RREQ(decodedBytes[0], decodedBytes[1], decodedBytes[2], decodedBytes[3],
                decodedBytes[4], decodedBytes[5], decodedBytes[6], decodedBytes[7], decodedBytes[8]);

        Route route = checkRoutingTable(routeRequest.getDestinationAddress());

        //no route found, broadcast a route request
        if (route == null || !route.isValid()) {
            writeToReversRoutingTable(routeRequest);
            routeRequest.setHopCount((byte) (routeRequest.getHopCount() + 1));  //increase hop count
            routeRequest.setPrevHopAddress(this.nodeAddress);
            broadCastRouteRequest(routeRequest);
        } else {
            RREP routeReply = new RREP((byte) 16, routeRequest.getPrevHopAddress(), this.nodeAddress, decodedBytes[3],
                    routeRequest.getOriginatorAddress(), routeRequest.getDestinationSequence(), route.getHopCount(),
                    route.getDestinationAddress());
            sendRouteReply(routeReply.toMessage());
        }
    }

    private void handleRouteReply(byte[] decodedBytes) {
        // check if route reply is for this node
        if (decodedBytes[4] == this.nodeAddress) {
            RREP routeReply = new RREP(decodedBytes[0], decodedBytes[1], decodedBytes[2], decodedBytes[3],
                    decodedBytes[4], decodedBytes[5], decodedBytes[6], decodedBytes[7]);
            updateRoutingTable(routeReply);

            //ready to send message
            this.waitingForRouteReply = false;
            Route route = checkRoutingTable(decodedBytes[7]);
            String message = this.savedMessageAfterRouteRequest;

            MSG messagePacket = new MSG((byte) 48, route.getNextHop(), this.nodeAddress, route.getDestinationAddress(),
                    this.nodeAddress, route.getHopCount(), message);
            byte[] messagePacketBytes = (Base64.getEncoder().withoutPadding().encodeToString(messagePacket.toMessage())
                    + message).getBytes();

            try {
                sendMessagePacket(messagePacketBytes);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } else if (decodedBytes[1] == this.nodeAddress) {
            ReverseRoute foundReverseRoute = checkReverseRoutingTable(decodedBytes[4]);

            if (foundReverseRoute == null) {
                System.out.println("no reverse route entry found, discarding message.");
                return;
            }

            RREP routeReply = new RREP(decodedBytes[0], foundReverseRoute.getPreviousHop(), this.nodeAddress,
                    decodedBytes[3], decodedBytes[4], decodedBytes[5], (byte) (decodedBytes[6] + 1), decodedBytes[7]);
            updateRoutingTable(routeReply);
            sendRouteReply(routeReply.toMessage());
        }
    }

    private void handleRouteError(byte[] decodedBytes) {
        if (decodedBytes[1] != this.nodeAddress) {
            return;
        }

        List<Byte> destinationAddresses = new ArrayList<>();
        List<Route> brockenRoutes = new ArrayList<>();
        List<Byte> precursors = new ArrayList<>();

        RERR error = new RERR(decodedBytes[0], decodedBytes[1], decodedBytes[2], decodedBytes[3]);

        sendAcknowledgementAfterReceivingRouteError(error);

        for (int i = 4; i < error.getPathCount(); i += 2) {
            RoutePath routePath = new RoutePath(decodedBytes[i], decodedBytes[i++]);
            destinationAddresses.add(decodedBytes[i]);
            error.addPath(routePath);
        }

        //invalid routes
        for (Byte destinationAddress : destinationAddresses) {
            Route route = checkRoutingTable(destinationAddress);
            if (route != null) {
                route.setValid(false);
                brockenRoutes.add(route);
                precursors.addAll(route.getPrecursors());
            }
        }

        //remove duplicates
        precursors = precursors.stream()
                .distinct()
                .collect(Collectors.toList());

        for (Byte precursor : precursors) {
            sendRouteErrorMessage(precursor, decodedBytes[3], error.getDestinationAddresses());
        }
    }

    private void handleMessage(byte[] decodedBytes, String decodedReceivedMessage) {
        if (decodedBytes[3] == this.nodeAddress) {

            String messageString = decodedReceivedMessage.substring(8);

            MSG message = new MSG(decodedBytes[0], decodedBytes[1], decodedBytes[2], decodedBytes[3], decodedBytes[4],
                    decodedBytes[5], messageString);
            System.out.println("*******************************************");
            System.out.println("received message:");
            System.out.println(messageString);
            System.out.println("*******************************************");

            System.out.println("sending acknowledgement");
            sendAcknowledgementAfterReceivingMessage(message);
        }
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
        routeRequestBytes = Base64.getEncoder().encodeToString(routeRequestBytes).getBytes();

        try {
            if (!loraController.sendMessage(routeRequestBytes)) {
                System.out.println("sending route request failed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendRouteReply(byte[] routeReplyByte) {
        routeReplyByte = Base64.getEncoder().encodeToString(routeReplyByte).getBytes();
        try {
            loraController.sendMessage(routeReplyByte);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //todo: check the parameter of the route

    private void updateRoutingTable(@NotNull RREP routeReply) {
        Route route1 = checkRoutingTable(routeReply.getOriginatorAddress());
        if (route1 == null) {
            Route route = new Route(routeReply.getOriginatorAddress(), routeReply.getPrevHopAddress(),
                    routeReply.getHopCount(), routeReply.getDestinationSequence(), true);
            route.getPrecursors().add(routeReply.getPrevHopAddress());
            routingTable.add(route);
        } else {
            // todo: check if the route is better or newer
        }

    }

    private void sendAcknowledgementAfterReceivingMessage(MSG message) {
        ACK acknowledgement = new ACK((byte) 64, message.getPrevHopAddress(), this.nodeAddress);
        sendAcknowledgmentPacket(acknowledgement);
    }

    private void handleAcknowledgement(byte[] decodedBytes) {
        if (decodedBytes[1] == this.nodeAddress) {
            System.out.println("acknowledgement received");
            this.waitingForAcknowledgement = false;
            this.acknowledgmentReties = 0;
        }
    }

    private List<Route> getAllRoutesWithHopAddress(byte unreachableHopAddress) {
        return this.routingTable.stream()
                .filter(route -> route.getNextHop() == unreachableHopAddress && route.isValid())
                .collect(Collectors.toList());
    }

    private void sendAcknowledgementAfterReceivingRouteError(RERR error) {
        ACK acknowledgment = new ACK((byte) 64, error.getPrevHopAddress(), this.nodeAddress);
        sendAcknowledgmentPacket(acknowledgment);
        System.out.println("Acknowledgement message couldn't be send");
    }

    private void sendAcknowledgmentPacket(ACK acknowledgement) {
        byte[] acknowledgementBytes = acknowledgement.toMessage();
        acknowledgementBytes = Base64.getEncoder().withoutPadding().encodeToString(acknowledgementBytes).getBytes();

        try {
            if (!loraController.sendMessage(acknowledgementBytes)) {
                System.out.println("sending acknowledgement failed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
