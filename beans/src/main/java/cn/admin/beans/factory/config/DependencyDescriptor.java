package cn.admin.beans.factory.config;

import cn.admin.beans.BeansException;
import cn.admin.beans.factory.BeanFactory;
import cn.admin.beans.factory.InjectionPoint;
import cn.admin.beans.factory.NoUniqueBeanDefinitionException;
import cn.admin.core.GenericTypeResolver;
import cn.admin.core.MethodParameter;
import cn.admin.core.ParameterNameDiscoverer;
import cn.admin.core.ResolvableType;
import cn.admin.core.convert.TypeDescriptor;
import cn.admin.lang.Nullable;
import cn.admin.util.ObjectUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

public class DependencyDescriptor extends InjectionPoint implements Serializable {

    private final Class<?> declaringClass;

    @Nullable
    private String methodName;

    @Nullable
    private Class<?>[] parameterTypes;

    private int parameterIndex;

    @Nullable
    private String fieldName;

    private final boolean required;

    private final boolean eager;

    private int nestingLevel = 1;

    @Nullable
    private Class<?> containingClass;

    @Nullable
    private transient volatile ResolvableType resolvableType;

    @Nullable
    private transient volatile TypeDescriptor typeDescriptor;

    public DependencyDescriptor(MethodParameter methodParameter,boolean required) {
        this(methodParameter,required,true);
    }

    public DependencyDescriptor(MethodParameter methodParameter,boolean required,boolean eager) {
        super(methodParameter);
        this.declaringClass = methodParameter.getDeclaringClass();
        if (methodParameter.getMethod() != null) {
            this.methodName = methodParameter.getMethod().getName();
        }
        this.parameterTypes = methodParameter.getExecutable().getParameterTypes();
        this.parameterIndex = methodParameter.getParameterIndex();
        this.containingClass = methodParameter.getContainingClass();
        this.required = required;
        this.eager = eager;
    }

    public DependencyDescriptor(Field field,boolean required) {
        this(field,required,true);
    }

    public DependencyDescriptor(Field field,boolean required,boolean eager) {
        super(field);
        this.declaringClass = field.getDeclaringClass();
        this.fieldName = field.getName();
        this.required = required;
        this.eager = eager;
    }

    public DependencyDescriptor(DependencyDescriptor original) {
        super(original);

        this.declaringClass = original.declaringClass;
        this.methodName = original.methodName;
        this.parameterTypes = original.parameterTypes;
        this.parameterIndex = original.parameterIndex;
        this.fieldName = original.fieldName;
        this.containingClass = original.containingClass;
        this.required = original.required;
        this.eager = original.eager;
        this.nestingLevel = original.nestingLevel;
    }

    public boolean isRequired() {
        if (!this.required) {
            return false;
        }

        if (this.field != null) {
            return !(this.field.getType() == Optional.class || hasNullableAnnotation());
        } else {
            return !obtainMethodParameter().isOptional();
        }
    }

    private boolean hasNullableAnnotation() {
        for (Annotation ann : getAnnotations()) {
            if ("Nullable".equals(ann.annotationType().getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isEager() {
        return eager;
    }

    @Nullable
    public Object resolveNotUnique(ResolvableType type, Map<String,Object> matchingBeans) throws BeansException {
        throw new NoUniqueBeanDefinitionException(type,matchingBeans.keySet());
    }

    @Deprecated
    @Nullable
    public Object resolveNotUnique(Class<?> type, Map<String, Object> matchingBeans) throws BeansException {
        throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
    }

    @Nullable
    public Object resolveShortcut(BeanFactory beanFactory) throws BeansException {
        return null;
    }

    public Object resolveCandidate(String beanName,Class<?> requiredType,BeanFactory beanFactory) throws BeansException {
        return beanFactory.getBean(beanName);
    }

    public void increaseNestingLevel() {
        this.nestingLevel ++;
        this.resolvableType = null;
        if (this.methodParameter != null) {
            this.methodParameter.increaseNestingLevel();
        }
    }

    public void setContainingClass(Class<?> containingClass) {
        this.containingClass = containingClass;
        this.resolvableType = null;
        if (this.methodParameter != null) {
            GenericTypeResolver.resolveParameterType(this.methodParameter,containingClass);
        }
    }

    public ResolvableType getResolvableType() {
        ResolvableType resolvableType = this.resolvableType;
        if (resolvableType == null) {
            resolvableType = (this.field != null ? ResolvableType.forField(this.field,
                    this.nestingLevel,this.containingClass) :
                    ResolvableType.forMethodParameter(obtainMethodParameter()));
            this.resolvableType = resolvableType;
        }
        return resolvableType;
    }

    public TypeDescriptor getTypeDescriptor() {
        TypeDescriptor typeDescriptor = this.typeDescriptor;
        if (typeDescriptor == null) {
            typeDescriptor = (this.field != null ?
                    new TypeDescriptor(getResolvableType(),getDependencyType(),getAnnotations()) :
                    new TypeDescriptor(obtainMethodParameter()));
            this.typeDescriptor = typeDescriptor;
        }
        return typeDescriptor;
    }

    public boolean fallbackMatchAllowed() {
        return false;
    }

    public DependencyDescriptor forFallbackMatch() {
        return new DependencyDescriptor(this) {
            @Override
            public boolean fallbackMatchAllowed() {
                return true;
            }
        };
    }

    public void initParameterNameDiscovery(ParameterNameDiscoverer parameterNameDiscoverer) {
        if (this.methodParameter != null) {
            this.methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
        }
    }

    @Nullable
    public String getDependencyName() {
        return this.field != null ? this.field.getName() :
                obtainMethodParameter().getParameterName();
    }

    public Class<?> getDependencyType() {
        if (this.field != null) {
            if (this.nestingLevel > 1) {
                Type type = this.field.getGenericType();
                for (int i = 2;i <= this.nestingLevel;i ++) {
                    if (type instanceof ParameterizedType) {
                        Type[] args = ((ParameterizedType) type).getActualTypeArguments();
                        type = args[args.length - 1];
                    }
                }
                if (type instanceof Class) {
                    return (Class<?>) type;
                } else if (type instanceof ParameterizedType) {
                    Type arg = ((ParameterizedType) type).getRawType();
                    if (arg instanceof Class) {
                        return (Class<?>) arg;
                    }
                }
                return Object.class;
            } else {
                return this.field.getType();
            }
        } else {
            return obtainMethodParameter().getNestedParameterType();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!super.equals(other)) {
            return false;
        }
        DependencyDescriptor otherDesc = (DependencyDescriptor) other;
        return (this.required == otherDesc.required && this.eager == otherDesc.eager &&
                this.nestingLevel == otherDesc.nestingLevel && this.containingClass == otherDesc.containingClass);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + ObjectUtils.nullSafeHashCode(this.containingClass);
    }

    private void readObject(ObjectInputStream ois) throws IOException,ClassNotFoundException {
        ois.defaultReadObject();

        // Restore reflective handles (which are unfortunately not serializable)
        try {
            if (this.fieldName != null) {
                this.field = this.declaringClass.getDeclaredField(this.fieldName);
            }
            else {
                if (this.methodName != null) {
                    this.methodParameter = new MethodParameter(
                            this.declaringClass.getDeclaredMethod(this.methodName, this.parameterTypes), this.parameterIndex);
                }
                else {
                    this.methodParameter = new MethodParameter(
                            this.declaringClass.getDeclaredConstructor(this.parameterTypes), this.parameterIndex);
                }
                for (int i = 1; i < this.nestingLevel; i++) {
                    this.methodParameter.increaseNestingLevel();
                }
            }
        }
        catch (Throwable ex) {
            throw new IllegalStateException("Could not find original class structure", ex);
        }
    }

}
