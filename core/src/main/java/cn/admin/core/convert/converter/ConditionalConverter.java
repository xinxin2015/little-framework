package cn.admin.core.convert.converter;

import cn.admin.core.convert.TypeDescriptor;

public interface ConditionalConverter {

    boolean matches(TypeDescriptor sourceType,TypeDescriptor targetType);

}
