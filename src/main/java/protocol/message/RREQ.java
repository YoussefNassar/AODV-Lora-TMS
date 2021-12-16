package protocol.message;

public class RREQ extends Message {
    private byte requestId;
    private byte destinationAddress;
    private byte destinationSequence;
    private byte hopCount;
    private byte originatorAddress;
    private byte TTL;

    public RREQ(byte type_flags, byte hopAddress, byte prevHopAddress, byte requestId, byte destinationAddress,
                byte destinationSequence, byte hopCount, byte originatorAddress, byte TTL) {
        super(type_flags, hopAddress, prevHopAddress);
        this.requestId = requestId;
        this.destinationAddress = destinationAddress;
        this.destinationSequence = destinationSequence;
        this.hopCount = hopCount;
        this.originatorAddress = originatorAddress;
        this.TTL = TTL;
    }

    public byte getRequestId() {
        return requestId;
    }

    public void setRequestId(byte requestId) {
        this.requestId = requestId;
    }

    public byte getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(byte destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public byte getDestinationSequence() {
        return destinationSequence;
    }

    public void setDestinationSequence(byte destinationSequence) {
        this.destinationSequence = destinationSequence;
    }

    public byte getHopCount() {
        return hopCount;
    }

    public void setHopCount(byte hopCount) {
        this.hopCount = hopCount;
    }

    public byte getOriginatorAddress() {
        return originatorAddress;
    }

    public void setOriginatorAddress(byte originatorAddress) {
        this.originatorAddress = originatorAddress;
    }

    public byte getTTL() {
        return TTL;
    }

    public void setTTL(byte TTL) {
        this.TTL = TTL;
    }

    @Override
    public byte[] toMessage() {
        return new byte[]{getType_flags(), getHopAddress(), getPrevHopAddress(), requestId, destinationAddress
                , destinationSequence, hopCount, originatorAddress, TTL};
    }
}
