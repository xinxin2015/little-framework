package cn.admin.core.io;

import cn.admin.core.env.PropertyResolver;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;

import java.beans.PropertyEditorSupport;

public class ResourceEditor extends PropertyEditorSupport {

    private final ResourceLoader resourceLoader;

    @Nullable
    private PropertyResolver propertyResolver;

    private final boolean ignoreUnresolvablePlaceholders;

    public ResourceEditor() {
        this(new DefaultResourceLoader(),null);
    }

    public ResourceEditor(ResourceLoader resourceLoader,
                          @Nullable PropertyResolver propertyResolver) {
        this(resourceLoader,propertyResolver,true);
    }

    public ResourceEditor(ResourceLoader resourceLoader,
                          @Nullable PropertyResolver propertyResolver,
                          boolean ignoreUnresolvablePlaceholders) {
        Assert.notNull(resourceLoader,"ResourceLoader must not be null");
        this.resourceLoader = resourceLoader;
        this.propertyResolver = propertyResolver;
        this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        super.setAsText(text);
    }

    protected String resolvePath(String path) {
        if (this.propertyResolver == null) {

        }
    }
}
