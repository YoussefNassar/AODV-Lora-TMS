package utilities;

public class TwosComplementUtility {

    public static boolean isNewer(int firstNumber, int secondNumber) {
        return (subtract(firstNumber, secondNumber) < 0);
    }

    private static int subtract(int a, int b)
    {
        int c;

        // ~b is the 1's Complement
        // of b adding 1 to it make
        // it 2's Complement
        c = a + (~b + 1);

        return c;
    }
}
