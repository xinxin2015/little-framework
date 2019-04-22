package cn.admin.core.convert;

import cn.admin.core.MethodParameter;
import cn.admin.core.ResolvableType;
import cn.admin.core.annotation.AnnotatedElementUtils;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ClassUtils;
import cn.admin.util.ObjectUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TypeDescriptor implements Serializable {

    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    private static final Map<Class<?>,TypeDescriptor> commonTypesCache = new HashMap<>(32);

    private static final Class<?>[] CACHED_COMMON_TYPES = {
            boolean.class,Boolean.class,byte.class,Byte.class,char.class,Character.class,
            double.class,Double.class,float.class,Float.class,int.class,Integer.class,
            long.class,Long.class,short.class,Short.class,String.class,Object.class
    };

    static {
        for (Class<?> preCachedClass : CACHED_COMMON_TYPES) {
            //TODO commonTypesCache.put(preCachedClass,)
        }
    }

    private final Class<?> type;

    private final ResolvableType resolvableType;

    private final AnnotatedElementAdapter annotatedElement;

    public TypeDescriptor(MethodParameter methodParameter) {
        this.resolvableType = ResolvableType.forMethodParameter(methodParameter);
        this.type = this.resolvableType.resolve(methodParameter.getNestedParameterType());
        this.annotatedElement = new AnnotatedElementAdapter(methodParameter.getParameterIndex() == -1 ?
                methodParameter.getMethodAnnotations() : methodParameter.getParameterAnnotations());
    }

    public TypeDescriptor(Field field) {
        this.resolvableType = ResolvableType.forField(field);
        this.type = this.resolvableType.resolve(field.getType());
        this.annotatedElement = new AnnotatedElementAdapter(field.getAnnotations());
    }

    public TypeDescriptor(Property property) {
        Assert.notNull(property,"Property must not be null");
        this.resolvableType = ResolvableType.forMethodParameter(property.getMethodParameter());
        this.type = this.resolvableType.resolve(property.getType());
        this.annotatedElement = new AnnotatedElementAdapter(property.getAnnotations());
    }

    public TypeDescriptor(ResolvableType resolvableType,@Nullable Class<?> type,@Nullable Annotation[] annotations) {
        this.resolvableType = resolvableType;
        this.type = (type != null ? type : resolvableType.toClass());
        this.annotatedElement = new AnnotatedElementAdapter(annotations);
    }

    public Class<?> getObjectType() {
        return ClassUtils.resolvePrimitiveIfNecessary(getType());
    }

    public Class<?> getType() {
        return type;
    }

    public ResolvableType getResolvableType() {
        return resolvableType;
    }

    public Object getSource() {
        return this.resolvableType.getSource();
    }

    public TypeDescriptor narrow(@Nullable Object value) {
        if (value == null) {
            return this;
        }
        ResolvableType narrowed = ResolvableType.forType(value.getClass(),getResolvableType());
        return new TypeDescriptor(narrowed,value.getClass(),getAnnotations());
    }

    @Nullable
    public TypeDescriptor upcast(@Nullable Class<?> superType) {
        if (superType == null) {
            return null;
        }
        Assert.isAssignable(superType,getType());
        return new TypeDescriptor(getResolvableType().as(superType),superType,getAnnotations());
    }

    public String getName() {
        return ClassUtils.getQualifiedName(getType());
    }

    public boolean isPrimitive() {
        return getType().isPrimitive();
    }

    public Annotation[] getAnnotations() {
        return this.annotatedElement.getAnnotations();
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        if (this.annotatedElement.isEmpty()) {
            return false;
        }
        return AnnotatedElementUtils.isAnnotated(annotatedElement,annotationType);
    }

    @Nullable
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        if (this.annotatedElement.isEmpty()) {
            // Shortcut: AnnotatedElementUtils would have to expect AnnotatedElement.getAnnotations()
            // to return a copy of the array, whereas we can do it more efficiently here.
            return null;
        }
        return AnnotatedElementUtils.getMergedAnnotation(this.annotatedElement, annotationType);
    }

    public boolean isAssignableTo(TypeDescriptor typeDescriptor) {
        boolean typeAssignable = typeDescriptor.getObjectType().isAssignableFrom(getObjectType());
        if (!typeAssignable) {
            return false;
        }
        return !isArray() || !typeDescriptor.isArray();
    }

    private boolean isNestedAssignable(@Nullable TypeDescriptor nestedTypeDescriptor,
                                       @Nullable TypeDescriptor otherNestedTypeDescriptor) {

        return (nestedTypeDescriptor == null || otherNestedTypeDescriptor == null ||
                nestedTypeDescriptor.isAssignableTo(otherNestedTypeDescriptor));
    }

    public boolean isCollection() {
        return Collection.class.isAssignableFrom(getType());
    }

    public boolean isArray() {
        return getType().isArray();
    }

    @Nullable
    public static TypeDescriptor forObject(@Nullable Object source) {
        return source != null ? valueOf(source.getClass()) : null;
    }

    public static TypeDescriptor valueOf(@Nullable Class<?> type) {
        if (type == null) {
            type = Object.class;
        }
        TypeDescriptor desc = commonTypesCache.get(type);
        return desc != null ? desc : new TypeDescriptor(ResolvableType.forClass(type),null,null);
    }

    private class AnnotatedElementAdapter implements AnnotatedElement,Serializable {

        @Nullable
        private final Annotation[] annotations;

        AnnotatedElementAdapter(@Nullable Annotation[] annotations) {
            this.annotations = annotations;
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            for (Annotation annotation : getAnnotations()) {
                if (annotation.annotationType() == annotationClass) {
                    return true;
                }
            }
            return false;
        }

        @Override
        @Nullable
        @SuppressWarnings("unchecked")
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            for (Annotation annotation : getAnnotations()) {
                if (annotation.annotationType() == annotationClass) {
                    return (T) annotation;
                }
            }
            return null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return this.annotations != null ? this.annotations : EMPTY_ANNOTATION_ARRAY;
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return getAnnotations();
        }

        public boolean isEmpty() {
            return ObjectUtils.isEmpty(this.annotations);
        }

        @Override
        public boolean equals(Object other) {
            return (this == other || (other instanceof AnnotatedElementAdapter &&
                    Arrays.equals(this.annotations, ((AnnotatedElementAdapter) other).annotations)));
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.annotations);
        }

        @Override
        public String toString() {
            return TypeDescriptor.this.toString();
        }

    }

}
