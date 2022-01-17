package protocol;

public class RoutePath {
    private byte destinationAddress;
    private byte sequenceNumber;

    public RoutePath(byte destinationAddress, byte sequenceNumber) {
        this.destinationAddress = destinationAddress;
        this.sequenceNumber = sequenceNumber;
    }

    public byte getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(byte destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public byte getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(byte sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
