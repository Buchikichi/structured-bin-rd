package to.kit.io.processor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ObjectArrayProcessor extends ArrayProcessor {
    @Override
    public boolean realize() {
        return this.fieldType != null && this.fieldType.isArray();
    }

    @Override
    public boolean process(InputStream stream, Consumer<Object> consumer) throws IOException {
        Object[] objects = getFieldValue(Object[].class);
        int remain = stream.available();

        if (objects == null) {
            if (this.allocate == null) {
                return false;
            }
            if (!this.allocate.count().isEmpty()) {
                int count = getValueByName(this.allocate.count());

                objects = (Object[]) allocateArray(count, true);
                try {
                    this.field.set(this.parent, objects);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                Arrays.stream(objects).forEach(consumer);
                return true;
            }
            int length = getValueByName(this.allocate.value());
            int available = remain;
            Class<?> componentType = this.fieldType.getComponentType();
            List<Object> list = new ArrayList<>();

            while (remain - available < length) {
                try {
                    Object instance = componentType.getConstructor().newInstance();

                    consumer.accept(instance);
                    list.add(instance);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                available = stream.available();
            }
            int size = list.size();
            Object array = Array.newInstance(componentType, size);
            for (int ix = 0; ix < size; ix++) {
                Array.set(array, ix, list.get(ix));
            }
            try {
                this.field.set(this.parent, array);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public ObjectArrayProcessor(Object parent, Field field) {
        super(parent, field);
    }
}
