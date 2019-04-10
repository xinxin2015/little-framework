package cn.admin.core.env;

import cn.admin.lang.Nullable;

public interface PropertyResolver {

    boolean containsProperty(String key);

    @Nullable
    String getProperty(String key);

    String getProperty(String key,String defaultValue);

    @Nullable
    <T> T getProperty(String key,Class<T> targetType);

    <T> T getProperty(String key,Class<T> targetType,T defaultValue);

    String getRequiredProperty(String key) throws IllegalStateException;

    <T> T getRequiredProperty(String key,Class<T> targetType) throws IllegalStateException;

    String resolvePlaceholders(String text);

    String resolveRequiredPlaceholders(String text) throws IllegalArgumentException;

}
