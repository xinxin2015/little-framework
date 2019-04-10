package cn.admin.core.io;

import cn.admin.lang.Nullable;
import cn.admin.util.ResourceUtils;

public interface ResourceLoader {

    String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;

    Resource getResource(String location);

    @Nullable
    ClassLoader getClassLoader();

}
