package cn.admin.core.env;

import cn.admin.core.convert.support.ConfigurableConversionService;
import cn.admin.lang.Nullable;

public interface ConfigurablePropertyResolver extends PropertyResolver {

    ConfigurableConversionService getConversionService();

    void setConversionService(ConfigurableConversionService conversionService);

    void setPlaceholderPrefix(String placeholderPrefix);

    void setPlaceholderSuffix(String placeholderSuffix);

    void setValueSeparator(@Nullable String valueSeparator);

    void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders);

    void setRequiredProperties(String ...requiredProperties);

    void validateRequiredProperties() throws MissingRequiredPropertiesException;

}
