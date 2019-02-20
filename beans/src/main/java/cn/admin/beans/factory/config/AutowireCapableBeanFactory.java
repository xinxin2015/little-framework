package cn.admin.beans.factory.config;

import cn.admin.beans.BeansException;
import cn.admin.beans.factory.BeanFactory;
import cn.admin.lang.Nullable;

public interface AutowireCapableBeanFactory extends BeanFactory {

    int AUTOWIRE_NO = 0;

    int AUTOWIRE_BY_NAME = 1;

    int AUTOWIRE_BY_TYPE = 2;

    int AUTOWIRE_CONSTRUCTOR = 3;

    @Deprecated
    int AUTOWIRE_AUTODETECT = 4;

    String ORIGINAL_INSTANCE_SUFFIX = ".ORIGINAL";

    <T> T createBean(Class<T> beanClass) throws BeansException;

    void autowireBean(Object existBean) throws BeansException;

    Object configureBean(Object existBean,String beanName) throws BeansException;

    Object createBean(Class<?> beanClass,int autowireMode,boolean dependencyCheck) throws BeansException;

    Object autowire(Class<?> beanClass,int autowireMode,boolean dependencyCheck) throws BeansException;

    void autowireBeanProperties(Object existBean,int autowireMode,boolean dependencyCheck) throws BeansException;

    void applyBeanPropertyValues(Object existingBean,String beanName) throws BeansException;

    Object initializeBean(Object existingBean,String beanName) throws BeansException;

    Object applyBeanPostProcessorsBeforeInitialization(Object existingBean,String beanName) throws BeansException;

    Object applyBeanPostProcessorAfterInitialization(Object existingBean,String beanName) throws BeansException;

    void destroyBean(Object existingBean);

    <T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException;

    @Nullable
    Object resolveDependency() throws BeansException;//TODO



}
