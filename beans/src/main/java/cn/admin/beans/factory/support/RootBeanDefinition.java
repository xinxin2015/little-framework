package cn.admin.beans.factory.support;

import cn.admin.beans.MutablePropertyValues;
import cn.admin.beans.factory.config.BeanDefinition;
import cn.admin.beans.factory.config.BeanDefinitionHolder;
import cn.admin.beans.factory.config.ConstructorArgumentValues;
import cn.admin.core.ResolvableType;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class RootBeanDefinition extends AbstractBeanDefinition {

    @Nullable
    private BeanDefinitionHolder decoratedDefinition;

    @Nullable
    private AnnotatedElement qualifiedElement;

    boolean allowCaching = true;

    boolean isFactoryMethodUnique = true;

    @Nullable
    volatile ResolvableType targetType;

    @Nullable
    volatile Class<?> resolvedTargetType;

    @Nullable
    volatile ResolvableType factoryMethodReturnType;

    @Nullable
    volatile Method factoryMethodToIntrospect;

    final Object constructorArgumentLock = new Object();

    @Nullable
    Executable resolvedConstructorOrFactoryMethod;

    boolean constructorArgumentsResolved = false;

    @Nullable
    Object[] resolvedConstructorArguments;

    @Nullable
    Object[] preparedConstructorArguments;

    final Object postProcessingLock = new Object();

    boolean postProcessed = false;

    @Nullable
    volatile Boolean beforeInstantiationResolved;

    @Nullable
    private Set<Member> externallyManagedConfigMembers;

    @Nullable
    private Set<String> externallyManagedInitMethods;

    @Nullable
    private Set<String> externallyManagedDestroyMethods;

    public RootBeanDefinition() {
        super();
    }

    public RootBeanDefinition(@Nullable Class<?> beanClass) {
        super();
        setBeanClass(beanClass);
    }

    public <T> RootBeanDefinition(@Nullable Class<T> beanClass, @Nullable Supplier<T> supplier) {
        super();
        setBeanClass(beanClass);
        setInstanceSupplier(supplier);
    }

    public <T> RootBeanDefinition(@Nullable Class<T> beanClass,String scope,
                                  @Nullable Supplier<T> supplier) {
        super();
        setBeanClass(beanClass);
        setScope(scope);
        setInstanceSupplier(supplier);
    }

    public RootBeanDefinition(@Nullable Class<?> beanClass,int autowireMode,
                              boolean dependencyCheck) {
        super();
        setBeanClass(beanClass);
        setAutowireMode(autowireMode);
        if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
            setDependencyCheck(DEPENDENCY_CHECK_OBJECTS);
        }
    }

    public RootBeanDefinition(@Nullable Class<?> beanClass,
                              @Nullable ConstructorArgumentValues cargs,
                              @Nullable MutablePropertyValues pvs) {
        super(cargs,pvs);
        setBeanClass(beanClass);
    }

    public RootBeanDefinition(String beanClassName) {
        setBeanClassName(beanClassName);
    }

    public RootBeanDefinition(RootBeanDefinition original) {
        super(original);
        this.decoratedDefinition = original.decoratedDefinition;
        this.qualifiedElement = original.qualifiedElement;
        this.allowCaching = original.allowCaching;
        this.isFactoryMethodUnique = original.isFactoryMethodUnique;
        this.targetType = original.targetType;
    }

    RootBeanDefinition(BeanDefinition original) {
        super(original);
    }

    @Override
    public RootBeanDefinition cloneBeanDefinition() {
        return new RootBeanDefinition(this);
    }

    @Override
    public void setParentName(String parentName) {
        if (parentName != null) {
            throw new IllegalArgumentException("Root bean cannot be changed into a child bean with parent reference");
        }
    }

    @Override
    public String getParentName() {
        return null;
    }

    public void setDecoratedDefinition(@Nullable BeanDefinitionHolder decoratedDefinition) {
        this.decoratedDefinition = decoratedDefinition;
    }

    @Nullable
    public BeanDefinitionHolder getDecoratedDefinition() {
        return decoratedDefinition;
    }

    public void setQualifiedElement(@Nullable AnnotatedElement qualifiedElement) {
        this.qualifiedElement = qualifiedElement;
    }

    @Nullable
    public AnnotatedElement getQualifiedElement() {
        return qualifiedElement;
    }

    public void setTargetType(@Nullable ResolvableType targetType) {
        this.targetType = targetType;
    }

    public void setTargetType(@Nullable Class<?> targetType) {
        this.targetType = targetType != null ? ResolvableType.forClass(targetType) : null;
    }

    @Nullable
    public Class<?> getTargetType() {
        if (this.resolvedTargetType != null) {
            return this.resolvedTargetType;
        }
        ResolvableType targetType = this.targetType;
        return targetType != null ? targetType.resolve() : null;
    }

    public ResolvableType getResolvedType() {
        ResolvableType targetType = this.targetType;
        return targetType != null ? targetType : ResolvableType.forClass(getBeanClass());
    }

    @Nullable
    public Constructor<?>[] getPreferredConstructors() {
        return null;
    }

    public void setUniqueFactoryMethodName(String name) {
        Assert.hasText(name, "Factory method name must not be empty");
        setFactoryMethodName(name);
        this.isFactoryMethodUnique = true;
    }

    public boolean isfactoryMethod(Method candidate) {
        return candidate.getName().equals(getFactoryMethodName());
    }

    @Nullable
    public Method getResolvedFactoryMethod() {
        return this.factoryMethodToIntrospect;
    }

    public void registerExternallyManagedConfigMember(Member configMember) {
        synchronized (this.postProcessingLock) {
            if (this.externallyManagedConfigMembers == null) {
                this.externallyManagedConfigMembers = new HashSet<>(1);
            }
            this.externallyManagedConfigMembers.add(configMember);
        }
    }

    public boolean isExternallyManagedConfigMember(Member configMember) {
        synchronized (this.postProcessingLock) {
            return (this.externallyManagedConfigMembers != null &&
                    this.externallyManagedConfigMembers.contains(configMember));
        }
    }

    public void registerExternallyManagedInitMethod(String initMethod) {
        synchronized (this.postProcessingLock) {
            if (this.externallyManagedInitMethods == null) {
                this.externallyManagedInitMethods = new HashSet<>(1);
            }
            this.externallyManagedInitMethods.add(initMethod);
        }
    }

    public boolean isExternallyManagedInitMethod(String initMethod) {
        synchronized (this.postProcessingLock) {
            return (this.externallyManagedInitMethods != null &&
                    this.externallyManagedInitMethods.contains(initMethod));
        }
    }

    public void registerExternallyManagedDestroyMethod(String destroyMethod) {
        synchronized (this.postProcessingLock) {
            if (this.externallyManagedDestroyMethods == null) {
                this.externallyManagedDestroyMethods = new HashSet<>(1);
            }
            this.externallyManagedDestroyMethods.add(destroyMethod);
        }
    }

    public boolean isExternallyManagedDestroyMethod(String destroyMethod) {
        synchronized (this.postProcessingLock) {
            return (this.externallyManagedDestroyMethods != null &&
                    this.externallyManagedDestroyMethods.contains(destroyMethod));
        }
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof RootBeanDefinition && super.equals(other)));
    }

    @Override
    public String toString() {
        return "Root bean: " + super.toString();
    }

}
