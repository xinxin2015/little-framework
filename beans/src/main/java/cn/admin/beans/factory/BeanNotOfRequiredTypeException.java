package cn.admin.beans.factory;

import cn.admin.beans.BeansException;
import cn.admin.util.ClassUtils;

public class BeanNotOfRequiredTypeException extends BeansException {


    private final String beanName;

    private final Class<?> requiredType;

    private final Class<?> actualType;

    public BeanNotOfRequiredTypeException(String beanName,Class<?> requiredType,
                                          Class<?> actualType) {
        super("Bean named '" + beanName + "' is expected to be of type '" + ClassUtils.getQualifiedName(requiredType) +
                "' but was actually of type '" + ClassUtils.getQualifiedName(actualType) + "'");
        this.beanName = beanName;
        this.requiredType = requiredType;
        this.actualType = actualType;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getActualType() {
        return actualType;
    }

    public Class<?> getRequiredType() {
        return requiredType;
    }
}
