package to.kit.io.processor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class BinaryProcessorBase implements BinaryProcessor {
    protected final Class<?> parentClass;
    protected final Object parent;
    protected final Field field;
    protected final Class<?> fieldType;

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

    public BinaryProcessorBase(Object parent, Field field, Object fieldValue) {
        this.parentClass = parent.getClass();
        this.parent = parent;
        this.field = field;
        this.fieldType = field.getType();
    }

    public static BinaryProcessor create(Class<? extends BinaryProcessor> clazz,
                                         Object parent, Field field, Object fieldValue) {
        try {
            return clazz.getConstructor(Object.class, Field.class, Object.class)
                    .newInstance(parent, field, fieldValue);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
