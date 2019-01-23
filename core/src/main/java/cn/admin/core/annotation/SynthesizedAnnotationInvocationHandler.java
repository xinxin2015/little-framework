package cn.admin.core.annotation;

import cn.admin.util.Assert;
import cn.admin.util.ObjectUtils;
import cn.admin.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SynthesizedAnnotationInvocationHandler implements InvocationHandler {

    private final AnnotationAttributeExtractor<?> attributeExtractor;

    private final Map<String,Object> valueCache = new ConcurrentHashMap<>(8);

    SynthesizedAnnotationInvocationHandler(AnnotationAttributeExtractor<?> attributeExtractor) {
        Assert.notNull(attributeExtractor,"AnnotationAttributeExtractor must not be null");
        this.attributeExtractor = attributeExtractor;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }

    private Class<? extends Annotation> annotationType() {
        return this.attributeExtractor.getAnnotationType();
    }

    private Object getAttributeValue(Method attributeMethod) {
        String attributeName = attributeMethod.getName();
        Object value = this.valueCache.get(attributeName);
        if (value == null) {
            value = this.attributeExtractor.getAttributeValue(attributeMethod);
            if (value == null) {
                String msg = String.format("%s returned null for attribute name [%s] from attribute source [%s]",
                        this.attributeExtractor.getClass().getName(), attributeName, this.attributeExtractor.getSource());
                throw new IllegalStateException(msg);
            }

            if (value instanceof Annotation) {
                //TODO
            }
        }
        return null;
    }

    private Object cloneArray(Object array) {
        if (array instanceof boolean[]) {
            return ((boolean[]) array).clone();
        }
        if (array instanceof byte[]) {
            return ((byte[]) array).clone();
        }
        if (array instanceof char[]) {
            return ((char[]) array).clone();
        }
        if (array instanceof double[]) {
            return ((double[]) array).clone();
        }
        if (array instanceof float[]) {
            return ((float[]) array).clone();
        }
        if (array instanceof int[]) {
            return ((int[]) array).clone();
        }
        if (array instanceof long[]) {
            return ((long[]) array).clone();
        }
        if (array instanceof short[]) {
            return ((short[]) array).clone();
        }

        // else
        return ((Object[]) array).clone();
    }

    private boolean annotationEquals(Object other) {
        if (this == other) {
            return true;
        }
        if (!annotationType().isInstance(other)) {
            return false;
        }

        for (Method attributeMethod : AnnotationUtils.getAttributeMethods(annotationType())) {
            Object thisValue = getAttributeValue(attributeMethod);
            Object otherValue = null;//TODO
            if (!ObjectUtils.nullSafeEquals(thisValue,otherValue)) {
                return false;
            }
        }
        return true;
    }

}
