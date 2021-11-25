package protocol;

import java.util.LinkedList;

public class Route {
    private byte destinationAddress;
    private byte destinationSequence;
    private boolean validRoute;
    private byte hopCount;
    private byte nextHop;
    private LinkedList<Node> precursorsList = new LinkedList<>();
    private int precursor = 0;
    private int lifetimeUnsigned;
    private long lifetime;
}
