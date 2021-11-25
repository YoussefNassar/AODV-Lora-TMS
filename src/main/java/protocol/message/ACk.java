package protocol.message;

public class ACk extends Message{
    private byte hopAddress;
    private byte sourceAddress;

    public ACk(byte type, String destinationAddress, byte hopAddress, byte sourceAddress) {
        super(type, destinationAddress);
        this.hopAddress = hopAddress;
        this.sourceAddress = sourceAddress;
    }

    @Override
    public byte[] toMessage() {
        return new byte[0];
    }
}
