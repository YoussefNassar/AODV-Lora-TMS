package protocol.message;

public class RREQ extends Message{

    private byte flag;
    private byte hopAddress;
    private byte sourceAddress;
    private byte hopCount;
    private byte sequenceNumber;
    private byte DestinationSequenceNumber;
    private byte RequestId;
    private byte TTL;


    public RREQ(byte type, String destinationAddress, byte flag, byte hopAddress, byte sourceAddress,
                byte hopCount, byte sequenceNumber, byte destinationSequenceNumber, byte requestId, byte TTL) {
        super(type, destinationAddress);
        this.flag = flag;
        this.hopAddress = hopAddress;
        this.sourceAddress = sourceAddress;
        this.hopCount = hopCount;
        this.sequenceNumber = sequenceNumber;
        DestinationSequenceNumber = destinationSequenceNumber;
        RequestId = requestId;
        this.TTL = TTL;
    }

    @Override
    public byte[] toMessage() {
        return new byte[0];
    }
}
