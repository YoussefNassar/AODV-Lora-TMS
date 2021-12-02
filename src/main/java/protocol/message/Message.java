package protocol.message;

public abstract class Message {
    private byte type_flags;
    private byte hopAddress;
    private byte prevHopAddress;

    public Message(byte type_flags, byte hopAddress, byte prevHopAddress) {
        this.type_flags = type_flags;
        this.hopAddress = hopAddress;
        this.prevHopAddress = prevHopAddress;
    }

    public byte getType_flags() {
        return type_flags;
    }

    public void setType_flags(byte type_flags) {
        this.type_flags = type_flags;
    }

    public byte getHopAddress() {
        return hopAddress;
    }

    public void setHopAddress(byte hopAddress) {
        this.hopAddress = hopAddress;
    }

    public byte getPrevHopAddress() {
        return prevHopAddress;
    }

    public void setPrevHopAddress(byte prevHopAddress) {
        this.prevHopAddress = prevHopAddress;
    }

    public abstract byte[] toMessage();
}
