package to.kit.io.processor;

import to.kit.io.annotation.Allocate;
import to.kit.util.ValueUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public abstract class ArrayProcessor extends BinaryProcessorBase {
    protected final Allocate allocate;

    private void fillArray(Object array, Class<?> componentType, int length) {
        for (int ix = 0; ix < length; ix++) {
            try {
                Object instance = componentType.getConstructor().newInstance();
                Array.set(array, ix, instance);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    protected Object allocateArray(final int length, final boolean fill) {
        int paddedLength = ValueUtils.pad(length, this.allocate.padding());
        Class<?> componentType = this.field.getType().getComponentType();
        Object array = Array.newInstance(componentType, paddedLength);

        if (fill) {
            fillArray(array, componentType, paddedLength);
        }
        return array;
    }

    protected Object allocateArray(int length) {
        return allocateArray(length, false);
    }

    public ArrayProcessor(Object target, Field field, int index, Object parent) {
        super(target, field, index, parent);
        this.allocate = this.field == null ? null : this.field.getAnnotation(Allocate.class);
    }
}
