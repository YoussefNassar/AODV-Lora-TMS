package protocol.message;

public abstract class Message {
    private byte type;
    private byte flags;
    private byte hopAddress;
    private byte prevHopAddress;

    public Message(byte type, byte flags, byte hopAddress, byte prevHopAddress) {
        this.type = type;
        this.flags = flags;
        this.hopAddress = hopAddress;
        this.prevHopAddress = prevHopAddress;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
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
