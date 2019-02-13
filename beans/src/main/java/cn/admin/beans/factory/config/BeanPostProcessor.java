package cn.admin.beans.factory.config;

import cn.admin.beans.BeansException;
import cn.admin.lang.Nullable;

public interface BeanPostProcessor {

    @Nullable
    default Object postProcessBeforeInitialization(Object bean,String beanName) throws BeansException {
        return bean;
    }

    @Nullable
    default Object postProcessAfterInitialization(Object bean,String beanName) throws BeansException {
        return bean;
    }

}
