package cn.admin.beans.factory;

import cn.admin.lang.Nullable;
import cn.admin.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BeanFactoryUtils {

    public static final String GENERATED_BEAN_NAME_SEPARATOR = "#";

    private static final Map<String,String> transformedBeanNameCache = new ConcurrentHashMap<>();

    public static String transformedBeanName(String name) {
        Assert.notNull(name,"'name' must not be null");
        if (!name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
            return name;
        }
        return transformedBeanNameCache.computeIfAbsent(name, beanName -> {
           do {
               beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
           } while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
           return beanName;
        });
    }

    public static boolean isFactoryDereference(@Nullable String name) {
        return name != null && name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX);
    }

}
