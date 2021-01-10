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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StructuredBinaryReader implements AutoCloseable {
    private final InputStream stream;
    private final int totalLength;
    private final List<Class<? extends BinaryProcessor>> processorList = new ArrayList<>();

    private void registerProcessors() {
        registerProcessor(ByteArrayProcessor.class, ObjectArrayProcessor.class);
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

    public <T> void read(final T obj) throws IOException, IllegalAccessException {
        Class<?> clazz = obj.getClass();

        for (final Field field : clazz.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            int mod = field.getModifiers();

            if (Modifier.isStatic(mod) || fieldType.isPrimitive()) {
                continue;
            }
//            System.out.format(">%s#%s@%08x\n", clazz.getSimpleName(), field.getName(), getPos());
            field.setAccessible(true);

            int lastPos = getPos();
            final Object fieldValue = field.get(obj);
            BinaryProcessor processor = this.processorList.stream()
                    .map(c -> BinaryProcessorBase.create(c, obj, field, fieldValue))
                    .filter(Objects::nonNull)
                    .filter(BinaryProcessor::realize).findFirst().orElse(null);

            if (processor != null) {
                processor.process(this.stream, o -> {
                    try {
                        read(o);
                    } catch (IOException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                if (fieldValue == null) {
                    field.set(obj, read(fieldType));
                } else {
                    read(fieldValue);
                }
            }
            pad(lastPos, field);
        }
    }

    public <T> T read(Class<T> clazz) throws IOException {
        T object;
        try {
            object = clazz.getConstructor().newInstance();
            read(object);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
        return object;
    }

    @SafeVarargs
    public final void registerProcessor(Class<? extends BinaryProcessor>... processors) {
        this.processorList.addAll(Arrays.asList(processors));
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
