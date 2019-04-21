package cn.admin.core.convert;

import cn.admin.core.convert.converter.ConversionException;
import cn.admin.lang.Nullable;

public class ConverterNotFoundException extends ConversionException {

    @Nullable
    private final TypeDescriptor sourceType;

    private final TypeDescriptor targetType;

    public ConverterNotFoundException(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType) {
        super("No converter found capable of converting from type [" + sourceType + "] to type [" + targetType + "]");
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    @Nullable
    public TypeDescriptor getSourceType() {
        return sourceType;
    }

    public TypeDescriptor getTargetType() {
        return targetType;
    }
}
