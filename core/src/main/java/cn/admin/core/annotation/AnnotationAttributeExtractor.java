package cn.admin.core.annotation;

import cn.admin.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

interface AnnotationAttributeExtractor<S> {

    Class<? extends Annotation> getAnnotationType();

    @Nullable
    Object getAnnotatedElement();

    S getSource();

    @Nullable
    Object getAttributeValue(Method attributeMethod);

}
