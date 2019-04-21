package cn.admin.core.convert.support;

import cn.admin.core.convert.ConversionService;
import cn.admin.core.convert.TypeDescriptor;
import cn.admin.core.convert.converter.ConversionFailedException;
import cn.admin.core.convert.converter.GenericConverter;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ClassUtils;

abstract class ConversionUtils {

    @Nullable
    static Object invokeConverter(GenericConverter converter, @Nullable Object source,
                                  TypeDescriptor sourceType,TypeDescriptor targetType) {
        try {
            return converter.convert(source, sourceType, targetType);
        } catch (ConversionFailedException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new ConversionFailedException(sourceType,targetType,source,ex);
        }
    }

    static boolean canConvertElements(@Nullable TypeDescriptor sourceElementType,
                                      @Nullable TypeDescriptor targetElementType, ConversionService conversionService) {

        if (targetElementType == null) {
            // yes
            return true;
        }
        if (sourceElementType == null) {
            // maybe
            return true;
        }
        if (conversionService.canConvert(sourceElementType, targetElementType)) {
            // yes
            return true;
        }
        if (ClassUtils.isAssignable(sourceElementType.getType(), targetElementType.getType())) {
            // maybe
            return true;
        }
        // no
        return false;
    }

    static Class<?> getEnumType(Class<?> targetType) {
        Class<?> enumType = targetType;
        while (enumType != null && !enumType.isEnum()) {
            enumType = enumType.getSuperclass();
        }
        Assert.notNull(enumType, () -> "The target type " + targetType.getName() + " does not refer to an enum");
        return enumType;
    }

}
