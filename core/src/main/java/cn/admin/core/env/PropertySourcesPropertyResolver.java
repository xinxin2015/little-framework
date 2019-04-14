package cn.admin.core.env;

import cn.admin.core.convert.support.ConfigurableConversionService;
import cn.admin.lang.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertySourcesPropertyResolver implements ConfigurablePropertyResolver {

    protected final Log logger = LogFactory.getLog(getClass());

    @Nullable
    private volatile ConfigurableConversionService conversionService;

    //todo

    @Override
    public boolean containsProperty(String key) {
        return false;
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return null;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        return null;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return null;
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        return null;
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        return null;
    }

    @Override
    public String resolvePlaceholders(String text) {
        return null;
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return null;
    }
}
