package cn.admin.core.io;

import cn.admin.lang.Nullable;

@FunctionalInterface
public interface ProtocolResolver {

    @Nullable
    Resource resolve(String location,ResourceLoader resourceLoader);

}
