package cn.admin.util;

import cn.admin.lang.Nullable;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConcurrentReferenceHashMap<K,V> extends AbstractMap<K,V> implements ConcurrentMap<K, V> {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    private static final ReferenceType DEFAULT_REFERENCE_TYPE = ReferenceType.SOFT;

    private static final int MAXIMUM_CONCURRENCY_LEVEL = 1 << 16;

    private static final int MAXIMUM_SEGMENT_SIZE = 1 << 30;

    private final Segment[] segments;

    private final float loadFactor;

    private final ReferenceType referenceType;

    private final int shift;

    private volatile Set<Map<K,V>> entrySet;

    @SuppressWarnings("unchecked")
    public ConcurrentReferenceHashMap(int initialCapacity,float loadFactor,int concurrencyLevel,
                                      ReferenceType referenceType) {
        Assert.isTrue(initialCapacity >= 0, "Initial capacity must not be negative");
        Assert.isTrue(loadFactor > 0f, "Load factor must be positive");
        Assert.isTrue(concurrencyLevel > 0, "Concurrency level must be positive");
        Assert.notNull(referenceType, "Reference type must not be null");
        this.loadFactor = loadFactor;
        this.shift = calculateShift(concurrencyLevel,MAXIMUM_CONCURRENCY_LEVEL);
        int size = 1 << this.shift;
        this.referenceType = referenceType;
        int roundedUpSegmentCapacity = (int)((initialCapacity + size - 1L) / size);
        int initialSize = 1 << calculateShift(roundedUpSegmentCapacity, MAXIMUM_SEGMENT_SIZE);
        Segment[] segments = (Segment[]) Array.newInstance(Segment.class, size);
        int resizeThreshold = (int) (initialSize * getLoadFactor());
        for (int i = 0; i < segments.length; i++) {
            segments[i] = new Segment(initialSize, resizeThreshold);
        }
        this.segments = segments;
    }

    protected final float getLoadFactor() {
        return this.loadFactor;
    }

    protected final int getSegmentsSize() {
        return this.segments.length;
    }

    protected final Segment getSegment(int index) {
        return this.segments[index];
    }

    protected ReferenceManger createReferenceManager() {
        return new ReferenceManger();
    }

    protected int getHash(@Nullable Object o) {
        int hash = (o != null ? o.hashCode() : 0);
        hash += (hash << 15) ^ 0xffffcd7d;
        hash ^= (hash >>> 10);
        hash += (hash << 3);
        hash ^= (hash >>> 6);
        hash += (hash << 2) + (hash << 14);
        hash ^= (hash >>> 16);
        return hash;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return null;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {

    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return false;
    }

    @Override
    public V replace(K key,final V value) {
        return null;
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {

    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return null;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return null;
    }

    @Override
    public V putIfAbsent(K key,V value) {
        return null;
    }

    public enum ReferenceType {
        SOFT,
        WEAK
    }

    protected static int calculateShift(int minimumValue,int maximumValue) {
        int shift = 0;
        int value = 1;
        while (value < minimumValue && value < maximumValue) {
            value <<= 1;
            shift ++;
        }
        return shift;
    }

    protected final class Segment extends ReentrantLock {

        private final ReferenceManger referenceManger;

        private final int initialSize;

        private volatile Reference<K,V>[] references;

        private volatile int count = 0;

        private int resizeThreshold;

        public Segment(int initialSize,int resizeThreshold) {
            this.referenceManger = createReferenceManager();
            this.initialSize = initialSize;
            this.references = createReferenceArray(initialSize);
            this.resizeThreshold = resizeThreshold;
        }

        @Nullable
        public Reference<K,V> getReference(@Nullable Object key,int hash,Restructure restructure) {
            if (restructure == Restructure.WHEN_NECESSARY) {
                //TODO
            }
            if (this.count == 0) {
                return null;
            }
            Reference<K,V>[] references = this.references;

            return null;//TODO
        }

        protected final void restructureIfNecessary(boolean allowResize) {
            boolean needsResize = (this.count > 0 && this.count >= this.resizeThreshold);

        }

        @SuppressWarnings({"rawtypes","unchecked"})
        private Reference<K,V>[] createReferenceArray(int size) {
            return new Reference[size];
        }

        private int getIndex(int hash,Reference<K,V>[] references) {
            return (hash & (references.length - 1));
        }

        public final int getSize() {
            return this.references.length;
        }

        public final int getCount() {
            return this.count;
        }

    }

    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    private class EntryIterator implements Iterator<Map.Entry<K,V>> {

        private int segmentIndex;

        private int referenceIndex;

        @Nullable
        private Reference<K,V>[] references;

        @Nullable
        private Reference<K,V> reference;

        @Nullable
        private Entry<K,V> next;

        @Nullable
        private Entry<K,V> last;

        public EntryIterator() {
            moveToNextSegment();
        }

        @Override
        public boolean hasNext() {
            getNextIfNecessary();
            return this.next != null;
        }

        @Override
        public Map.Entry<K, V> next() {
            getNextIfNecessary();
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            this.last = this.next;
            this.next = null;
            return this.last;
        }

        @Override
        public void remove() {
            Assert.state(this.last != null, "No element to remove");
            ConcurrentReferenceHashMap.this.remove(this.last.key);
        }

        private void getNextIfNecessary() {
            while (this.next == null) {
                moveToNextReference();
                if (this.reference == null) {
                    return;
                }
                this.next = this.reference.get();
            }
        }

        private void moveToNextReference() {
            if (this.reference != null) {
                this.reference = this.reference.getNext();
            }
            while (this.reference == null && this.references != null) {
                if (this.referenceIndex >= this.references.length) {
                    moveToNextSegment();
                    this.referenceIndex = 0;
                } else {
                    this.reference = this.references[this.referenceIndex];
                    this.referenceIndex ++;
                }
            }
        }

        private void moveToNextSegment() {
            this.reference = null;
            this.references = null;
            if (this.segmentIndex < ConcurrentReferenceHashMap.this.segments.length) {
                this.references =
                        ConcurrentReferenceHashMap.this.segments[this.segmentIndex].references;
                this.segmentIndex ++;
            }
        }
    }

    protected enum Restructure {
        WHEN_NECESSARY,NEVER
    }

    protected class ReferenceManger {

        private final ReferenceQueue<Entry<K,V>> queue = new ReferenceQueue<>();

        public Reference<K,V> createReference(Entry<K,V> entry, int hash, @Nullable Reference<K, V> next) {

            if (ConcurrentReferenceHashMap.this.referenceType == ReferenceType.WEAK) {
                return new WeakEntryReference<>(entry,hash,next,queue);
            }
            return new SoftEntryReference<>(entry,hash,next,queue);
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public Reference<K,V> pollForPurge() {
            return (Reference<K, V>) this.queue.poll();
        }

    }

    protected interface Reference<K,V> {

        @Nullable
        Entry<K,V> get();

        int getHash();

        @Nullable
        Reference<K,V> getNext();

        void release();

    }

    protected static final class Entry<K,V> implements Map.Entry<K,V> {

        @Nullable
        private final K key;

        @Nullable
        private volatile V value;

        public Entry(@Nullable K key,@Nullable V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        @Nullable
        public K getKey() {
            return this.key;
        }

        @Override
        @Nullable
        public V getValue() {
            return this.value;
        }

        @Override
        @Nullable
        public V setValue(@Nullable V value) {
            V previous = this.value;
            this.value = value;
            return previous;
        }

        @Override
        public String toString() {
            return (this.key + "=" + this.value);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public final boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Map.Entry)) {
                return false;
            }
            Map.Entry otherEntry = (Map.Entry) other;
            return (ObjectUtils.nullSafeEquals(getKey(), otherEntry.getKey()) &&
                    ObjectUtils.nullSafeEquals(getValue(), otherEntry.getValue()));
        }

        @Override
        public final int hashCode() {
            return (ObjectUtils.nullSafeHashCode(this.key) ^ ObjectUtils.nullSafeHashCode(this.value));
        }
    }

    private static final class SoftEntryReference<K,V> extends SoftReference<Entry<K,V>> implements Reference<K,V> {

        private final int hash;

        @Nullable
        private final Reference<K,V> nextReference;

        public SoftEntryReference(Entry<K,V> entry,int hash,@Nullable Reference<K,V> next,
                             ReferenceQueue<Entry<K,V>> queue) {
            super(entry,queue);
            this.hash = hash;
            this.nextReference = next;
        }

        @Override
        public int getHash() {
            return this.hash;
        }

        @Override
        public Reference<K, V> getNext() {
            return this.nextReference;
        }

        @Override
        public void release() {
            enqueue();
            clear();
        }
    }

    private static final class WeakEntryReference<K,V> extends WeakReference<Entry<K,V>> implements Reference<K,V> {

        private final int hash;

        @Nullable
        private final Reference<K,V> nextReference;

        public WeakEntryReference(Entry<K,V> entry,int hash,@Nullable Reference<K,V> next,
                                  ReferenceQueue<Entry<K,V>> queue) {
            super(entry,queue);
            this.hash = hash;
            this.nextReference = next;
        }

        @Override
        public int getHash() {
            return this.hash;
        }

        @Override
        @Nullable
        public Reference<K, V> getNext() {
            return this.nextReference;
        }

        @Override
        public void release() {
            enqueue();
            clear();
        }
    }

}
