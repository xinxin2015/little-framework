package cn.admin.beans;

import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ObjectUtils;

import java.io.Serializable;

public class PropertyValue extends BeanMetadataAttributeAccessor implements Serializable {

    private final String name;

    @Nullable
    private final Object value;

    private boolean optional = false;

    private boolean converted = false;

    @Nullable
    private Object convertedValue;

    @Nullable
    volatile Boolean conversionNecessary;

    @Nullable
    transient volatile Object resolvedTokens;

    public PropertyValue(String name,@Nullable Object value) {
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

    public PropertyValue getOriginalPropertyValue() {
        PropertyValue original = this;
        Object source = getSource();
        while (source instanceof PropertyValue && source != original) {
            original = (PropertyValue) source;
            source = original.getSource();
        }
        return original;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isOptional() {
        return optional;
    }

    public synchronized boolean isConverted() {
        return converted;
    }

    public synchronized void setConvertedValue(@Nullable Object convertedValue) {
        this.converted = true;
        this.convertedValue = convertedValue;
    }

    @Nullable
    public synchronized Object getConvertedValue() {
        return convertedValue;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PropertyValue)) {
            return false;
        }
        PropertyValue otherPv = (PropertyValue) other;
        return (this.name.equals(otherPv.name) &&
                ObjectUtils.nullSafeEquals(this.value, otherPv.value) &&
                ObjectUtils.nullSafeEquals(getSource(), otherPv.getSource()));
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
    }

    @Override
    public String toString() {
        return "bean property '" + this.name + "'";
    }

}
