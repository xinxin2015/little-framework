package cn.admin.beans.factory.config;

import cn.admin.beans.BeanMetadataElement;
import cn.admin.beans.Mergeable;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ClassUtils;
import cn.admin.util.ObjectUtils;

import java.util.*;
import java.util.function.BiConsumer;

public class ConstructorArgumentValues {

    private final Map<Integer,ValueHolder> indexedArgumentValues = new LinkedHashMap<>();

    private final List<ValueHolder> genericArgumentValues = new ArrayList<>();

    public ConstructorArgumentValues() {

    }

    public ConstructorArgumentValues(ConstructorArgumentValues original) {
        addArgumentValues(original);
    }

    public void addArgumentValues(@Nullable ConstructorArgumentValues other) {
        if (other != null) {
            other.indexedArgumentValues.forEach(
                    (integer, valueHolder) -> addOrMergeIndexedArgumentValue(integer,valueHolder.copy()));
            other.genericArgumentValues.stream()
                    .filter(valueHolder -> !this.genericArgumentValues.contains(valueHolder))
                    .forEach(valueHolder -> addOrMergeGenericArgumentValue(valueHolder.copy()));
        }
    }

    public void addIndexedArgumentValue(int index, @Nullable Object value) {
        addIndexedArgumentValue(index, new ValueHolder(value));
    }

    public void addIndexedArgumentValue(int index, @Nullable Object value, String type) {
        addIndexedArgumentValue(index, new ValueHolder(value, type));
    }

    public void addIndexedArgumentValue(int index,ValueHolder newValue) {
        Assert.isTrue(index >= 0, "Index must not be negative");
        Assert.notNull(newValue, "ValueHolder must not be null");
        addOrMergeIndexedArgumentValue(index,newValue);
    }

    private void addOrMergeIndexedArgumentValue(Integer key,ValueHolder newValue) {
        ValueHolder currentValue = this.indexedArgumentValues.get(key);
        if (currentValue != null && newValue.getValue() instanceof Mergeable) {
            Mergeable mergeable = (Mergeable) newValue.getValue();
            if (mergeable.isMergeEnabled()) {
                newValue.setValue(mergeable.merge(currentValue));
            }
        }
        this.indexedArgumentValues.put(key,newValue);
    }

    public boolean hasIndexedArgumentValue(int index) {
        return this.indexedArgumentValues.containsKey(index);
    }

    @Nullable
    public ValueHolder getIndexedArgumentValue(int index, @Nullable Class<?> requiredType) {
        return getIndexedArgumentValue(index, requiredType, null);
    }

    @Nullable
    public ValueHolder getIndexedArgumentValue(int index,@Nullable Class<?> requiredType,
                                             @Nullable String requiredName) {
        Assert.isTrue(index >= 0,"Index must not be negative");
        ValueHolder valueHolder = this.indexedArgumentValues.get(index);
        if (valueHolder != null &&
                (valueHolder.getType() == null ||
                        (requiredType != null && ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) &&
                (valueHolder.getName() == null || "".equals(requiredName) ||
                        (requiredName != null && requiredName.equals(valueHolder.getName())))) {
            return valueHolder;
        }
        return null;
    }

    public Map<Integer, ValueHolder> getIndexedArgumentValues() {
        return Collections.unmodifiableMap(this.indexedArgumentValues);
    }

    public void addGenericArgumentValue(Object value) {
        this.genericArgumentValues.add(new ValueHolder(value));
    }

    public void addGenericArgumentValue(Object value,String type) {
        this.genericArgumentValues.add(new ValueHolder(value,type));
    }

    public void addGenericArgumentValue(ValueHolder newValue) {
        Assert.notNull(newValue,"ValueHolder must not be null");
        if (!this.genericArgumentValues.contains(newValue)) {
            addOrMergeGenericArgumentValue(newValue);
        }
    }

    private void addOrMergeGenericArgumentValue(ValueHolder newValue) {
        if (newValue.getName() != null) {
            for (Iterator<ValueHolder> it = this.genericArgumentValues.iterator();it.hasNext();) {
                ValueHolder currentValue = it.next();
                if (newValue.getName().equals(currentValue.getName())) {
                    if (newValue.getValue() instanceof Mergeable) {
                        Mergeable mergeable = (Mergeable) newValue.getValue();
                        if (mergeable.isMergeEnabled()) {
                            newValue.setValue(mergeable.merge(currentValue.getValue()));
                        }
                    }
                    it.remove();
                }
            }
        }
        this.genericArgumentValues.add(newValue);
    }

    @Nullable
    public ValueHolder getGenericArgumentValue(Class<?> requiredType) {
        return getGenericArgumentValue(requiredType, null, null);
    }

    @Nullable
    public ValueHolder getGenericArgumentValue(Class<?> requiredType, String requiredName) {
        return getGenericArgumentValue(requiredType, requiredName, null);
    }

    @Nullable
    public ValueHolder getGenericArgumentValue(@Nullable Class<?> requiredType, @Nullable String requiredName, @Nullable Set<ValueHolder> usedValueHolders) {
        for (ValueHolder valueHolder : this.genericArgumentValues) {
            if (usedValueHolders != null && usedValueHolders.contains(valueHolder)) {
                continue;
            }
            if (valueHolder.getName() != null && !"".equals(requiredName) &&
                    (requiredName == null || !valueHolder.getName().equals(requiredName))) {
                continue;
            }
            if (valueHolder.getType() != null &&
                    (requiredType == null || !ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) {
                continue;
            }
            if (requiredType != null && valueHolder.getType() == null && valueHolder.getName() == null &&
                    !ClassUtils.isAssignableValue(requiredType, valueHolder.getValue())) {
                continue;
            }
            return valueHolder;
        }
        return null;
    }

    public List<ValueHolder> getGenericArgumentValues() {
        return Collections.unmodifiableList(this.genericArgumentValues);
    }

    @Nullable
    public ValueHolder getArgumentValue(int index, Class<?> requiredType) {
        return getArgumentValue(index, requiredType, null, null);
    }

    @Nullable
    public ValueHolder getArgumentValue(int index, Class<?> requiredType, String requiredName) {
        return getArgumentValue(index, requiredType, requiredName, null);
    }

    @Nullable
    public ValueHolder getArgumentValue(int index, @Nullable Class<?> requiredType, @Nullable String requiredName, @Nullable Set<ValueHolder> usedValueHolders) {
        Assert.isTrue(index >= 0, "Index must not be negative");
        ValueHolder valueHolder = getIndexedArgumentValue(index, requiredType, requiredName);
        if (valueHolder == null) {
            valueHolder = getGenericArgumentValue(requiredType, requiredName, usedValueHolders);
        }
        return valueHolder;
    }

    public int getArgumentCount() {
        return (this.indexedArgumentValues.size() + this.genericArgumentValues.size());
    }

    public boolean isEmpty() {
        return this.indexedArgumentValues.isEmpty() && this.genericArgumentValues.isEmpty();
    }

    public void clear() {
        this.indexedArgumentValues.clear();
        this.genericArgumentValues.clear();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ConstructorArgumentValues)) {
            return false;
        }
        ConstructorArgumentValues that = (ConstructorArgumentValues) other;
        if (this.genericArgumentValues.size() != that.genericArgumentValues.size() ||
                this.indexedArgumentValues.size() != that.indexedArgumentValues.size()) {
            return false;
        }
        Iterator<ValueHolder> it1 = this.genericArgumentValues.iterator();
        Iterator<ValueHolder> it2 = that.genericArgumentValues.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            ValueHolder vh1 = it1.next();
            ValueHolder vh2 = it2.next();
            if (!vh1.contentEquals(vh2)) {
                return false;
            }
        }
        for (Map.Entry<Integer, ValueHolder> entry : this.indexedArgumentValues.entrySet()) {
            ValueHolder vh1 = entry.getValue();
            ValueHolder vh2 = that.indexedArgumentValues.get(entry.getKey());
            if (!vh1.contentEquals(vh2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 7;
        for (ValueHolder valueHolder : this.genericArgumentValues) {
            hashCode = 31 * hashCode + valueHolder.contentHashCode();
        }
        hashCode = 29 * hashCode;
        for (Map.Entry<Integer, ValueHolder> entry : this.indexedArgumentValues.entrySet()) {
            hashCode = 31 * hashCode + (entry.getValue().contentHashCode() ^ entry.getKey().hashCode());
        }
        return hashCode;
    }

    public static class ValueHolder implements BeanMetadataElement {

        @Nullable
        private Object value;

        @Nullable
        private String type;

        @Nullable
        private String name;

        @Nullable
        private Object source;

        private boolean converted = false;

        @Nullable
        private Object convertedValue;

        public ValueHolder(@Nullable Object value) {
            this.value = value;
        }

        public ValueHolder(@Nullable Object value, @Nullable String type) {
            this.value = value;
            this.type = type;
        }

        public ValueHolder(@Nullable Object value, @Nullable String type, @Nullable String name) {
            this.value = value;
            this.type = type;
            this.name = name;
        }

        public void setValue(@Nullable Object value) {
            this.value = value;
        }

        @Nullable
        public Object getValue() {
            return value;
        }

        public void setType(@Nullable String type) {
            this.type = type;
        }

        @Nullable
        public String getType() {
            return type;
        }

        public void setName(@Nullable String name) {
            this.name = name;
        }

        @Nullable
        public String getName() {
            return name;
        }

        public void setSource(@Nullable Object source) {
            this.source = source;
        }

        @Override
        @Nullable
        public Object getSource() {
            return this.source;
        }

        public synchronized boolean isConverted() {
            return this.converted;
        }

        public synchronized void setConvertedValue(@Nullable Object value) {
            this.converted = (value != null);
            this.convertedValue = value;
        }

        @Nullable
        public synchronized Object getConvertedValue() {
            return this.convertedValue;
        }

        private boolean contentEquals(ValueHolder other) {
            return (this == other ||
                    (ObjectUtils.nullSafeEquals(this.value, other.value) && ObjectUtils.nullSafeEquals(this.type, other.type)));
        }

        private int contentHashCode() {
            return ObjectUtils.nullSafeHashCode(this.value) * 29 + ObjectUtils.nullSafeHashCode(this.type);
        }

        public ValueHolder copy() {
            ValueHolder copy = new ValueHolder(this.value, this.type, this.name);
            copy.setSource(this.source);
            return copy;
        }
    }
}
