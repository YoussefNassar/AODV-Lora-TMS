package protocol;

import lora.LoraController;
import org.jetbrains.annotations.NotNull;
import protocol.message.ACK;
import protocol.message.MSG;
import protocol.message.RERR;
import protocol.message.RREP;
import protocol.message.RREQ;
import utilities.TwosComplementUtility;

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
    private byte sequenceNumber = 0;
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
                (System.currentTimeMillis() - this.startTimeWaitingForAcknowledgment > 120000)) {
            this.startTimeWaitingForAcknowledgment = System.currentTimeMillis();
            this.acknowledgmentReties++;
            System.out.println("no acknowledgment yet");
            System.out.println("resend message");
            sendMessagePacket(this.savedMessageForRetries,true);
        } else if (this.waitingForAcknowledgement && this.acknowledgmentReties == 3 &&
                (System.currentTimeMillis() - this.startTimeWaitingForAcknowledgment > 120000)) {
            System.out.println("message sent 3 times and no acknowledgement was received");
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
                (System.currentTimeMillis() - this.startTimeWaitingForRouteResponse > 300000)) {
            this.startTimeWaitingForRouteResponse = System.currentTimeMillis();
            this.routeRequestRetries++;
            System.out.println("no route response yet");
            System.out.println("resending route request");
            this.requestId++;
            this.sequenceNumber++;
            this.savedRouteRequestForRetries.setRequestId(this.requestId);
            this.savedRouteRequestForRetries.setOriginatorSequence(this.sequenceNumber);
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
            int indexOfLastComma = decodedReceivedMessage.lastIndexOf(",");

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

        System.out.println("you have 8 seconds to enter your message :");
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 8 * 1000 && !bufferedReader.ready()) {
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
            this.requestId += 1;
            this.sequenceNumber += 1;
            RREQ routeRequest = new RREQ((byte) 0, (byte) 255, this.nodeAddress, this.requestId,
                    (byte) destinationAddress, (byte) 0, (byte) 0, this.nodeAddress, this.sequenceNumber);
            broadCastRouteRequest(routeRequest);
            //wait for route reply
            this.waitingForRouteReply = true;
            this.startTimeWaitingForRouteResponse = System.currentTimeMillis();
            this.savedRouteRequestForRetries = routeRequest;
            this.savedMessageAfterRouteRequest = message;
        } else {
            MSG messagePacket = new MSG((byte) 48, route.getNextHop(), this.nodeAddress, (byte) destinationAddress,
                    this.nodeAddress, route.getHopCount());
            String decodedMessage = Base64.getEncoder().withoutPadding().encodeToString(messagePacket.toMessage());
            decodedMessage = decodedMessage + message;
            byte[] messageBytes = decodedMessage.getBytes();

            try {
                sendMessagePacket(messageBytes, true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessagePacket(byte[] messageBytes, boolean isSender) throws InterruptedException {
        if (isSender) {
            if (acknowledgmentReties == 0) {
                //save the message in case we need to send it again
                this.savedMessageForRetries = messageBytes;
            }

            loraController.sendMessage(messageBytes);
            this.waitingForAcknowledgement = true;
            this.startTimeWaitingForAcknowledgment = System.currentTimeMillis();
        } else {
            loraController.sendMessage(messageBytes);
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
            byte[] decodedBytes;

            //check if it is a message packet
            String firstTwoChars = decodedReceivedMessage.substring(0, 2);
            if (firstTwoChars.equals("MA")) {
                decodedBytes = Base64.getDecoder().decode(decodedReceivedMessage.substring(0, 8));
            } else {
                decodedBytes = Base64.getDecoder().decode(decodedReceivedMessage);
            }

            decodedBytes[0] = (byte) (decodedBytes[0] & 0xF0);

            switch (decodedBytes[0]) {
                case 0:
                    handleRouteRequest(decodedBytes);
                    break;
                case 16:
                    handleRouteReply(decodedBytes);
                    break;
                case 32:
                    handleRouteError(decodedBytes);
                    break;
                case 48:
                    handleMessage(decodedBytes, decodedReceivedMessage);
                    break;
                case 64:
                    handleAcknowledgement(decodedBytes);
                    break;
                default:
                    // the message is not valid
                    break;
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            System.out.println("the string cannot be decoded with base64");
        }
    }

    private void handleRouteRequest(byte[] decodedBytes) {
        updateRoutingTableFromRouteRequest(decodedBytes);

        //todo: this need to be checked against the protocol
        if (decodedBytes[2] == this.nodeAddress) {
            System.out.println("route request package is an echo, return...");
            return;
        }

        //check if this node is the destination
        if (decodedBytes[4] == this.nodeAddress) {
            System.out.println("this node is the destination for a route request, sending route reply");
            RREP routeReply = new RREP((byte) 16, decodedBytes[2], this.nodeAddress, decodedBytes[3],
                    decodedBytes[7], decodedBytes[8], (byte) 0, this.nodeAddress);
            byte[] routeReplyBytes = routeReply.toMessage();
            sendRouteReply(routeReplyBytes);
            return;
        }

        RREQ routeRequest = new RREQ((byte) 0, decodedBytes[1], decodedBytes[2], decodedBytes[3],
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
                    routeRequest.getOriginatorAddress(), routeRequest.getOriginatorSequence(), route.getHopCount(),
                    route.getDestinationAddress());
            sendRouteReply(routeReply.toMessage());
        }
    }

    private void updateRoutingTableFromRouteRequest(byte[] decodedBytes) {
        Route route = checkRoutingTable(decodedBytes[4]);
        if (route == null) {
            if (decodedBytes[7] == decodedBytes[2]) {
                Route route1 = new Route(decodedBytes[7], decodedBytes[7], (byte) 0, this.sequenceNumber,true);
                route1.getPrecursors().add(decodedBytes[2]);
                this.routingTable.add(route1);
            } else {
                Route route1 = new Route(decodedBytes[7], decodedBytes[2], (byte) (decodedBytes[1] + 1),
                        this.sequenceNumber,true);
                route1.getPrecursors().add(decodedBytes[2]);
                this.routingTable.add(route1);
            }
        }
    }

    private void handleRouteReply(byte[] decodedBytes) {
        // check if route reply is for this node
        if (decodedBytes[4] == this.nodeAddress) {
            RREP routeReply = new RREP((byte) 16, decodedBytes[1], decodedBytes[2], decodedBytes[3],
                    decodedBytes[4], decodedBytes[5], decodedBytes[6], decodedBytes[7]);
            updateRoutingTable(routeReply);

            //ready to send message
            this.waitingForRouteReply = false;
            Route route = checkRoutingTable(decodedBytes[7]);
            String message = this.savedMessageAfterRouteRequest;

            MSG messagePacket = new MSG((byte) 48, route.getNextHop(), this.nodeAddress, route.getDestinationAddress(),
                    this.nodeAddress, route.getHopCount());
            byte[] messagePacketBytes = (Base64.getEncoder().withoutPadding().encodeToString(messagePacket.toMessage())
                    + message).getBytes();

            try {
                sendMessagePacket(messagePacketBytes,true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } else if (decodedBytes[1] == this.nodeAddress) {
            ReverseRoute foundReverseRoute = checkReverseRoutingTable(decodedBytes[4]);
            System.out.println("hop address  for route reply: " + decodedBytes[1]);

            if (foundReverseRoute == null) {
                System.out.println("no reverse route entry found, discarding message.");
                return;
            }

            /*
            when saving to reverse route from route request:the destination = destination
            source = originator
            prev hop = prev hop
             */
            RREP routeReply = new RREP((byte) 16, decodedBytes[1], decodedBytes[2],
                    decodedBytes[3], decodedBytes[4], decodedBytes[5], decodedBytes[6], decodedBytes[7]);

            updateRoutingTable(routeReply);

            routeReply = new RREP((byte) 16, foundReverseRoute.getPreviousHop(), this.nodeAddress,
                    decodedBytes[3], decodedBytes[4], decodedBytes[5], (byte) (decodedBytes[6] + 1), decodedBytes[7]);
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

        RERR error = new RERR((byte) 32, decodedBytes[1], decodedBytes[2], decodedBytes[3]);

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
        String messageString = decodedReceivedMessage.substring(8);

        if (decodedBytes[3] == this.nodeAddress) {
            MSG message = new MSG((byte) 48, decodedBytes[1], decodedBytes[2], decodedBytes[3], decodedBytes[4],
                    decodedBytes[5]);
            System.out.println("*******************************************");
            System.out.println("received message:");
            System.out.println(messageString);
            System.out.println("*******************************************");

            System.out.println("sending acknowledgement");
            sendAcknowledgementAfterReceivingMessage(message);
        } else {
            Route route = checkRoutingTable(decodedBytes[3]);
            if (route == null) {
                System.out.println("no route entry to forward the message packet");
                System.out.println("dropping message");
                return;
            }

            MSG message = new MSG((byte) 48, route.getNextHop(), this.nodeAddress, decodedBytes[3], decodedBytes[4],
                    ++decodedBytes[5]);

            byte[] messageBytes = (Base64.getEncoder().withoutPadding().encodeToString(message.toMessage())
                    + messageString).getBytes();

            ACK acknowledgement = new ACK((byte) 64, decodedBytes[2], this.nodeAddress);
            try {
                sendAcknowledgmentPacket(acknowledgement);
                sendMessagePacket(messageBytes, false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Route checkRoutingTable(byte destinationAddress) {
        return routingTable.stream()
                .filter(route -> route.getDestinationAddress() == destinationAddress && route.isValid())
                .findAny()
                .orElse(null);
    }

    private ReverseRoute checkReverseRoutingTable(byte destinationAddress) {
        return reverseRoutingTable.stream()
                .filter(reverseRoute -> reverseRoute.getSource() == destinationAddress)
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
        System.out.println("broad casting: " + Base64.getEncoder().withoutPadding().encodeToString(routeRequestBytes));
        routeRequestBytes = Base64.getEncoder().withoutPadding().encodeToString(routeRequestBytes).getBytes();

        try {
            if (!loraController.sendMessage(routeRequestBytes)) {
                System.out.println("sending route request failed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendRouteReply(byte[] routeReplyByte) {
        System.out.println(Base64.getEncoder().encodeToString(routeReplyByte));
        routeReplyByte = Base64.getEncoder().withoutPadding().encodeToString(routeReplyByte).getBytes();
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
            routeReply.setHopCount((byte) (routeReply.getHopCount() + 1));

            Route route = new Route(routeReply.getOriginatorAddress(), routeReply.getPrevHopAddress(),
                    routeReply.getHopCount(), routeReply.getDestinationSequence(), true);
            route.getPrecursors().add(routeReply.getPrevHopAddress());
            routingTable.add(route);
        } else {
            //todo: check if checking the sequence number is right
            if (!(route1.isValid() || routeReply.getHopCount() < route1.getHopCount() ||
                    TwosComplementUtility.isNewer(routeReply.getDestinationSequence(), route1.getDestinationSequenceNumber()))) {
                route1.setValid(false);
                Route route = new Route(routeReply.getOriginatorAddress(), routeReply.getPrevHopAddress(),
                        routeReply.getHopCount(), routeReply.getDestinationSequence(), true);
                route.getPrecursors().add(routeReply.getPrevHopAddress());
                routingTable.add(route);
            }
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
