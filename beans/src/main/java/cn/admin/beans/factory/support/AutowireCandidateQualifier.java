package cn.admin.beans.factory.support;

import cn.admin.beans.BeanMetadataAttributeAccessor;
import cn.admin.util.Assert;

public class AutowireCandidateQualifier extends BeanMetadataAttributeAccessor {

    public static final String VALUE_KEY = "value";

    private final String typeName;

    public AutowireCandidateQualifier(Class<?> type) {
        this(type.getName());
    }

    public AutowireCandidateQualifier(String typeName) {
        Assert.notNull(typeName,"Type name must not be null");
        this.typeName = typeName;
    }

    public AutowireCandidateQualifier(Class<?> type,Object value) {
        this(type.getName(),value);
    }

    public AutowireCandidateQualifier(String typeName,Object value) {
        Assert.notNull(typeName,"Type name must not be null");
        this.typeName = typeName;
        setAttribute(typeName,value);
    }

    public String getTypeName() {
        return typeName;
    }
}
