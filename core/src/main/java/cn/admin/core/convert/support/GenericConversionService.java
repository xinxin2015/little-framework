package cn.admin.core.convert.support;

import cn.admin.core.DecoratingProxy;
import cn.admin.core.ResolvableType;
import cn.admin.core.convert.ConverterNotFoundException;
import cn.admin.core.convert.TypeDescriptor;
import cn.admin.core.convert.converter.*;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ClassUtils;
import cn.admin.util.ConcurrentReferenceHashMap;
import cn.admin.util.StringUtils;

import java.lang.reflect.Array;
import java.util.*;

public class GenericConversionService implements ConfigurableConversionService {

    private static final GenericConverter NO_OP_CONVERTER = new NoOpConverter("NO_OP");

    private static final GenericConverter NO_MATCH = new NoOpConverter("NO_MATCH");

    private final Converters converters = new Converters();

    private final Map<ConverterCacheKey,GenericConverter> converterCache =
            new ConcurrentReferenceHashMap<>(64);

    @Override
    public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
        Assert.notNull(targetType,"Target type must not be null");
        return canConvert(sourceType != null ? TypeDescriptor.valueOf(sourceType) : null,
                TypeDescriptor.valueOf(targetType));
    }

    @Override
    public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
        Assert.notNull(targetType,"Target type to convert to cannot be null");
        if (sourceType == null) {
            return false;
        }
        GenericConverter converter = getConverter(sourceType,targetType);
        return converter != null;
    }

    public boolean canBypassConvert(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType) {
        Assert.notNull(targetType, "Target type to convert to cannot be null");
        if (sourceType == null) {
            return true;
        }
        GenericConverter converter = getConverter(sourceType, targetType);
        return (converter == NO_OP_CONVERTER);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T convert(Object source, Class<T> targetType) {
        Assert.notNull(targetType, "Target type to convert to cannot be null");
        return (T) convert(source, TypeDescriptor.forObject(source), TypeDescriptor.valueOf(targetType));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        Assert.notNull(targetType,"Target type to convert to cannot be null");
        if (sourceType == null) {
            Assert.isTrue(source == null,"Source must be [null] if source type == [null]");
            return handleResult(null,targetType,convertNullSource(null,targetType));
        }
        if (source != null && !sourceType.getObjectType().isInstance(source)) {
            throw new IllegalArgumentException("Source to convert from must be an instance of [" +
                    sourceType + "]; instead it was a [" + source.getClass().getName() + "]");
        }
        GenericConverter converter = getConverter(sourceType,targetType);
        if (converter != null) {
            Object result = ConversionUtils.invokeConverter(converter,source,sourceType,targetType);
            return handleResult(sourceType,targetType,result);
        }
        return handleConverterNotFound(source,sourceType,targetType);
    }

    @Nullable
    public Object convert(@Nullable Object source,TypeDescriptor targetType) {
        return convert(source,TypeDescriptor.forObject(source),targetType);
    }

    @Override
    public String toString() {
        return this.converters.toString();
    }

    @Override
    public void addConverted(Converter<?, ?> converter) {
        ResolvableType[] typeInfo = getRequiredTypeInfo(converter.getClass(),Converter.class);
        if (typeInfo == null && converter instanceof DecoratingProxy) {
            typeInfo = getRequiredTypeInfo(((DecoratingProxy) converter).getDecoratedClass(),
                    Converter.class);
        }
        if (typeInfo == null) {
            throw new IllegalArgumentException("Unable to determine source type <S> and target type <T> for your " +
                    "Converter [" + converter.getClass().getName() + "]; does the class parameterize those types?");
        }
        addConverter(new ConverterAdapter(converter,typeInfo[0],typeInfo[1]));
    }

    @Override
    public <S, T> void addConverter(Class<S> sourceType, Class<T> targetType, Converter<? super S, ? extends T> converter) {
        addConverter(new ConverterAdapter(converter,ResolvableType.forClass(sourceType),
                ResolvableType.forClass(targetType)));
    }

    @Override
    public void addConverter(GenericConverter converter) {
        this.converters.add(converter);
        this.invalidateCache();
    }

    @Override
    public void addConverterFactory(ConverterFactory<?, ?> factory) {
        ResolvableType[] typeInfo = getRequiredTypeInfo(factory.getClass(),ConverterFactory.class);
        if (typeInfo == null && factory instanceof DecoratingProxy) {
            typeInfo = getRequiredTypeInfo(((DecoratingProxy) factory).getDecoratedClass(),
                    ConverterFactory.class);
        }
        if (typeInfo == null) {
            throw new IllegalArgumentException("Unable to determine source type <S> and target type <T> for your " +
                    "ConverterFactory [" + factory.getClass().getName() + "]; does the class parameterize those types?");
        }
        addConverter(new ConverterFactoryAdapter(factory,
                new GenericConverter.ConvertiblePair(typeInfo[0].toClass(), typeInfo[1].toClass())));
    }

    @Override
    public void removeConvertible(Class<?> sourceType, Class<?> targetType) {
        this.converters.remove(sourceType,targetType);
        this.invalidateCache();
    }

    @Nullable
    protected Object convertNullSource(@Nullable TypeDescriptor sourceType,
                                       TypeDescriptor targetType) {
        if (targetType.getObjectType() == Optional.class) {
            return Optional.empty();
        }
        return null;
    }

    @Nullable
    protected GenericConverter getConverter(TypeDescriptor sourceType,TypeDescriptor targetType) {
        ConverterCacheKey key = new ConverterCacheKey(sourceType,targetType);
        GenericConverter converter = this.converterCache.get(key);
        if (converter != null) {
            return converter != NO_MATCH ? converter : null;
        }

        converter = this.converters.find(sourceType,targetType);
        if (converter == null) {
            converter = getDefaultConverter(sourceType,targetType);
        }

        if (converter != null) {
            this.converterCache.put(key,converter);
            return converter;
        }

        this.converterCache.put(key,NO_MATCH);
        return null;
    }

    @Nullable
    protected GenericConverter getDefaultConverter(TypeDescriptor sourceType,
                                                   TypeDescriptor targetType) {
        return sourceType.isAssignableTo(targetType) ? NO_OP_CONVERTER : null;
    }

    @Nullable
    private ResolvableType[] getRequiredTypeInfo(Class<?> converterClass,Class<?> genericIfc) {
        ResolvableType resolvableType = ResolvableType.forClass(converterClass).as(genericIfc);
        ResolvableType[] generics = resolvableType.getGenerics();
        if (generics.length < 2) {
            return null;
        }
        Class<?> sourceType = generics[0].resolve();
        Class<?> targetType = generics[1].resolve();
        if (sourceType == null || targetType == null) {
            return null;
        }
        return generics;
    }

    private void invalidateCache() {
        this.converterCache.clear();
    }

    @Nullable
    private Object handleConverterNotFound(@Nullable Object source,
                                           @Nullable TypeDescriptor sourceType,
                                           TypeDescriptor targetType) {
        if (source == null) {
            assertNotPrimitiveTargetType(sourceType,targetType);
            return null;
        }
        if ((sourceType == null || sourceType.isAssignableTo(targetType)) && targetType.getObjectType().isInstance(source)) {
            return source;
        }
        throw new ConverterNotFoundException(sourceType, targetType);
    }

    @Nullable
    private Object handleResult(@Nullable TypeDescriptor sourceType,TypeDescriptor targetType,
                                @Nullable Object result) {
        if (result == null) {
            assertNotPrimitiveTargetType(sourceType,targetType);
        }
        return result;
    }

    private void assertNotPrimitiveTargetType(@Nullable TypeDescriptor sourceType,
                                              TypeDescriptor targetType) {
        if (targetType.isPrimitive()) {
            throw new ConversionFailedException(sourceType, targetType, null,
                    new IllegalArgumentException("A null value cannot be assigned to a primitive type"));
        }
    }

    @SuppressWarnings("unchecked")
    private final class ConverterAdapter implements ConditionalGenericConverter {

        private final Converter<Object,Object> converter;

        private final ConvertiblePair typeInfo;

        private final ResolvableType targetType;

        private ConverterAdapter(Converter<?,?> converter,ResolvableType sourceType,
                                 ResolvableType targetType) {
            this.converter = (Converter<Object, Object>) converter;
            this.typeInfo = new ConvertiblePair(sourceType.toClass(),targetType.toClass());
            this.targetType = targetType;
        }

        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            // Check raw type first...
            if (this.typeInfo.getTargetType() != targetType.getObjectType()) {
                return false;
            }
            // Full check for complex generic type match required?
            ResolvableType rt = targetType.getResolvableType();
            if (!(rt.getType() instanceof Class) && !rt.isAssignableFrom(this.targetType) &&
                    !this.targetType.hasUnresolvableGenerics()) {
                return false;
            }
            return !(this.converter instanceof ConditionalConverter) ||
                    ((ConditionalConverter) this.converter).matches(sourceType, targetType);
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(typeInfo);
        }

        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (source == null) {
                return convertNullSource(sourceType, targetType);
            }
            return this.converter.convert(source);
        }
    }

    @SuppressWarnings("unchecked")
    private final class ConverterFactoryAdapter implements ConditionalGenericConverter {

        private final ConverterFactory<Object,Object> converterFactory;

        private final ConvertiblePair typeInfo;

        private ConverterFactoryAdapter(ConverterFactory<?,?> converterFactory,
                                        ConvertiblePair typeInfo) {
            this.converterFactory = (ConverterFactory<Object, Object>) converterFactory;
            this.typeInfo = typeInfo;
        }

        @Override
        public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
            boolean matches = true;
            if (this.converterFactory instanceof ConditionalConverter) {
                matches = ((ConditionalConverter) this.converterFactory).matches(sourceType,
                        targetType);
            }
            if (matches) {
                Converter<?,?> converter = this.converterFactory.getConverter(targetType.getType());
                if (converter instanceof ConditionalConverter) {
                    matches = ((ConditionalConverter) converter).matches(sourceType,targetType);
                }
            }
            return matches;
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return Collections.singleton(this.typeInfo);
        }

        @Override
        @Nullable
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            if (source == null) {
                return convertNullSource(sourceType,targetType);
            }
            return this.converterFactory.getConverter(targetType.getType()).convert(source);
        }

        @Override
        public String toString() {
            return (this.typeInfo + " : " + this.converterFactory);
        }
    }

    private static final class ConverterCacheKey implements Comparable<ConverterCacheKey> {

        private final TypeDescriptor sourceType;

        private final TypeDescriptor targetType;

        private ConverterCacheKey(TypeDescriptor sourceType,TypeDescriptor targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ConverterCacheKey)) {
                return false;
            }
            ConverterCacheKey otherKey = (ConverterCacheKey) other;
            return (this.sourceType.equals(otherKey.sourceType)) &&
                    this.targetType.equals(otherKey.targetType);
        }

        @Override
        public int hashCode() {
            return (this.sourceType.hashCode() * 29 + this.targetType.hashCode());
        }

        @Override
        public String toString() {
            return ("ConverterCacheKey [sourceType = " + this.sourceType +
                    ", targetType = " + this.targetType + "]");
        }

        @Override
        public int compareTo(ConverterCacheKey other) {
            int result = this.sourceType.getResolvableType().toString().compareTo(
                    other.sourceType.getResolvableType().toString());
            if (result == 0) {
                result = this.targetType.getResolvableType().toString().
                        compareTo(other.targetType.getResolvableType().toString());
            }
            return result;
        }
    }

    private static class Converters {

        private final Set<GenericConverter> globalConverters = new LinkedHashSet<>();

        private final Map<GenericConverter.ConvertiblePair,ConvertersForPair> converters =
                new LinkedHashMap<>(36);

        public void add(GenericConverter converter) {
            Set<GenericConverter.ConvertiblePair> convertibleTypes =
                    converter.getConvertibleTypes();
            if (convertibleTypes == null) {
                Assert.state(converter instanceof ConditionalConverter,
                        "Only conditional converters may return null convertible types");
                this.globalConverters.add(converter);
            } else {
                for (GenericConverter.ConvertiblePair convertiblePair : convertibleTypes) {
                    ConvertersForPair convertersForPair = getMatchableConverters(convertiblePair);
                    convertersForPair.add(converter);
                }
            }
        }

        private ConvertersForPair getMatchableConverters(GenericConverter.ConvertiblePair convertiblePair) {
            ConvertersForPair convertersForPair = this.converters.get(convertiblePair);
            if (convertersForPair == null) {
                convertersForPair = new ConvertersForPair();
                this.converters.put(convertiblePair,convertersForPair);
            }
            return convertersForPair;
        }

        public void remove(Class<?> sourceType,Class<?> targetType) {
            this.converters.remove(new GenericConverter.ConvertiblePair(sourceType,targetType));
        }

        @Nullable
        public GenericConverter find(TypeDescriptor sourceType,TypeDescriptor targetType) {
            List<Class<?>> sourceCandidates = getClassHierarchy(sourceType.getType());
            List<Class<?>> targetCandidates = getClassHierarchy(targetType.getType());
            for (Class<?> sourceCandidate : sourceCandidates) {
                for (Class<?> targetCandidate : targetCandidates) {
                    GenericConverter.ConvertiblePair convertiblePair =
                            new GenericConverter.ConvertiblePair(sourceCandidate,targetCandidate);
                    GenericConverter converter = getRegisteredConverter(sourceType, targetType,
                            convertiblePair);
                    if (converter != null) {
                        return converter;
                    }
                }
            }
            return null;
        }

        @Nullable
        private GenericConverter getRegisteredConverter(TypeDescriptor sourceType,
                                                        TypeDescriptor targetType,
                                                        GenericConverter.ConvertiblePair convertiblePair) {
            ConvertersForPair convertersForPair = this.converters.get(convertiblePair);
            if (convertersForPair != null) {
                GenericConverter converter = convertersForPair.getConverter(sourceType,
                        targetType);
                if (converter != null) {
                    return converter;
                }
            }

            for (GenericConverter globalConverter : this.globalConverters) {
                if (((ConditionalConverter)globalConverter).matches(sourceType,targetType)) {
                    return globalConverter;
                }
            }
            return null;
        }

        private List<Class<?>> getClassHierarchy(Class<?> type) {
            List<Class<?>> hierarchy = new ArrayList<>(20);
            Set<Class<?>> visited = new HashSet<>(20);
            addToClassHierarchy(0, ClassUtils.resolvePrimitiveIfNecessary(type),false,hierarchy,visited);
            boolean array = type.isArray();

            int i = 0;
            while (i < hierarchy.size()) {
                Class<?> candidate = hierarchy.get(i);
                candidate = array ? candidate.getComponentType() :
                        ClassUtils.resolvePrimitiveIfNecessary(candidate);
                Class<?> superClass = candidate.getSuperclass();
                if (superClass != null && superClass != Object.class && superClass != Enum.class) {
                    addToClassHierarchy(i + 1,candidate,array,hierarchy,visited);
                }
                addInterfacesToClassHierarchy(candidate,array,hierarchy,visited);
                i ++;
            }

            if (Enum.class.isAssignableFrom(type)) {
                addToClassHierarchy(hierarchy.size(), Enum.class, array, hierarchy, visited);
                addToClassHierarchy(hierarchy.size(), Enum.class, false, hierarchy, visited);
                addInterfacesToClassHierarchy(Enum.class, array, hierarchy, visited);
            }

            addToClassHierarchy(hierarchy.size(), Object.class, array, hierarchy, visited);
            addToClassHierarchy(hierarchy.size(), Object.class, false, hierarchy, visited);
            return hierarchy;
        }

        private void addInterfacesToClassHierarchy(Class<?> type,boolean asArray,
                                                   List<Class<?>> hierarchy,Set<Class<?>> visited) {
            for (Class<?> implementedInterface : type.getInterfaces()) {
                addToClassHierarchy(hierarchy.size(),implementedInterface,asArray,hierarchy,visited);
            }
        }

        private void addToClassHierarchy(int index,Class<?> type,boolean asArray,
                                         List<Class<?>> hierarchy,Set<Class<?>> visited) {
            if (asArray) {
                type = Array.newInstance(type,0).getClass();
            }
            if (visited.add(type)) {
                hierarchy.add(index,type);
            }
        }

    }

    private static class ConvertersForPair {

        private final LinkedList<GenericConverter> converters = new LinkedList<>();

        public void add(GenericConverter converter) {
            this.converters.addFirst(converter);
        }

        GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
            for (GenericConverter converter : this.converters) {
                if (!(converter instanceof ConditionalGenericConverter) ||
                        ((ConditionalGenericConverter) converter).matches(sourceType,targetType)) {
                    return converter;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return StringUtils.collectionToCommaDelimitedString(this.converters);
        }
    }

    private static class NoOpConverter implements GenericConverter {

        private final String name;

        private NoOpConverter(String name) {
            this.name = name;
        }

        @Override
        public Set<ConvertiblePair> getConvertibleTypes() {
            return null;
        }

        @Override
        @Nullable
        public Object convert(@Nullable Object source, TypeDescriptor sourceType,
                              TypeDescriptor targetType) {
            return source;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
