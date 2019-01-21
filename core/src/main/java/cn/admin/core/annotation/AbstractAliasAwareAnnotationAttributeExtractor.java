package cn.admin.core.annotation;

import cn.admin.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

abstract class AbstractAliasAwareAnnotationAttributeExtractor<S> implements AnnotationAttributeExtractor<S> {

    private final Class<? extends Annotation> annotationType;

    @Nullable
    private final Object annotatedElement;

    private final S source;

    private final Map<String, List<String>> attributeAliasMap;

}
