package cn.admin.beans;

public abstract class PropertyAccessorUtils {

    public static String getPropertyName(String propertyPath) {
        int separatorIndex = propertyPath.endsWith(PropertyAccessor.PROPERTY_KEY_SUFFIX) ?
                propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) : -1;
        return separatorIndex != -1 ? propertyPath.substring(0, separatorIndex) : propertyPath;
    }

    public static int getFirstNestedPropertySeparatorIndex(String propertyPath) {
        return getNestedPropertySeparatorIndex(propertyPath, false);
    }

    public static int getLastNestedPropertySeparatorIndex(String propertyPath) {
        return getNestedPropertySeparatorIndex(propertyPath, true);
    }

    private static int getNestedPropertySeparatorIndex(String propertyPath, boolean last) {
        boolean inKey = false;
        int length = propertyPath.length();
        int i = last ? length - 1 : 0;
        while (last ? i >= 0 : i < length) {
            switch (propertyPath.charAt(i)) {
                case PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR:
                case PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR:
                    inKey = !inKey;
                    break;
                case PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR:
                    if (!inKey) {
                        return i;
                    }
            }
            if (last) {
                i --;
            } else {
                i ++;
            }
        }
        return -1;
    }

    public static boolean matchesProperty(String registeredPath, String propertyPath) {
        if (!registeredPath.startsWith(propertyPath)) {
            return false;
        }
        if (registeredPath.length() == propertyPath.length()) {
            return true;
        }
        if (registeredPath.charAt(propertyPath.length()) != PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR) {
            return false;
        }
        return (registeredPath.indexOf(PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR,
                propertyPath.length() + 1) ==
                registeredPath.length() - 1);
    }

}
