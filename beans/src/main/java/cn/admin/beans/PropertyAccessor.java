package cn.admin.beans;

import cn.admin.core.convert.TypeDescriptor;
import cn.admin.lang.Nullable;

import java.util.Map;

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

    @Nullable
    TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException;

    @Nullable
    Object getPropertyValue(String propertyName) throws BeansException;

    void setPropertyValue(String propertyName,@Nullable Object value) throws BeansException;

    void setPropertyValue(PropertyValue pv) throws BeansException;

    void setPropertyValues(Map<?,?> map) throws BeansException;

    void setPropertyValues(PropertyValues pvs) throws BeansException;

    void setPropertyValues(PropertyValues pvs,boolean ignoreUnknown) throws BeansException;

    void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid)
            throws BeansException;

}
