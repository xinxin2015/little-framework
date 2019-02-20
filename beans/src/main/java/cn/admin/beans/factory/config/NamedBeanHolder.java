package cn.admin.beans.factory.config;

import cn.admin.beans.factory.NamedBean;
import cn.admin.util.Assert;

public class NamedBeanHolder<T> implements NamedBean {

    private final String beanName;

    private final T beanInstance;

    public NamedBeanHolder(String beanName,T beanInstance) {
        Assert.notNull(beanName,"Bean name must not be null");
        this.beanName = beanName;
        this.beanInstance = beanInstance;
    }

    @Override
    public String getBeanName() {
        return beanName;
    }

    public T getBeanInstance() {
        return beanInstance;
    }
}
