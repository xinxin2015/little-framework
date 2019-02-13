package cn.admin.core.convert;

import cn.admin.lang.Nullable;

public interface ConversionService {

    boolean canConvert(@Nullable Class<?> sourceType,Class<?> targetType);

    boolean canConvert(@Nullable TypeDescriptor sourceType,TypeDescriptor targetType);

    @Nullable
    <T> T convert(@Nullable Object source,Class<T> targetType);

    @Nullable
    Object convert(@Nullable Object source,@Nullable TypeDescriptor sourceType,TypeDescriptor targetType);

}
