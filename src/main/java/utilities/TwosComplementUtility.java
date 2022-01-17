package utilities;

public class TwosComplementUtility {

    public static boolean isNewer(int incoming, int current) {
        return (subtract(incoming, current) < 0);
    }

    private static int subtract(int incoming, int current)
    {
        //todo: with bit overflow
        int result;
        // ~b is the 1's Complement
        // of b adding 1 to it make
        // it 2's Complement
        result = incoming + (~current + 1);

        return result;
    }
}
