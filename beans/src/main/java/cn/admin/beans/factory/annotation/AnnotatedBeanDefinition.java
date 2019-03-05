package cn.admin.beans.factory.annotation;

import cn.admin.beans.factory.config.BeanDefinition;
import cn.admin.core.type.AnnotationMetadata;
import cn.admin.core.type.MethodMetadata;
import cn.admin.lang.Nullable;

public interface AnnotatedBeanDefinition extends BeanDefinition {

    AnnotationMetadata getMetadata();

    @Nullable
    MethodMetadata getFactoryMethodMetadata();

}
