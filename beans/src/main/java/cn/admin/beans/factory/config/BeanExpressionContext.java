package cn.admin.beans.factory.config;

import cn.admin.lang.Nullable;
import cn.admin.util.Assert;

public class BeanExpressionContext {

    private final ConfigurableBeanFactory beanFactory;

    @Nullable
    private final Scope scope;

    public BeanExpressionContext(ConfigurableBeanFactory beanFactory,@Nullable Scope scope) {
        Assert.notNull(beanFactory,"BeanFactory must not be null");
        this.beanFactory = beanFactory;
        this.scope = scope;
    }

    public final ConfigurableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Nullable
    public final Scope getScope() {
        return scope;
    }

    public boolean containsObject(String key) {
        return this.beanFactory.containsBean(key) ||
                (this.scope != null && this.scope.resolveContextualObject(key) != null);
    }

    @Nullable
    public Object getObject(String key) {
        if (this.beanFactory.containsBean(key)) {
            return this.beanFactory.getBean(key);
        } else if (this.scope != null) {
            return this.scope.resolveContextualObject(key);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BeanExpressionContext)) {
            return false;
        }
        BeanExpressionContext otherContext = (BeanExpressionContext) other;
        return (this.beanFactory == otherContext.beanFactory && this.scope == otherContext.scope);
    }

    @Override
    public int hashCode() {
        return this.beanFactory.hashCode();
    }

}
