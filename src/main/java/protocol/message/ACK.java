package protocol.message;

public class ACK extends Message {

    public ACK(byte type_flags, byte hopAddress, byte prevHopAddress) {
        super(type_flags, hopAddress, prevHopAddress);
    }



    @Override
    public byte[] toMessage() {
        return new byte[]{getType_flags(), getHopAddress(), getPrevHopAddress()};
    }
}
