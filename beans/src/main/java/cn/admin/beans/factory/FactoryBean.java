package cn.admin.beans.factory;

import cn.admin.lang.Nullable;

public interface FactoryBean<T> {

    @Nullable
    T getObject() throws Exception;

    @Nullable
    Class<?> getObjectType();

    default boolean isSingleton() {
        return true;
    }

}
