package cn.admin.core.convert.converter;

import cn.admin.core.NestedRuntimeException;

public class ConversionException extends NestedRuntimeException {

    public ConversionException(String msg) {
        super(msg);
    }

    public ConversionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
