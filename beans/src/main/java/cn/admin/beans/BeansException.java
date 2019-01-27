package cn.admin.beans;

import cn.admin.core.NestedRuntimeException;
import cn.admin.lang.Nullable;

public abstract class BeansException extends NestedRuntimeException {

    public BeansException(String msg) {
        super(msg);
    }

    public BeansException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
