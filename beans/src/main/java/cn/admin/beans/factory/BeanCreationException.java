package cn.admin.beans.factory;

import cn.admin.beans.FatalBeanException;
import cn.admin.lang.Nullable;

import java.util.List;

public class BeanCreationException extends FatalBeanException {

    @Nullable
    private final String beanName;

    @Nullable
    private final String resourceDescription;

    @Nullable
    private List<Throwable> relatedCauses;

    public BeanCreationException(String msg) {
        super(msg);
        this.beanName = null;
        this.resourceDescription = null;
    }

    public BeanCreationException(String msg, Throwable cause) {
        super(msg, cause);
        this.beanName = null;
        this.resourceDescription = null;
    }

}
