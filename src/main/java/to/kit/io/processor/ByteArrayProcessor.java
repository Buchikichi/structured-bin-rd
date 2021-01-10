package to.kit.io.processor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public class ByteArrayProcessor extends ArrayProcessor {
    private byte[] bytes;

    @Override
    public boolean realize() {
        return this.field.getType().equals(byte[].class);
    }

    @Override
    public boolean process(InputStream stream, Consumer<Object> consumer) throws IOException {
        if (this.bytes == null) {
            if (this.allocate == null) {
                return false;
            }
            int length = getValueByName(this.allocate.value());

            this.bytes = (byte[]) allocateArray(length);
            try {
                this.field.set(this.parent, bytes);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (this.bytes == null) {
            return false;
        }
        int read = stream.read(bytes);

        return bytes.length == read;
    }

    public ByteArrayProcessor(Object parent, Field field, Object fieldValue) {
        super(parent, field, fieldValue);
        if (fieldValue instanceof byte[]) {
            this.bytes = (byte[]) fieldValue;
        }
    }
}
