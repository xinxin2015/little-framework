package cn.admin.beans;

import cn.admin.core.MethodParameter;
import cn.admin.core.convert.TypeDescriptor;
import cn.admin.lang.Nullable;

import java.lang.reflect.Field;

public interface TypeConverter {

    @Nullable
    <T> T convertIfNecessary(@Nullable Object value,@Nullable Class<T> requiredType) throws TypeMismatchException;

    @Nullable
    <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
                             @Nullable MethodParameter methodPara) throws TypeMismatchException;

    @Nullable
    <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType, @Nullable Field field)
            throws TypeMismatchException;

    @Nullable
    default <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
                                     @Nullable TypeDescriptor typeDescriptor) throws TypeMismatchException {

        throw new UnsupportedOperationException("TypeDescriptor resolution not supported");
    }

}
