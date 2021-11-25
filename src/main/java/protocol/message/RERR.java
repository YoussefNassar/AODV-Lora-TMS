package protocol.message;

import java.util.List;

public class RERR extends Message{

    private byte flag;
    private byte hopAddress;
    private byte sourceAddress;
    private byte pathCount;
    private List<Byte> destinationAddress;

    public RERR(byte type, String destinationAddress, byte flag, byte hopAddress, byte sourceAddress,
                byte pathCount, List<Byte> destinationAddress1) {
        super(type, destinationAddress);
        this.flag = flag;
        this.hopAddress = hopAddress;
        this.sourceAddress = sourceAddress;
        this.pathCount = pathCount;
        this.destinationAddress = destinationAddress1;
    }

    @Override
    public byte[] toMessage() {
        return new byte[0];
    }
}
