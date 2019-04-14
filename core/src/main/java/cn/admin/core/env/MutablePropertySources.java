package cn.admin.core.env;

import cn.admin.lang.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class MutablePropertySources implements PropertySources {

    private final List<PropertySource<?>> propertySourceList = new CopyOnWriteArrayList<>();

    public MutablePropertySources() {

    }

    public MutablePropertySources(PropertySources propertySources) {
        this();
        for (PropertySource<?> propertySource : propertySources) {
            addLast(propertySource);
        }
    }

    @Override
    public Iterator<PropertySource<?>> iterator() {
        return this.propertySourceList.iterator();
    }

    @Override
    public Spliterator<PropertySource<?>> spliterator() {
        return Spliterators.spliterator(this.propertySourceList,0);
    }

    @Override
    public Stream<PropertySource<?>> stream() {
        return this.propertySourceList.stream();
    }

    @Override
    public boolean contains(String name) {
        return this.propertySourceList.contains(PropertySource.named(name));
    }

    @Override
    @Nullable
    public PropertySource<?> get(String name) {
        int index = this.propertySourceList.indexOf(PropertySource.named(name));
        return index != -1 ? this.propertySourceList.get(index) : null;
    }

    public void addFirst(PropertySource<?> propertySource) {
        removeIfPresent(propertySource);
        this.propertySourceList.add(0,propertySource);
    }

    public void addLast(PropertySource<?> propertySource) {
        removeIfPresent(propertySource);
        this.propertySourceList.add(propertySource);
    }

    public void addBefore(String relativePropertySourceName,PropertySource<?> propertySource) {
        assertLegalRelativeAddition(relativePropertySourceName, propertySource);
        removeIfPresent(propertySource);
        int index = assertPresentAndGetIndex(relativePropertySourceName);
        addAtIndex(index, propertySource);
    }

    public void addAfter(String relativePropertySourceName, PropertySource<?> propertySource) {
        assertLegalRelativeAddition(relativePropertySourceName, propertySource);
        removeIfPresent(propertySource);
        int index = assertPresentAndGetIndex(relativePropertySourceName);
        addAtIndex(index + 1, propertySource);
    }

    public int precedenceOf(PropertySource<?> propertySource) {
        return this.propertySourceList.indexOf(propertySource);
    }

    @Nullable
    public PropertySource<?> remove(String name) {
        int index = this.propertySourceList.indexOf(PropertySource.named(name));
        return (index != -1 ? this.propertySourceList.remove(index) : null);
    }

    public void replace(String name, PropertySource<?> propertySource) {
        int index = assertPresentAndGetIndex(name);
        this.propertySourceList.set(index, propertySource);
    }

    public int size() {
        return this.propertySourceList.size();
    }

    @Override
    public String toString() {
        return this.propertySourceList.toString();
    }

    protected void assertLegalRelativeAddition(String relativePropertySourceName,
                                               PropertySource<?> propertySource) {
        String newPropertySourceName = propertySource.getName();
        if (relativePropertySourceName.equals(newPropertySourceName)) {
            throw new IllegalArgumentException(
                    "PropertySource named '" + newPropertySourceName + "' cannot be added relative to itself");
        }
    }

    protected void removeIfPresent(PropertySource<?> propertySource) {
        this.propertySourceList.remove(propertySource);
    }

    private void addAtIndex(int index,PropertySource<?> propertySource) {
        removeIfPresent(propertySource);
        this.propertySourceList.add(index,propertySource);
    }

    private int assertPresentAndGetIndex(String name) {
        int index = this.propertySourceList.indexOf(PropertySource.named(name));
        if (index == -1) {
            throw new IllegalArgumentException("PropertySource named '" + name + "' does not " +
                    "exist");
        }
        return index;
    }

}
