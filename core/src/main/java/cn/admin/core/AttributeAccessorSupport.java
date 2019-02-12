package cn.admin.core;

import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.StringUtils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AttributeAccessorSupport implements AttributeAccessor, Serializable {

    private final Map<String,Object> attributes = new LinkedHashMap<>();

    @Override
    public void setAttribute(String name, Object value) {
        Assert.notNull(name,"Name must not be null");
        if (value != null) {
            this.attributes.put(name,value);
        } else {
            removeAttribute(name);
        }
    }

    @Override
    @Nullable
    public Object getAttribute(String name) {
        Assert.notNull(name,"Name must not be null");
        return this.attributes.get(name);
    }

    @Override
    @Nullable
    public Object removeAttribute(String name) {
        Assert.notNull(name,"Name must not be null");
        return this.attributes.remove(name);
    }

    @Override
    public boolean hasAttribute(String name) {
        Assert.notNull(name,"Name must not be null");
        return this.attributes.containsKey(name);
    }

    @Override
    public String[] attributeNames() {
        return StringUtils.toStringArray(this.attributes.keySet());
    }

    protected void copyAttributesFrom(AttributeAccessor source) {
        Assert.notNull(source,"Source must not be null");
        String[] attributeNames = source.attributeNames();
        for (String attributeName : attributeNames) {
            setAttribute(attributeName,getAttribute(attributeName));
        }
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof AttributeAccessorSupport &&
                this.attributes.equals(((AttributeAccessorSupport) other).attributes)));
    }

    @Override
    public int hashCode() {
        return this.attributes.hashCode();
    }

}
