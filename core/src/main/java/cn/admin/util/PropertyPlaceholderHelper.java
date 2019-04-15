package cn.admin.util;

import cn.admin.lang.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class PropertyPlaceholderHelper {

    private static final Log logger = LogFactory.getLog(PropertyPlaceholderHelper.class);

    private static final Map<String,String> wellKnownSimplePrefixes = new HashMap<>(4);

    static {
        wellKnownSimplePrefixes.put("}", "{");
        wellKnownSimplePrefixes.put("]", "[");
        wellKnownSimplePrefixes.put(")", "(");
    }

    private final String placeholderPrefix;

    private final String placeholderSuffix;

    private final String simplePrefix;

    @Nullable
    private final String valueSeparator;

    private final boolean ignoreUnresolvablePlaceholders;

    public PropertyPlaceholderHelper(String placeholderPrefix,String placeholderSuffix) {
        this(placeholderPrefix,placeholderSuffix,null,true);
    }

    public PropertyPlaceholderHelper(String placeholderPrefix,String placeholderSuffix,
                                     @Nullable String valueSeparator,
                                     boolean ignoreUnresolvablePlaceholders) {
        Assert.notNull(placeholderPrefix,"'placeholderPrefix' must not be null");
        Assert.notNull(placeholderSuffix, "'placeholderSuffix' must not be null");
        this.placeholderPrefix = placeholderPrefix;
        this.placeholderSuffix = placeholderSuffix;
        String simplePrefixForSuffix = wellKnownSimplePrefixes.get(this.placeholderSuffix);
        if (simplePrefixForSuffix != null && this.placeholderPrefix.endsWith(simplePrefixForSuffix)) {
            this.simplePrefix = simplePrefixForSuffix;
        } else {
            this.simplePrefix = this.placeholderPrefix;
        }
        this.valueSeparator = valueSeparator;
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    public String replacePlaceholders(String value, final Properties properties) {
        Assert.notNull(properties,"'properties' must not be null");
        return replacePlaceholders(value, properties::getProperty);
    }

    public String replacePlaceholders(String value, PlaceholderResolver placeholderResolver) {
        Assert.notNull(value, "'value' must not be null");
        return parseStringValue(value, placeholderResolver, new HashSet<>());
    }

    protected String parseStringValue(String value, PlaceholderResolver placeholderResolver,
                                      Set<String> visitedPlaceholders) {
        StringBuilder result = new StringBuilder(value);

        int startIndex = value.indexOf(this.placeholderPrefix);
        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(result,startIndex);
            if (endIndex != -1) {
                String placeholder =
                        result.substring(startIndex + this.placeholderPrefix.length(),endIndex);
                String originalPlaceholder = placeholder;
                if (!visitedPlaceholders.add(originalPlaceholder)) {
                    throw new IllegalArgumentException(
                            "Circular placeholder reference '" + originalPlaceholder + "' in property definitions");
                }

                placeholder = parseStringValue(placeholder,placeholderResolver,visitedPlaceholders);

                String propVal = placeholderResolver.resolvePlaceholder(placeholder);
                if (propVal == null && this.valueSeparator != null) {
                    int separatorIndex = placeholder.indexOf(this.valueSeparator);
                    if (separatorIndex != -1) {
                        String actualPlaceholder = placeholder.substring(0,separatorIndex);
                        String defaultValue =
                                placeholder.substring(separatorIndex + this.valueSeparator.length());
                        propVal = placeholderResolver.resolvePlaceholder(actualPlaceholder);
                        if (propVal == null) {
                            propVal = defaultValue;
                        }
                    }
                }

                if (propVal != null) {
                    propVal = parseStringValue(propVal,placeholderResolver,visitedPlaceholders);
                    result.replace(startIndex,endIndex + this.placeholderSuffix.length(),propVal);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Resolved placeholder '" + placeholder + "'");
                    }
                    startIndex = result.indexOf(this.placeholderPrefix,
                            startIndex + propVal.length());
                } else if (this.ignoreUnresolvablePlaceholders) {
                    startIndex = result.indexOf(this.placeholderPrefix,
                            endIndex + this.placeholderSuffix.length());
                } else {
                    throw new IllegalArgumentException("Could not resolve placeholder '" +
                            placeholder + "'" + " in value \"" + value + "\"");
                }
                visitedPlaceholders.remove(originalPlaceholder);
            } else {
                startIndex = -1;
            }
        }
        return result.toString();
    }

    private int findPlaceholderEndIndex(CharSequence buf,int startIndex) {
        int index = startIndex + this.placeholderPrefix.length();
        int withinNestedPlaceholder = 0;
        while (index < buf.length()) {
            if (StringUtils.substringMatch(buf,index,this.placeholderSuffix)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder --;
                    index = index + this.placeholderSuffix.length();
                } else {
                    return index;
                }
            }  else if (StringUtils.substringMatch(buf,index,this.simplePrefix)) {
                withinNestedPlaceholder ++;
                index = index + this.simplePrefix.length();
            } else {
                index ++;
            }
        }
        return -1;
    }

    @FunctionalInterface
    public interface PlaceholderResolver {

        @Nullable
        String resolvePlaceholder(String placeholderName);

    }

}
