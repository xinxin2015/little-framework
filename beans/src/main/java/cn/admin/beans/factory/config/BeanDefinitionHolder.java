package cn.admin.beans.factory.config;

import cn.admin.beans.BeanMetadataElement;
import cn.admin.beans.factory.BeanFactoryUtils;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ObjectUtils;
import cn.admin.util.StringUtils;

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

    public String getShortDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bean definition with name '").append(this.beanName).append("'");
        if (this.aliases != null) {
            sb.append(" and aliases [").append(StringUtils.arrayToCommaDelimitedString(this.aliases)).append("]");
        }
        return sb.toString();
    }

    public String getLongDescription() {
        return getShortDescription() + ": " + this.beanDefinition;
    }

    @Override
    public String toString() {
        return getLongDescription();
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BeanDefinitionHolder)) {
            return false;
        }
        BeanDefinitionHolder otherHolder = (BeanDefinitionHolder) other;
        return this.beanDefinition.equals(otherHolder.beanDefinition) &&
                this.beanName.equals(otherHolder.beanName) &&
                ObjectUtils.nullSafeEquals(this.aliases, otherHolder.aliases);
    }

    @Override
    public int hashCode() {
        int hashCode = this.beanDefinition.hashCode();
        hashCode = 29 * hashCode + this.beanName.hashCode();
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.aliases);
        return hashCode;
    }
}
