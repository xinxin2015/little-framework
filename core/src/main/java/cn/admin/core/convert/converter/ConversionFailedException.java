package cn.admin.core.convert.converter;

import cn.admin.core.convert.TypeDescriptor;
import cn.admin.lang.Nullable;
import cn.admin.util.ObjectUtils;

public class ConversionFailedException extends ConversionException {

    @Nullable
    private final TypeDescriptor sourceType;

    private final TypeDescriptor targetType;

    @Nullable
    private final Object value;

    public ConversionFailedException(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType,
                                     @Nullable Object value, Throwable cause) {

        super("Failed to convert from type [" + sourceType + "] to type [" + targetType +
                "] for value '" + ObjectUtils.nullSafeToString(value) + "'", cause);
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.value = value;
    }

    @Nullable
    public TypeDescriptor getSourceType() {
        return sourceType;
    }

    public TypeDescriptor getTargetType() {
        return targetType;
    }

    @Nullable
    public Object getValue() {
        return value;
    }
}
