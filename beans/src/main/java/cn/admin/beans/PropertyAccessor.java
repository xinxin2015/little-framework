package cn.admin.beans;

import cn.admin.lang.Nullable;

public interface PropertyAccessor {

    String NESTED_PROPERTY_SEPARATOR = ".";

    char NESTED_PROPERTY_SEPARATOR_CHAR = '.';

    String PROPERTY_KEY_PREFIX = "[";

    char PROPERTY_KEY_PREFIX_CHAR = '[';

    String PROPERTY_KEY_SUFFIX = "]";

    char PROPERTY_KEY_SUFFIX_CHAR = ']';

    boolean isReadableProperty(String propertyName);

    boolean isWritableProperty(String propertyName);

    @Nullable
    Class<?> getPropertyType(String propertyName) throws BeansException;

    //TODO

}
