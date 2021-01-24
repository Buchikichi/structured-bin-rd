package to.kit.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PackBits {
    public static byte[] decode(byte[] source) {
        List<Byte> list = new ArrayList<>();
        int repeat = 0;
        int literal = 0;

        for (byte b : source) {
            if (0 < repeat) {
                for (; 0 < repeat; repeat--) {
                    list.add(b);
               }
                continue;
            }
            if (0 < literal) {
                list.add(b);
                literal--;
                continue;
            }
            if (b < 0) {
                repeat = 1 - b;
            } else {
                literal = b + 1;
            }
        }
        byte[] bytes = new byte[list.size()];
        for (int ix = 0; ix < list.size(); ix++) {
            bytes[ix] = list.get(ix);
        }
        return bytes;
    }

    private PackBits() {
        // nop
    }
}
