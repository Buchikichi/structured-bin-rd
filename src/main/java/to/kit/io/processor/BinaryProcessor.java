package to.kit.io.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

public interface BinaryProcessor {
    boolean realize();

    boolean process(InputStream stream, ObjIntConsumer<Object> consumer) throws IOException;
}
