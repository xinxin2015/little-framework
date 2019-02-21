package cn.admin.util;

import cn.admin.lang.Nullable;

import java.util.Collection;
import java.util.Map;

public abstract class CollectionUtils {

    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isEmpty(@Nullable Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

}
