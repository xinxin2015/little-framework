package cn.admin.beans;

import cn.admin.core.AttributeAccessorSupport;
import cn.admin.lang.Nullable;

public class BeanMetadataAttributeAccessor extends AttributeAccessorSupport implements BeanMetadataElement {

    @Nullable
    private Object source;

    public void setSource(@Nullable Object source) {
        this.source = source;
    }

    @Override
    @Nullable
    public Object getSource() {
        return this.source;
    }

    public void addMetadataAttribute(BeanMetadataAttribute attribute) {
        super.setAttribute(attribute.getName(),attribute);
    }

    @Nullable
    public BeanMetadataAttribute getMetadataAttribute(String name) {
        return (BeanMetadataAttribute) super.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        super.setAttribute(name, new BeanMetadataAttribute(name,value));
    }

    @Override
    @Nullable
    public Object getAttribute(String name) {
        BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.getAttribute(name);
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    @Nullable
    public Object removeAttribute(String name) {
        BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.removeAttribute(name);
        return attribute != null ? attribute.getValue() : null;
    }
}
