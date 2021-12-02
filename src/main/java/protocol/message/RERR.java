package protocol.message;

import protocol.RoutePath;

import java.util.ArrayList;
import java.util.List;

public class RERR extends Message {
    private byte pathCount; // not sure if still needed
    private List<RoutePath> destinationAddresses = new ArrayList<>();

    public RERR(byte type_flags, byte hopAddress, byte prevHopAddress, byte pathCount, List<RoutePath> destinationAddresses) {
        super(type_flags, hopAddress, prevHopAddress);
        this.pathCount = pathCount;
        this.destinationAddresses = destinationAddresses;
    }

    public RERR(byte type_flags, byte hopAddress, byte prevHopAddress, byte pathCount) {
        super(type_flags, hopAddress, prevHopAddress);
        this.pathCount = pathCount;
    }

    public List<RoutePath> getDestinationAddresses() {
        return destinationAddresses;
    }

    public void setDestinationAddresses(List<RoutePath> destinationAddresses) {
        this.destinationAddresses = destinationAddresses;
    }

    public void addPath(RoutePath routePath) {
        this.destinationAddresses.add(routePath);
    }

    @Override
    public byte[] toMessage() {
        byte[] message;
        int offset = 4;
        if (pathCount > 1) {
            message = new byte[offset + (destinationAddresses.size() * 2) + 1];
            message[message.length - 1] = (byte) 0;
        } else {
            message = new byte[offset + (destinationAddresses.size() * 2)];
        }

        message[0] = getType_flags();
        message[1] = getHopAddress();
        message[2] = getPrevHopAddress();
        message[3] = pathCount;

        for (int i = 0; i < destinationAddresses.size(); i++) {
            message[offset + (i * 2)] = destinationAddresses.get(i).getDestinationAddress();
        }

        for (int i = 0; i < destinationAddresses.size(); i++) {
            message[offset + ((i * 2) + 1)] = destinationAddresses.get(i).getSequenceNumber();
        }

        return message;
    }
}
