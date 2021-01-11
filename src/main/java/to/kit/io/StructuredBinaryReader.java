package to.kit.io;

import to.kit.io.annotation.Padded;
import to.kit.io.processor.BinaryProcessor;
import to.kit.io.processor.BinaryProcessorBase;
import to.kit.io.processor.ByteArrayProcessor;
import to.kit.io.processor.ObjectArrayProcessor;
import to.kit.util.ValueUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class StructuredBinaryReader implements AutoCloseable {
    private final InputStream stream;
    private final int totalLength;
    private final List<Class<? extends BinaryProcessor>> processorList = new ArrayList<>();

    private void registerProcessors() {
        registerProcessor(ObjectArrayProcessor.class, ByteArrayProcessor.class);
    }

    private int getPos() throws IOException {
        return this.totalLength - this.stream.available();
    }

    private void pad(int lastPos, Field field) throws IOException {
        Padded padded = field.getAnnotation(Padded.class);

        if (padded == null) {
            return;
        }
        int read = getPos() - lastPos;
        for (int remain = ValueUtils.pad(read, padded.value()) - read; 0 < remain; remain--) {
            boolean failed = stream.read() == -1;

            if (failed) {
                // FIXME:
                throw new IOException("Cannot read data.");
            }
        }
    }

    public <T> void read(final T target, final Field targetField, final int index, final Object parent)
            throws IOException, IllegalAccessException {
        int lastPos = getPos();
        Class<?> clazz;

        if (target != null) {
            clazz = target.getClass();
        } else if (targetField != null) {
            clazz = targetField.getType();
        } else {
            return;
        }
        BinaryProcessor processor = this.processorList.stream()
                .map(c -> BinaryProcessorBase.create(c, target, targetField, index, parent))
                .filter(Objects::nonNull)
                .filter(BinaryProcessor::realize).findFirst().orElse(null);
        if (processor != null) {
            boolean processed = processor.process(this.stream, (o, ix) -> {
                try {
                    read(o, null, ix, parent);
                } catch (IOException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            if (processed) {
                return;
            }
        }
        for (final Field field : clazz.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            int mod = field.getModifiers();

            if (Modifier.isStatic(mod) || fieldType.isPrimitive()) {
                continue;
            }
//            System.out.format(">%s.%s@%08x\n", clazz.getSimpleName(), field.getName(), getPos());
            field.setAccessible(true);
            read(field.get(target), field, 0, target);
        }
        if (targetField != null) {
            pad(lastPos, targetField);
        }
    }

    public <T> T read(Class<T> clazz) throws IOException {
        T object;
        try {
            object = clazz.getConstructor().newInstance();
            read(object, null, 0, null);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
        return object;
    }

    @SafeVarargs
    public final void registerProcessor(Class<? extends BinaryProcessor>... processors) {
        Stream.of(processors).forEach(p -> this.processorList.add(0, p));
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    public StructuredBinaryReader(InputStream stream) throws IOException {
        this.stream = stream;
        this.totalLength = this.stream.available();
        registerProcessors();
    }
}
