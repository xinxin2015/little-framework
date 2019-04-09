package cn.admin.util;

import cn.admin.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class StringUtils {

    public static boolean hasText(@Nullable CharSequence str) {
        return (str != null && str.length() > 0 && containsText(str));
    }

    public static boolean hasText(@Nullable String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0;i < strLen;i ++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String arrayToDelimitedString(@Nullable Object[] arr,String delim) {

        if (ObjectUtils.isEmpty(arr)) {
            return "";
        }

        if (arr.length == 1) {
            return ObjectUtils.nullSafeToString(arr[0]);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(delim);
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }

    public static String capitalize(String str) {
        return changeFirstCharacterCase(str, true);
    }

    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (!hasLength(str)) {
            return str;
        }

        char baseChar = str.charAt(0);
        char updatedChar;
        if (capitalize) {
            updatedChar = Character.toUpperCase(baseChar);
        }
        else {
            updatedChar = Character.toLowerCase(baseChar);
        }
        if (baseChar == updatedChar) {
            return str;
        }

        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars, 0, chars.length);
    }

    public static boolean hasLength(@Nullable CharSequence str) {
        return (str != null && str.length() > 0);
    }

    public static boolean hasLength(@Nullable String str) {
        return (str != null && !str.isEmpty());
    }

    public static String[] toStringArray(Collection<String> collection) {
        return collection.toArray(new String[0]);
    }

    public static String collectionToCommaDelimitedString(@Nullable Collection<?> coll) {
        return collectionToDelimitedString(coll,",");
    }

    public static String collectionToDelimitedString(@Nullable Collection<?> coll, String delim) {
        return collectionToDelimitedString(coll, delim, "", "");
    }

    public static String[] commaDelimitedListToStringArray(@Nullable String str) {
        return delimitedListToStringArray(str,",");
    }

    public static String[] delimitedListToStringArray(@Nullable String str,
                                                      @Nullable String delimiter) {
        return delimitedListToStringArray(str,delimiter,null);
    }

    public static String[] delimitedListToStringArray(@Nullable String str,
                                                      @Nullable String delimiter,
                                                      @Nullable String charsToDelete) {
        if (str == null) {
            return new String[0];
        }
        if (delimiter == null) {
            return new String[] {str};
        }

        List<String> result = new ArrayList<>();
        if (delimiter.isEmpty()) {
            for (int i = 0;i < str.length();i ++) {
                result.add(deleteAny(str.substring(i,i + 1),charsToDelete));
            }
        } else {
            int pos = 0;
            int delPos;
            while ((delPos = str.indexOf(delimiter, pos)) != -1) {
                result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                pos = delPos + delimiter.length();
            }
            if (str.length() > 0 && pos <= str.length()) {
                // Add rest of String, but not in case of empty input.
                result.add(deleteAny(str.substring(pos), charsToDelete));
            }
        }
        return toStringArray(result);
    }

    public static String collectionToDelimitedString(
            @Nullable Collection<?> coll, String delim, String prefix, String suffix) {

        if (CollectionUtils.isEmpty(coll)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Iterator<?> it = coll.iterator();
        while (it.hasNext()) {
            sb.append(prefix).append(it.next()).append(suffix);
            if (it.hasNext()) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

    public static String replace(String inString, String oldPattern, @Nullable String newPattern) {
        if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
            return inString;
        }
        int index = inString.indexOf(oldPattern);
        if (index == -1) {
            // no occurrence -> can return input as-is
            return inString;
        }

        int capacity = inString.length();
        if (newPattern.length() > oldPattern.length()) {
            capacity += 16;
        }
        StringBuilder sb = new StringBuilder(capacity);

        int pos = 0;  // our position in the old string
        int patLen = oldPattern.length();
        while (index >= 0) {
            sb.append(inString.substring(pos, index));
            sb.append(newPattern);
            pos = index + patLen;
            index = inString.indexOf(oldPattern, pos);
        }

        // append any characters to the right of a match
        sb.append(inString.substring(pos));
        return sb.toString();
    }

    public static String delete(String inString,String pattern) {
        return replace(inString,pattern,"");
    }

    public static String deleteAny(String inString,@Nullable String charsToDelete) {
        if (!hasLength(inString) || !hasLength(charsToDelete)) {
            return inString;
        }

        StringBuilder sb = new StringBuilder(inString.length());
        for (int i = 0;i < inString.length();i ++) {
            char c = inString.charAt(i);
            if (charsToDelete.indexOf(c) == -1) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String arrayToCommaDelimitedString(@Nullable Object[] arr) {
        return arrayToDelimitedString(arr,",");
    }

}
