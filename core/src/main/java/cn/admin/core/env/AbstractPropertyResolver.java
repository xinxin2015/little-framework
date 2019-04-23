package cn.admin.core.env;

import cn.admin.core.convert.ConversionService;
import cn.admin.core.convert.support.ConfigurableConversionService;
import cn.admin.core.convert.support.DefaultConversionService;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ClassUtils;
import cn.admin.util.PropertyPlaceholderHelper;
import cn.admin.util.SystemPropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractPropertyResolver implements ConfigurablePropertyResolver {

    protected final Log logger = LogFactory.getLog(getClass());

    @Nullable
    private volatile ConfigurableConversionService conversionService;

    @Nullable
    private PropertyPlaceholderHelper nonStrictHelper;

    @Nullable
    private PropertyPlaceholderHelper strictHelper;

    private boolean ignoreUnresolvableNestedPlaceholders = false;

    private String placeholderPrefix = SystemPropertyUtils.PLACEHOLDER_PREFIX;

    private String placeholderSuffix = SystemPropertyUtils.PLACEHOLDER_SUFFIX;

    @Nullable
    private String valueSeparator = SystemPropertyUtils.VALUE_SEPARATOR;

    private final Set<String> requiredProperties = new LinkedHashSet<>();

    @Override
    public ConfigurableConversionService getConversionService() {
        ConfigurableConversionService cs = this.conversionService;
        if (cs == null) {
            synchronized (this) {
                cs = this.conversionService;
                if (cs == null) {
                    cs = new DefaultConversionService();
                    this.conversionService = cs;
                }
            }
        }
        return cs;
    }

    @Override
    public void setConversionService(@Nullable ConfigurableConversionService conversionService) {
        Assert.notNull(conversionService,"ConversionService must not be null");
        this.conversionService = conversionService;
    }

    @Override
    public void setPlaceholderPrefix(String placeholderPrefix) {
        Assert.notNull(placeholderPrefix,"placeholderPrefix must not be null");
        this.placeholderPrefix = placeholderPrefix;
    }

    @Override
    public void setPlaceholderSuffix(String placeholderSuffix) {
        Assert.notNull(placeholderSuffix,"placeholderSuffix must not be null");
        this.placeholderSuffix = placeholderSuffix;
    }

    @Override
    public void setValueSeparator(@Nullable String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    @Override
    public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
        this.ignoreUnresolvableNestedPlaceholders = ignoreUnresolvableNestedPlaceholders;
    }

    @Override
    public void setRequiredProperties(String... requiredProperties) {
        this.requiredProperties.addAll(Arrays.asList(requiredProperties));
    }

    @Override
    public void validateRequiredProperties() {
        MissingRequiredPropertiesException ex = new MissingRequiredPropertiesException();
        for (String key : this.requiredProperties) {
            if (this.getProperty(key) == null) {
                ex.addMissingRequiredProperty(key);
            }
        }
        if (!ex.getMissingRequiredProperties().isEmpty()) {
            throw ex;
        }
    }

    @Override
    public boolean containsProperty(String key) {
        return getProperty(key) != null;
    }

    @Override
    public String getProperty(String key) {
        return getProperty(key,String.class);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        T value = getProperty(key,targetType);
        return value != null ? value : defaultValue;
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        String value = getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Required key '" + key + "' not found");
        }
        return value;
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> valueType) throws IllegalStateException {
        T value = getProperty(key, valueType);
        if (value == null) {
            throw new IllegalStateException("Required key '" + key + "' not found");
        }
        return value;
    }

    @Override
    public String resolvePlaceholders(String text) {
        if (this.nonStrictHelper == null) {
            this.nonStrictHelper = createPlaceholderHelper(true);
        }
        return doResolvePlaceholders(text,nonStrictHelper);
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        if (this.strictHelper == null) {
            this.strictHelper = createPlaceholderHelper(false);
        }
        return doResolvePlaceholders(text,strictHelper);
    }

    protected String resolveNestedPlaceholders(String value) {
        return (this.ignoreUnresolvableNestedPlaceholders ?
                resolvePlaceholders(value) : resolveRequiredPlaceholders(value));
    }

    private PropertyPlaceholderHelper createPlaceholderHelper(boolean ignoreUnresolvablePlaceholders) {
        return new PropertyPlaceholderHelper(this.placeholderPrefix,this.placeholderSuffix,
                this.valueSeparator,ignoreUnresolvablePlaceholders);
    }

    private String doResolvePlaceholders(String text,PropertyPlaceholderHelper helper) {
        return helper.replacePlaceholders(text, this::getPropertyAsRawString);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <T> T convertValueIfNecessary(Object value,@Nullable Class<T> targetType) {
        if (targetType == null) {
            return (T) value;
        }
        ConversionService conversionServiceToUse = this.conversionService;
        if (conversionServiceToUse == null) {
            if (ClassUtils.isAssignableValue(targetType,value)) {
                return (T) value;
            }
            conversionServiceToUse = DefaultConversionService.getSharedInstance();
        }
        return conversionServiceToUse.convert(value,targetType);
    }

    @Nullable
    protected abstract String getPropertyAsRawString(String key);
}
