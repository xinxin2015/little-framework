package cn.admin.beans.factory;

import cn.admin.beans.BeansException;
import cn.admin.core.ResolvableType;
import cn.admin.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface ListableBeanFactory extends BeanFactory {

    boolean containsBeanDefinition(String beanName);

    int getBeanDefinitionCount();

    String[] getBeanDefinitionNames();

    String[] getBeanNamesForType(ResolvableType type);

    String[] getBeanNamesForType(@Nullable Class<?> type);

    String[] getBeanNamesForType(@Nullable Class<?> type,boolean includeNonSingletons,boolean allowEagerInit);

    <T> Map<String,T> getBeansOfType(@Nullable Class<?> type) throws BeansException;

    <T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
            throws BeansException;

    String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);

    Map<String,Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

    @Nullable
    <A extends Annotation> A findAnnotationOnBeans(String beanName,Class<A> annotationType) throws NoSuchBeanDefinitionException;

}
