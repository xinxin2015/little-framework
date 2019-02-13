package cn.admin.beans.factory.config;

import cn.admin.beans.factory.ObjectFactory;
import cn.admin.lang.Nullable;

public interface Scope {

    Object get(String name, ObjectFactory<?> objectFactory);

    @Nullable
    Object remove(String name);

    void registerDestructionCallback(String name,Runnable callback);

    @Nullable
    Object resolveContextualObject(String key);

    @Nullable
    String getConversationId();

}
