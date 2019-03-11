package cn.admin.util;

import cn.admin.lang.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;

public class LinkedMultiValueMap<K,V> implements MultiValueMap<K,V>, Serializable,Cloneable {

    private final Map<K,List<V>> targetMap;

    public LinkedMultiValueMap() {
        targetMap = new LinkedHashMap<>();
    }

    public LinkedMultiValueMap(int initCapacity) {
        this.targetMap = new LinkedHashMap<>(initCapacity);
    }

    public LinkedMultiValueMap(Map<K,List<V>> otherMap) {
        this.targetMap = new LinkedHashMap<>(otherMap);
    }

    @Override
    public V getFirst(K key) {
        List<V> values = this.targetMap.get(key);
        return values != null ? values.get(0) : null;
    }

    @Override
    public void add(K key, V value) {
        List<V> values = this.targetMap.computeIfAbsent(key,k -> new LinkedList<>());
        values.add(value);
    }

    @Override
    public void addAll(K key, List<? extends V> values) {
        List<V> currentValues = this.targetMap.computeIfAbsent(key,k -> new LinkedList<>());
        currentValues.addAll(values);
    }

    @Override
    public void addAll(MultiValueMap<K, V> values) {
        for (Entry<K,List<V>> entry : values.entrySet()) {
            addAll(entry.getKey(),entry.getValue());
        }
    }

    @Override
    public void set(K key, V value) {
        List<V> values = new LinkedList<>();
        values.add(value);
        this.targetMap.put(key,values);
    }

    @Override
    public void setAll(Map<K, V> values) {
        values.forEach(this::set);
    }

    @Override
    public Map<K, V> toSingleValueMap() {
        LinkedHashMap<K, V> singleValueMap = new LinkedHashMap<>(this.targetMap.size());
        this.targetMap.forEach((key, value) -> singleValueMap.put(key, value.get(0)));
        return singleValueMap;
    }

    @Override
    public int size() {
        return this.targetMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.targetMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.targetMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.targetMap.containsValue(value);
    }

    @Override
    @Nullable
    public List<V> get(Object key) {
        return this.targetMap.get(key);
    }

    @Override
    @Nullable
    public List<V> put(K key, List<V> value) {
        return this.targetMap.put(key,value);
    }

    @Override
    @Nullable
    public List<V> remove(Object key) {
        return this.targetMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends List<V>> m) {
        this.targetMap.putAll(m);
    }

    @Override
    public void clear() {
        this.targetMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.targetMap.keySet();
    }

    @Override
    public Collection<List<V>> values() {
        return this.targetMap.values();
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet() {
        return this.targetMap.entrySet();
    }

    public LinkedMultiValueMap<K,V> deepCopy() {
        LinkedMultiValueMap<K,V> copy = new LinkedMultiValueMap<>(this.targetMap);
        this.targetMap.forEach(((k, vs) -> copy.put(k,new LinkedList<>(vs))));
        return copy;
    }

    @Override
    public LinkedMultiValueMap<K, V> clone() {
        return new LinkedMultiValueMap<>(this);
    }

    @Override
    public boolean equals(Object obj) {
        return this.targetMap.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.targetMap.hashCode();
    }

    @Override
    public String toString() {
        return this.targetMap.toString();
    }
}
