package protocol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class ProtocolController {

    // Routing table of all valid routes
    private HashMap<Integer, Route> routingTable = new HashMap<>();
    private HashMap<Integer, Route> reverseRoutingTable = new HashMap<>();

    public static Queue<String> receivedMessage = new LinkedList();



    public void startProtocolController() {

    }
}
