package cn.admin.beans.factory.config;

import cn.admin.beans.BeansException;
import cn.admin.beans.factory.ListableBeanFactory;
import cn.admin.beans.factory.NoSuchBeanDefinitionException;
import cn.admin.lang.Nullable;

import java.util.Iterator;

public interface ConfigurableListableBeanFactory extends ListableBeanFactory,
        AutowireCapableBeanFactory,ConfigurableBeanFactory {

    void ignoreDependencyType(Class<?> type);

    void ignoreDependencyInterface(Class<?> ifc);

    void registerResolvableDependency(Class<?> dependencyType, @Nullable Object autowireValue);

    boolean isAutowireCandidate(String beanName,DependencyDescriptor descriptor) throws NoSuchBeanDefinitionException;

    BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

    Iterator<String> getBeanNamesIterator();

    void clearMetadataCache();

    void freezeConfiguration();

    boolean isConfigurationFrozen();

    void preInstantiateSingletons() throws BeansException;

}
