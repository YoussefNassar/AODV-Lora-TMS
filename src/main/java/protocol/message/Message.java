package protocol.message;

public abstract class Message {
    private final byte type;
    private byte prevHop;
    private String destinationAddress;


    public Message(byte type, String destinationAddress) {
        this.type = type;
        this.destinationAddress = destinationAddress;
    }

    public byte getType() {
        return type;
    }

    public byte getPrevHop() {
        return prevHop;
    }

    public void setPrevHop(byte prevHop) {
        this.prevHop = prevHop;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public abstract byte[] toMessage();
}
