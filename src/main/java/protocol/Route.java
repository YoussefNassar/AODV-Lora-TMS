package protocol;

public class Route {
    private byte destinationAddress;
    private byte nextHop;
    private byte[] precursors;
    private byte hopCount;  //aka metric
    private byte destinationSequenceNumber;
    private boolean isValid;
}
