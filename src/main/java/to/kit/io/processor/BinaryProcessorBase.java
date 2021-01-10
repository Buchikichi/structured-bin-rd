package to.kit.io.processor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class BinaryProcessorBase implements BinaryProcessor {
    protected final Class<?> parentClass;
    protected final Object parent;
    protected final Field field;
    protected final Class<?> fieldType;

    protected <T> T getFieldValue(Class<T> clazz) {
        try {
            return clazz.cast(this.field.get(this.parent));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected int getValueByName(String name) {
        int value = 0;
        char upper = Character.toUpperCase(name.charAt(0));
        String methodName = "get" + upper + name.substring(1);

        try {
            Method getter = this.parentClass.getMethod(methodName);
            if (getter.getReturnType() == int.class) {
                value = Math.abs((int) getter.invoke(this.parent));
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return value;
    }

    public BinaryProcessorBase(Object parent, Field field) {
        this.parentClass = parent == null ? null : parent.getClass();
        this.parent = parent;
        this.field = field;
        this.fieldType = this.field == null ? null : field.getType();
    }

    public static BinaryProcessor create(Class<? extends BinaryProcessor> clazz,
                                         Object parent, Field field) {
        try {
            return clazz.getConstructor(Object.class, Field.class)
                    .newInstance(parent, field);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
