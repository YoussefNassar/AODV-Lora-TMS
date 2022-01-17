package testEnvironment;

import java.util.Locale;
import java.util.Scanner;

public class TestEnvironment {

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        TestEnvironment testEnvironment = new TestEnvironment();
        while (true) {
            String command = scanner.nextLine();
            String response = testEnvironment.handleCommand(command);
            System.out.println(response);
        }
    }

    private String handleCommand(String command) {
        command = command.toUpperCase(Locale.ROOT);

        if (command.contains("AT+CFG=")) {
            command = "AT+CFG =";
        } else if (command.contains("SEND")){
            handleSend();
            return "";
        }

        switch (command) {
            case "AT":
            case "AT+SEND":
            case "AT+CFG =":
                return "AT,OK \r\n";
            case "at+addr?":
                return "AT,0002,OK";
            default:
                return "AT,ERROR \r\n";
        }
    }

    private void handleSend() {
        System.out.println("inter message:");
        scanner.nextLine();
        System.out.println("AT,SENDING");
        System.out.println("AT,SENDED");
    }
}
