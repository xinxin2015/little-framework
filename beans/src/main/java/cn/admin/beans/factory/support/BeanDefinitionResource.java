package cn.admin.beans.factory.support;

import cn.admin.beans.factory.config.BeanDefinition;
import cn.admin.core.io.AbstractResource;
import cn.admin.util.Assert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

class BeanDefinitionResource extends AbstractResource {

    private final BeanDefinition beanDefinition;

    public BeanDefinitionResource(BeanDefinition beanDefinition) {
        Assert.notNull(beanDefinition,"BeanDefinition must not be null");
        this.beanDefinition = beanDefinition;
    }

    public final BeanDefinition getBeanDefinition() {
        return beanDefinition;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public String getDescription() {
        return "BeanDefinition defined in " + this.beanDefinition.getResourceDescription();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new FileNotFoundException(
                "Resource cannot be opened because it points to " + getDescription());
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof BeanDefinitionResource &&
                ((BeanDefinitionResource) other).beanDefinition.equals(this.beanDefinition)));
    }

    @Override
    public int hashCode() {
        return this.beanDefinition.hashCode();
    }

}
