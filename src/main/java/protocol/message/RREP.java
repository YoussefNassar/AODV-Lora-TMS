package protocol.message;

public class RREP extends Message{
    private byte requestId;
    private byte destinationAddress;
    private byte destinationSequence;
    private byte hopCount;
    private byte originatorAddress;
    private byte TTL;

    public RREP(byte type, byte flags, byte hopAddress, byte prevHopAddress, byte requestId, byte destinationAddress,
                byte destinationSequence, byte hopCount, byte originatorAddress, byte TTL) {
        super(type, flags, hopAddress, prevHopAddress);
        this.requestId = requestId;
        this.destinationAddress = destinationAddress;
        this.destinationSequence = destinationSequence;
        this.hopCount = hopCount;
        this.originatorAddress = originatorAddress;
        this.TTL = TTL;
    }

    @Override
    public byte[] toMessage() {
        return new byte[0];
    }
}
