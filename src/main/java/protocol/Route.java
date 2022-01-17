package protocol;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private byte destinationAddress;
    private byte nextHop;
    private List<Byte> precursors = new ArrayList<>();
    private byte hopCount;  //aka metric
    private byte destinationSequenceNumber;
    private boolean isValid;

    public Route() {
    }

    public Route(byte destinationAddress, byte nextHop, List<Byte> precursors, byte hopCount, byte destinationSequenceNumber, boolean isValid) {
        this.destinationAddress = destinationAddress;
        this.nextHop = nextHop;
        this.precursors = precursors;
        this.hopCount = hopCount;
        this.destinationSequenceNumber = destinationSequenceNumber;
        this.isValid = isValid;
    }

    public Route(byte destinationAddress, byte nextHop, byte hopCount, byte destinationSequenceNumber, boolean isValid) {
        this.destinationAddress = destinationAddress;
        this.nextHop = nextHop;
        this.hopCount = hopCount;
        this.destinationSequenceNumber = destinationSequenceNumber;
        this.isValid = isValid;
    }

    public byte getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(byte destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public byte getNextHop() {
        return nextHop;
    }

    public void setNextHop(byte nextHop) {
        this.nextHop = nextHop;
    }

    public List<Byte> getPrecursors() {
        return precursors;
    }

    public void setPrecursors(List<Byte> precursors) {
        this.precursors = precursors;
    }

    public byte getHopCount() {
        return hopCount;
    }

    public void setHopCount(byte hopCount) {
        this.hopCount = hopCount;
    }

    public byte getDestinationSequenceNumber() {
        return destinationSequenceNumber;
    }

    public void setDestinationSequenceNumber(byte destinationSequenceNumber) {
        this.destinationSequenceNumber = destinationSequenceNumber;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }


}
