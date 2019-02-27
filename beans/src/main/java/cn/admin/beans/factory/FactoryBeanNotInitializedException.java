package cn.admin.beans.factory;

import cn.admin.beans.FatalBeanException;

public class FactoryBeanNotInitializedException extends FatalBeanException {


    public FactoryBeanNotInitializedException(String msg) {
        super(msg);
    }

    public FactoryBeanNotInitializedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
