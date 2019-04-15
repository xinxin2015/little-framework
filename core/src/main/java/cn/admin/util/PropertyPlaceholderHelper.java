package cn.admin.util;

import cn.admin.lang.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

    }

    protected String parseStringValue(String value, PlaceholderResolver placeholderResolver,
                                      Set<String> visitedPlaceholders) {

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
