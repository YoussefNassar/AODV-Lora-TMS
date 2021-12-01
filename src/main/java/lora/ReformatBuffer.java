package lora;

public class ReformatBuffer {
    public String outputString;

    static int cutoffASCII = 10; // ASCII code of the character used for cut-off between received messages
    static String bufferReadToString = ""; // empty, but not null

    public static void parseByteArray(byte[] readBuffer) {

        String bufferString = new String(readBuffer);
        bufferReadToString = bufferReadToString.concat(bufferString);

        if((bufferReadToString.indexOf(cutoffASCII) + 1) > 0) {

            String outputString = bufferReadToString.substring(0, bufferReadToString.indexOf(cutoffASCII) + 1);
            bufferReadToString = bufferReadToString.substring(bufferReadToString.indexOf(cutoffASCII) + 1); // adjust as needed to accommodate the CRLF convention ("\n\r"), ASCII 10 & 13

            LoraController.receivedMessage.add(bufferReadToString);
            System.out.print(outputString);

        }
    }
}
