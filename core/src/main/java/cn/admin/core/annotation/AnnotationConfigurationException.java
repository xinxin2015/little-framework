package cn.admin.core.annotation;

import cn.admin.core.NestedRuntimeException;

public class AnnotationConfigurationException extends NestedRuntimeException {

    public AnnotationConfigurationException(String msg) {
        super(msg);
    }

    public AnnotationConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
