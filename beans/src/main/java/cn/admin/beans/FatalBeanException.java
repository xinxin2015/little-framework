package cn.admin.beans;

import cn.admin.lang.Nullable;

public class FatalBeanException extends BeansException {

    public FatalBeanException(String msg) {
        super(msg);
    }

    public FatalBeanException(String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
