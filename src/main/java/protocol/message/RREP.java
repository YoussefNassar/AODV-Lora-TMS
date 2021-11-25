package protocol.message;

public class RREP extends Message{

    private byte flag;
    private byte hopAddress;
    private byte sourceAddress;
    private byte hopCount;
    private byte DestinationSequenceNumber;
    private byte RequestId;
    private byte TTL;

    public RREP(byte type, String destinationAddress, byte flag, byte hopAddress, byte sourceAddress, byte hopCount
            , byte destinationSequenceNumber, byte requestId, byte TTL) {
        super(type, destinationAddress);
        this.flag = flag;
        this.hopAddress = hopAddress;
        this.sourceAddress = sourceAddress;
        this.hopCount = hopCount;
        DestinationSequenceNumber = destinationSequenceNumber;
        RequestId = requestId;
        this.TTL = TTL;
    }

    @Override
    public byte[] toMessage() {
        return new byte[0];
    }
}
