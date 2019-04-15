package cn.admin.util;

import cn.admin.lang.Nullable;

public abstract class SystemPropertyUtils {

    public static final String PLACEHOLDER_PREFIX = "${";

    public static final String PLACEHOLDER_SUFFIX = "}";

    public static final String VALUE_SEPARATOR = ":";

    private static final PropertyPlaceholderHelper strictHelper =
            new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX,PLACEHOLDER_SUFFIX,VALUE_SEPARATOR,
                    false);

    private static final PropertyPlaceholderHelper nonStrictHelper =
            new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX,PLACEHOLDER_SUFFIX,VALUE_SEPARATOR,
                    true);

    public static String resolvePlaceholders(String text) {
        return resolvePlaceholders(text,false);
    }

    public static String resolvePlaceholders(String text,boolean ignoreUnresolvablePlaceholders) {
        PropertyPlaceholderHelper helper = ignoreUnresolvablePlaceholders ? nonStrictHelper :
                strictHelper;
        return helper.replacePlaceholders(text,new SystemPropertyPlaceholderResolver(text));
    }

    private static class SystemPropertyPlaceholderResolver implements PropertyPlaceholderHelper.PlaceholderResolver {

        private final String text;

        private SystemPropertyPlaceholderResolver(String text) {
            this.text = text;
        }

        @Override
        @Nullable
        public String resolvePlaceholder(String placeholderName) {
            try {
                String propVal = System.getProperty(placeholderName);
                if (propVal == null) {
                    propVal = System.getenv(placeholderName);
                }
                return propVal;
            } catch (Throwable ex) {
                System.err.println("Could not resolve placeholder '" + placeholderName + "' in [" +
                        this.text + "] as system property: " + ex);
                return null;
            }
        }
    }

}
