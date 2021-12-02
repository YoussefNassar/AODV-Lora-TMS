import protocol.RoutePath;
import protocol.message.ACK;
import protocol.message.MSG;
import protocol.message.RERR;
import protocol.message.RREP;
import protocol.message.RREQ;

import java.io.IOException;
import java.util.Base64;

/**
 * at+cfg=433000000,5,6,12,4,1,0,0,0,0,3000,8,8
 * at+send=8
 * jo jo jo
 */
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        RREQ RREQ = new RREQ((byte) 0, (byte) 4, (byte) 10, (byte) 120, (byte) 1,
                (byte) 80, (byte) 2, (byte) 6, (byte) 100);

        RREP RREP = new RREP((byte) 16, (byte) 9, (byte) 7, (byte) 120, (byte) 1,
                (byte) 80, (byte) 2, (byte) 11, (byte) 6);

//        List<Byte> destinationAddresses = new ArrayList<>();
        RERR RERR = new RERR((byte) 32, (byte) 12, (byte) 1, (byte) 1);
        RERR.addPath(new RoutePath((byte) 20, (byte) 110));

        RERR RERR1 = new RERR((byte) 32, (byte) 8, (byte) 3, (byte) 2);
        RERR1.addPath(new RoutePath((byte) 21, (byte) 51));
        RERR1.addPath(new RoutePath((byte) 18, (byte) 10));

        MSG msg = new MSG((byte) 48, (byte) 2, (byte) 15, (byte) 4, (byte) 100, (byte) 4, "AODV");

        ACK ack = new ACK((byte) 64, (byte) 3, (byte) 67);

        String decoded = Base64.getEncoder().encodeToString(RREQ.toMessage());
        System.out.println(decoded);

        String decoded1 = Base64.getEncoder().encodeToString(RREP.toMessage());
        System.out.println(decoded1);

        String decoded2 = Base64.getEncoder().withoutPadding().encodeToString(RERR.toMessage());
        System.out.println(decoded2);

        String decoded3 = Base64.getEncoder().withoutPadding().encodeToString(RERR1.toMessage());
        System.out.println(decoded3);

        String decoded4 = Base64.getEncoder().withoutPadding().encodeToString(msg.toMessage());
        decoded4 = decoded4 + msg.getText();
        System.out.println(decoded4);

        String decoded5 = Base64.getEncoder().withoutPadding().encodeToString(ack.toMessage());
        System.out.println(decoded5);

//        while (true) {
//            System.out.println("enter your command: ");
//            Scanner scanner = new Scanner(System.in);
//            //String command = scanner.nextLine();
//            //command = command.toUpperCase();
//
//            LoraController loraController = new LoraController();
//            loraController.setUpCommunication();
//            loraController.setUpTheModule();
//            //loraController.testConnectionInLab();
//        }
    }

}
