package cn.admin.core.annotation;

import cn.admin.lang.Nullable;
import cn.admin.util.Assert;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

abstract class AbstractAliasAwareAnnotationAttributeExtractor<S> implements AnnotationAttributeExtractor<S> {

    private final Class<? extends Annotation> annotationType;

    @Nullable
    private final Object annotatedElement;

    private final S source;

    private final Map<String, List<String>> attributeAliasMap;

    AbstractAliasAwareAnnotationAttributeExtractor(Class<? extends Annotation> annotationType,
                                                   @Nullable Object annotatedElement,S source) {
        Assert.notNull(annotationType, "annotationType must not be null");
        Assert.notNull(source, "source must not be null");
        this.annotationType = annotationType;
        this.annotatedElement = annotatedElement;
        this.source = source;
        this.attributeAliasMap = null;//TODO
    }

}
