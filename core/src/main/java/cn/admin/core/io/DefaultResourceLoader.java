package cn.admin.core.io;

import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ClassUtils;
import cn.admin.util.ResourceUtils;
import cn.admin.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultResourceLoader implements ResourceLoader {

    @Nullable
    private ClassLoader classLoader;

    private final Set<ProtocolResolver> protocolResolvers = new LinkedHashSet<>(4);

    private final Map<Class<?>,Map<Resource,?>> resourceCaches = new ConcurrentHashMap<>(4);

    public DefaultResourceLoader() {
        this.classLoader = ClassUtils.getDefaultClassLoader();
    }

    public DefaultResourceLoader(@Nullable ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setClassLoader(@Nullable ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void addProtocolResolver(ProtocolResolver propertyResolver) {
        Assert.notNull(propertyResolver,"ProtocolResolver must not be null");
        this.protocolResolvers.add(propertyResolver);
    }

    public Collection<ProtocolResolver> getProtocolResolvers() {
        return protocolResolvers;
    }

    @SuppressWarnings("unchecked")
    public <T> Map<Resource,T> getResourceCache(Class<T> valueType) {
        return (Map<Resource, T>) this.resourceCaches.computeIfAbsent(valueType, key -> new ConcurrentHashMap<>());
    }

    public void clearResourceCaches() {
        this.resourceCaches.clear();
    }

    @Override
    public Resource getResource(String location) {
        Assert.notNull(location,"Location must not be null");

        for (ProtocolResolver protocolResolver : protocolResolvers) {
            Resource resource = protocolResolver.resolve(location,this);
            if (resource != null) {
                return resource;
            }
        }

        if (location.startsWith("/")) {
            return getResourceByPath(location);
        } else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()),
                    getClassLoader());
        } else {
            try {
                URL url = new URL(location);
                return ResourceUtils.isFileURL(url) ? new FileUrlResource(url) :
                        new UrlResource(url);
            } catch (MalformedURLException e) {
                return getResourceByPath(location);
            }
        }
    }

    protected Resource getResourceByPath(String path) {
        return new ClassPathContextResource(path,getClassLoader());
    }

    @Override
    @Nullable
    public ClassLoader getClassLoader() {
        return this.classLoader != null ? this.classLoader : ClassUtils.getDefaultClassLoader();
    }

    protected static class ClassPathContextResource extends ClassPathResource implements ContextResource {

        public ClassPathContextResource(String path,@Nullable ClassLoader classLoader) {
            super(path,classLoader);
        }

        @Override
        public String getPathWithContext() {
            return getPath();
        }

        @Override
        public Resource createRelative(String relativePath) throws IOException {
            String pathToUse = StringUtils.applyRelativePath(getPath(), relativePath);
            return new ClassPathContextResource(pathToUse, getClassLoader());
        }
    }
}
