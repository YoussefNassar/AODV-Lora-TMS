package protocol.message;

import java.util.List;

public class RERR extends Message {
    private byte pathCount;
    private List<Byte> destinationAddresses;

    public RERR(byte type, byte flags, byte hopAddress, byte prevHopAddress, byte pathCount, List<Byte> destinationAddresses) {
        super(type, flags, hopAddress, prevHopAddress);
        this.pathCount = pathCount;
        this.destinationAddresses = destinationAddresses;
    }

    @Override
    public byte[] toMessage() {
        return new byte[0];
    }
}
