package cn.admin.util;

import cn.admin.lang.Nullable;

@FunctionalInterface
public interface StringValueResolver {

    @Nullable
    String resolveStringValue(String strVal);

}
