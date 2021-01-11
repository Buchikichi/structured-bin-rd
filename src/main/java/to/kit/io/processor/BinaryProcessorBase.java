package to.kit.io.processor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class BinaryProcessorBase implements BinaryProcessor {
    protected final Object target;
    protected final Field field;
    protected final Class<?> fieldType;
    protected final int index;
    protected final Object parent;

    @Deprecated
    protected <T> T getFieldValue(Class<T> clazz) {
        try {
            return clazz.cast(this.field.get(this.target));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected int getValueByName(String name) {
        int value = 0;
        Class<?> parentClass = this.parent.getClass();
        char upper = Character.toUpperCase(name.charAt(0));
        String methodName = "get" + upper + name.substring(1);

        try {
            Method getter = parentClass.getMethod(methodName);
            if (getter.getReturnType() == int.class) {
                value = Math.abs((int) getter.invoke(this.parent));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return value;
    }

    public BinaryProcessorBase(Object target, Field field, int index, Object parent) {
        this.target = target;
        this.field = field;
        this.fieldType = this.field == null ? null : field.getType();
        this.index = index;
        this.parent = parent;
    }

    public static BinaryProcessor create(Class<? extends BinaryProcessor> clazz,
                                         Object target, Field field, int index, Object parent) {
        try {
            return clazz.getConstructor(Object.class, Field.class, int.class, Object.class)
                    .newInstance(target, field, index, parent);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
