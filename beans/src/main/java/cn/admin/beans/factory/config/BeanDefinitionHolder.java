package cn.admin.beans.factory.config;

import cn.admin.beans.BeanMetadataElement;
import cn.admin.beans.factory.BeanFactoryUtils;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ObjectUtils;

public class BeanDefinitionHolder implements BeanMetadataElement {

    private final BeanDefinition beanDefinition;

    private final String beanName;

    @Nullable
    private final String[] aliases;

    public BeanDefinitionHolder(BeanDefinition beanDefinition,String beanName) {
        this(beanDefinition,beanName,null);
    }

    public BeanDefinitionHolder(BeanDefinition beanDefinition,String beanName,String[] aliases) {
        Assert.notNull(beanDefinition,"BeanDefinition must not be null");
        Assert.notNull(beanName,"Bean name must not be null");
        this.beanDefinition = beanDefinition;
        this.beanName = beanName;
        this.aliases = aliases;
    }

    public BeanDefinition getBeanDefinition() {
        return beanDefinition;
    }

    public String getBeanName() {
        return beanName;
    }

    @Nullable
    public String[] getAliases() {
        return aliases;
    }

    @Override
    @Nullable
    public Object getSource() {
        return this.beanDefinition.getSource();
    }

    public boolean matchesName(@Nullable String candidateName) {
        return (candidateName != null && (candidateName.equals(this.beanName) ||
                candidateName.equals(BeanFactoryUtils.transformedBeanName(this.beanName)) ||
                ObjectUtils.containsElement(this.aliases, candidateName)));
    }
}
