package cn.admin.beans.factory.support;

import cn.admin.beans.BeanMetadataElement;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ObjectUtils;

import java.lang.reflect.Method;

public abstract class MethodOverride implements BeanMetadataElement {

    private final String methodName;

    private boolean overloaded = true;

    @Nullable
    private Object source;

    protected MethodOverride(String methodName) {
        Assert.notNull(methodName,"Method name must not be null");
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setOverloaded(boolean overloaded) {
        this.overloaded = overloaded;
    }

    public boolean isOverloaded() {
        return overloaded;
    }

    public void setSource(@Nullable Object source) {
        this.source = source;
    }

    @Override
    @Nullable
    public Object getSource() {
        return source;
    }

    public abstract boolean matches(Method method);

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MethodOverride)) {
            return false;
        }
        MethodOverride that = (MethodOverride) other;
        return (ObjectUtils.nullSafeEquals(this.methodName, that.methodName) &&
                ObjectUtils.nullSafeEquals(this.source, that.source));
    }

    @Override
    public int hashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(this.methodName);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.source);
        return hashCode;
    }
}
