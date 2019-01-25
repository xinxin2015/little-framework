package cn.admin.core.annotation;

import cn.admin.lang.Nullable;
import cn.admin.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

class DefaultAnnotationAttributeExtractor extends AbstractAliasAwareAnnotationAttributeExtractor<Annotation> {

    DefaultAnnotationAttributeExtractor(Annotation annotation, @Nullable Object annotationElement) {
        super(annotation.annotationType(),annotationElement,annotation);
    }

    @Override
    @Nullable
    protected Object getRawAttributeValue(Method attributeMethod) {
        ReflectionUtils.makeAccessible(attributeMethod);
        return ReflectionUtils.invokeMethod(attributeMethod,getSource());
    }

    @Override
    @Nullable
    protected Object getRawAttributeValue(String attributeName) {
        Method attributeMethod = ReflectionUtils.findMethod(getAnnotationType(),attributeName);
        return (attributeMethod != null ? getRawAttributeValue(attributeMethod) : null);
    }
}
