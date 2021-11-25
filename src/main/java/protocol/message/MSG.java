package protocol.message;

public class MSG extends Message{
    private byte destinationAddress;
    private byte originatorAddress;
    private byte hopCount;
    private byte text;

    public MSG(byte type, byte flags, byte hopAddress, byte prevHopAddress, byte destinationAddress,
               byte originatorAddress, byte hopCount, byte text) {
        super(type, flags, hopAddress, prevHopAddress);
        this.destinationAddress = destinationAddress;
        this.originatorAddress = originatorAddress;
        this.hopCount = hopCount;
        this.text = text;
    }

    @Override
    public byte[] toMessage() {
        return new byte[0];
    }
}
