package cn.admin.beans.factory.support;

import cn.admin.beans.FatalBeanException;

public class BeanDefinitionValidationException extends FatalBeanException {

    public BeanDefinitionValidationException(String msg) {
        super(msg);
    }

    public BeanDefinitionValidationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
