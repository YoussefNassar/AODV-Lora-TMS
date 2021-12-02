package protocol.message;

public class MSG extends Message {
    private byte destinationAddress;
    private byte originatorAddress;
    private byte hopCount;
    private String text;

    public MSG(byte type_flags, byte hopAddress, byte prevHopAddress, byte destinationAddress,
               byte originatorAddress, byte hopCount, String text) {
        super(type_flags, hopAddress, prevHopAddress);
        this.destinationAddress = destinationAddress;
        this.originatorAddress = originatorAddress;
        this.hopCount = hopCount;
        this.text = text;
    }

    public byte getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(byte destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public byte getOriginatorAddress() {
        return originatorAddress;
    }

    public void setOriginatorAddress(byte originatorAddress) {
        this.originatorAddress = originatorAddress;
    }

    public byte getHopCount() {
        return hopCount;
    }

    public void setHopCount(byte hopCount) {
        this.hopCount = hopCount;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public byte[] toMessage() {
        return new byte[]{getType_flags(), getHopAddress(), getPrevHopAddress(), destinationAddress
                , originatorAddress, hopCount};
    }
}
