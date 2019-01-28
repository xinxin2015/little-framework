package cn.admin.util;

import java.lang.reflect.Modifier;

public abstract class ClassUtils {

    public static boolean isInnerClass(Class<?> clazz) {
        return (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers()));
    }

}
