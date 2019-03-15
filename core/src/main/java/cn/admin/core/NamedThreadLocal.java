package cn.admin.core;

import cn.admin.util.Assert;

public class NamedThreadLocal<T> extends ThreadLocal<T> {

    private final String name;

    public NamedThreadLocal(String name) {
        Assert.hasText(name,"Name must not be null");
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
