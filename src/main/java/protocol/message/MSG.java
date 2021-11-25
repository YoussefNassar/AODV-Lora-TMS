package protocol.message;

public class MSG extends Message{

    private byte hopAddress;
    private byte hopCount;
    private byte sequenceNumber;
    private byte text;

    public MSG(byte type, String destinationAddress, byte hopAddress, byte hopCount, byte sequenceNumber, byte text) {
        super(type, destinationAddress);
        this.hopAddress = hopAddress;
        this.hopCount = hopCount;
        this.sequenceNumber = sequenceNumber;
        this.text = text;
    }

    @Override
    public byte[] toMessage() {
        return new byte[0];
    }
}
