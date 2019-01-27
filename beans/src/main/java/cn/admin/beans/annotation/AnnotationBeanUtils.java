package cn.admin.beans.annotation;

import cn.admin.lang.Nullable;
import cn.admin.util.StringValueResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class AnnotationBeanUtils {

    public static void copyPropertiesToBean(Annotation ann, Object bean,
                                            @Nullable StringValueResolver valueResolver,
                                            String ...excludeProperties) {
        Set<String> excluded = new HashSet<>(Arrays.asList(excludeProperties));
        Method[] annotationProperties = ann.annotationType().getDeclaredMethods();
        //TODO
    }

}
