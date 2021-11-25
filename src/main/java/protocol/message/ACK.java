package protocol.message;

public class ACK extends Message{
    
    public ACK(byte type, byte flags, byte hopAddress, byte prevHopAddress) {
        super(type, flags, hopAddress, prevHopAddress);
    }

    @Override
    public byte[] toMessage() {
        return new byte[0];
    }
}
