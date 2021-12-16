package protocol;

public class ReverseRoute {
    private byte destination;
    private byte source;
    private byte requestId;
    private byte hopCount;     // AKA metric
    private byte previousHop;

    public ReverseRoute() {
    }

    public ReverseRoute(byte destination, byte source, byte requestId, byte hopCount, byte previousHop) {
        this.destination = destination;
        this.source = source;
        this.requestId = requestId;
        this.hopCount = hopCount;
        this.previousHop = previousHop;
    }

    public byte getDestination() {
        return destination;
    }

    public void setDestination(byte destination) {
        this.destination = destination;
    }

    public byte getSource() {
        return source;
    }

    public void setSource(byte source) {
        this.source = source;
    }

    public byte getRequestId() {
        return requestId;
    }

    public void setRequestId(byte requestId) {
        this.requestId = requestId;
    }

    public byte getHopCount() {
        return hopCount;
    }

    public void setHopCount(byte hopCount) {
        this.hopCount = hopCount;
    }

    public byte getPreviousHop() {
        return previousHop;
    }

    public void setPreviousHop(byte previousHop) {
        this.previousHop = previousHop;
    }
}
