package cn.admin.beans.factory.annotation;

import cn.admin.beans.factory.support.GenericBeanDefinition;
import cn.admin.core.type.AnnotationMetadata;
import cn.admin.core.type.MethodMetadata;
import cn.admin.core.type.StandardAnnotationMetadata;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;

public class AnnotatedGenericBeanDefinition extends GenericBeanDefinition implements AnnotatedBeanDefinition {

    private final AnnotationMetadata metadata;

    @Nullable
    private MethodMetadata factoryMethodMetadata;

    public AnnotatedGenericBeanDefinition(Class<?> beanClass) {
        setBeanClass(beanClass);
        this.metadata = new StandardAnnotationMetadata(beanClass,true);
    }

    public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata) {
        Assert.notNull(metadata,"AnnotationMetadata must not be null");
        if (metadata instanceof StandardAnnotationMetadata) {
            setBeanClass(((StandardAnnotationMetadata) metadata).getIntrospectedClass());
        } else {
            setBeanClassName(metadata.getClassName());
        }
        this.metadata = metadata;
    }

    public AnnotatedGenericBeanDefinition(AnnotationMetadata metadata,
                                          MethodMetadata factoryMethodMetadata) {
        this(metadata);
        Assert.notNull(factoryMethodMetadata,"MethodMetadata must not be null");
        setFactoryMethodName(factoryMethodMetadata.getMethodName());
        this.factoryMethodMetadata = factoryMethodMetadata;
    }

    @Override
    public AnnotationMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    public MethodMetadata getFactoryMethodMetadata() {
        return this.factoryMethodMetadata;
    }
}
