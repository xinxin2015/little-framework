package cn.admin.beans;

import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ObjectUtils;

public class BeanMetadataAttribute implements BeanMetadataElement {

    private final String name;

    @Nullable
    private final Object value;

    @Nullable
    private Object source;

    public BeanMetadataAttribute(String name,@Nullable Object value) {
        Assert.notNull(name,"Name must not be null");
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public void setSource(@Nullable Object source) {
        this.source = source;
    }

    @Override
    @Nullable
    public Object getSource() {
        return this.source;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BeanMetadataAttribute)) {
            return false;
        }
        BeanMetadataAttribute otherMa = (BeanMetadataAttribute) other;
        return (this.name.equals(otherMa.name) &&
                ObjectUtils.nullSafeEquals(this.value, otherMa.value) &&
                ObjectUtils.nullSafeEquals(this.source, otherMa.source));
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
    }

    @Override
    public String toString() {
        return "metadata attribute '" + this.name + "'";
    }

}
