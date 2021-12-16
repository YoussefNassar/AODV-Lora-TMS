import lora.LoraController;
import protocol.ProtocolController;
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

//        Main main = new Main();
//        main.testEncoding();

//        set up connection and set up the module
        LoraController loraController = new LoraController();
        ProtocolController protocolController = new ProtocolController(loraController);
        loraController.setUpBluetoothConnection();
        loraController.setUpTheModule();

        while (true) {
            protocolController.startProtocolController();
            Thread.sleep(1000);
        }


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

    public void testEncoding() {
        RREQ routeRequest = new RREQ((byte) 0, (byte) 4, (byte) 10, (byte) 120, (byte) 1,
                (byte) 80, (byte) 2, (byte) 6, (byte) 100);

        RREP routeReply = new RREP((byte) 16, (byte) 9, (byte) 7, (byte) 120, (byte) 1,
                (byte) 80, (byte) 2, (byte) 11, (byte) 6);

        RERR Error = new RERR((byte) 32, (byte) 12, (byte) 1, (byte) 1);
        Error.addPath(new RoutePath((byte) 20, (byte) 110));

        RERR error1 = new RERR((byte) 32, (byte) 8, (byte) 3, (byte) 2);
        error1.addPath(new RoutePath((byte) 21, (byte) 51));
        error1.addPath(new RoutePath((byte) 18, (byte) 10));

        MSG message = new MSG((byte) 48, (byte) 2, (byte) 15, (byte) 4, (byte) 100, (byte) 4, "AODV");

        ACK acknowledgement = new ACK((byte) 64, (byte) 3, (byte) 67);

        String decoded = Base64.getEncoder().encodeToString(routeRequest.toMessage());
        System.out.println(decoded);
        byte[] decode = Base64.getDecoder().decode(decoded);
        System.out.println(decode);

        String decoded1 = Base64.getEncoder().encodeToString(routeReply.toMessage());
        System.out.println(decoded1);

        String decoded2 = Base64.getEncoder().withoutPadding().encodeToString(Error.toMessage());
        System.out.println(decoded2);
        byte[] decodeError = Base64.getDecoder().decode(decoded2);
        System.out.println(decodeError);

        String decoded3 = Base64.getEncoder().withoutPadding().encodeToString(error1.toMessage());
        System.out.println(decoded3);
        byte[] decodeError2 = Base64.getDecoder().decode(decoded3);
        System.out.println(decodeError2);

        String decoded4 = Base64.getEncoder().withoutPadding().encodeToString(message.toMessage());
        decoded4 = decoded4 + message.getText();
        System.out.println(decoded4);

        String decoded5 = Base64.getEncoder().withoutPadding().encodeToString(acknowledgement.toMessage());
        System.out.println(decoded5);
    }

}
