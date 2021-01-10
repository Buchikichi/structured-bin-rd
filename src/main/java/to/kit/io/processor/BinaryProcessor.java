package to.kit.io.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public interface BinaryProcessor {
    boolean realize();

    boolean process(InputStream stream, Consumer<Object> consumer) throws IOException;
}
