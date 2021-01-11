package to.kit.io.processor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

public class ByteArrayProcessor extends ArrayProcessor {
    @Override
    public boolean realize() {
        return this.field != null && this.field.getType().equals(byte[].class);
    }

    @Override
    public boolean process(InputStream stream, ObjIntConsumer<Object> consumer) throws IOException {
        byte[] bytes = (byte[]) this.target;

        if (bytes == null) {
            if (this.allocate == null) {
                return false;
            }
            int length = getValueByName(this.allocate.value());

            bytes = (byte[]) allocateArray(length);
            try {
                this.field.set(this.parent, bytes);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (bytes == null) {
            return false;
        }
        int read = stream.read(bytes);

        return bytes.length == read;
    }

    public ByteArrayProcessor(Object target, Field field, int index, Object parent) {
        super(target, field, index, parent);
    }
}
