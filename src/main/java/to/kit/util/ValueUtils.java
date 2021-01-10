package to.kit.util;

public class ValueUtils {
    public static int pad(int value, int padding) {
        int mod = value % padding;

        if (mod == 0) {
            return value;
        }
        return value + padding - mod;
    }

    private ValueUtils() {
        // nop
    }
}
