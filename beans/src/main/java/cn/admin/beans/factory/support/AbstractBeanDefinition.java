package cn.admin.beans.factory.support;

import cn.admin.beans.BeanMetadataAttributeAccessor;
import cn.admin.beans.MutablePropertyValues;
import cn.admin.beans.factory.config.AutowireCapableBeanFactory;
import cn.admin.beans.factory.config.BeanDefinition;
import cn.admin.beans.factory.config.ConstructorArgumentValues;
import cn.admin.core.io.DescriptiveResource;
import cn.admin.core.io.Resource;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ClassUtils;
import cn.admin.util.ObjectUtils;
import cn.admin.util.StringUtils;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor
        implements BeanDefinition,Cloneable {

    public static final String SCOPE_DEFAULT = "";

    public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;

    public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

    public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

    public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;

    @Deprecated
    public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;

    public static final int DEPENDENCY_CHECK_NONE = 0;

    public static final int DEPENDENCY_CHECK_OBJECTS = 1;

    public static final int DEPENDENCY_CHECK_SIMPLE = 2;

    public static final int DEPENDENCY_CHECK_ALL = 3;

    public static final String INFER_METHOD = "(inferred)";

    @Nullable
    private volatile Object beanClass;

    @Nullable
    private String scope = SCOPE_DEFAULT;

    private boolean abstractFlag = false;

    private boolean lazyInit = false;

    private int autowireMode = AUTOWIRE_NO;

    private int dependencyCheck = DEPENDENCY_CHECK_NONE;

    @Nullable
    String[] dependsOn;

    private boolean autowireCandidate = true;

    private boolean primary = true;

    private final Map<String,AutowireCandidateQualifier> qualifiers = new LinkedHashMap<>();

    @Nullable
    private Supplier<?> instanceSupplier;

    private boolean nonPublicAccessAllowed = true;

    private boolean lenientConstructorResolution = true;

    @Nullable
    private String factoryBeanName;

    @Nullable
    private String factoryMethodName;

    @Nullable
    private ConstructorArgumentValues constructorArgumentValues;

    @Nullable
    private MutablePropertyValues propertyValues;

    @Nullable
    private MethodOverrides methodOverrides;

    @Nullable
    private String initMethodName;

    @Nullable
    private String destroyMethodName;

    private boolean enforceInitMethod = true;

    private boolean enforceDestroyMethod = true;

    private boolean synthetic = false;

    private int role = BeanDefinition.ROLE_APPLICATION;

    @Nullable
    private String description;

    @Nullable
    private Resource resource;

    protected AbstractBeanDefinition() {
        this(null,null);
    }

    protected AbstractBeanDefinition(@Nullable ConstructorArgumentValues cargs,
                                     @Nullable MutablePropertyValues pvs) {
        this.constructorArgumentValues = cargs;
        this.propertyValues = pvs;
    }

    protected AbstractBeanDefinition(BeanDefinition original) {
        setParentName(original.getParentName());
        setBeanClassName(original.getBeanClassName());
        setScope(original.getScope());
        setAbstract(original.isAbstract());
        setLazyInit(original.isLazyInit());
        setFactoryBeanName(original.getFactoryBeanName());
        setFactoryMethodName(original.getFactoryMethodName());
        setRole(original.getRole());
        setSource(original.getSource());
        copyAttributesFrom(original);

        if (original instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
            if (originalAbd.hasBeanClass()) {
                setBeanClass(originalAbd.getBeanClass());
            }
            if (originalAbd.hasConstructorArgumentValues()) {
                setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
            }
            if (originalAbd.hasPropertyValues()) {
                setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
            }
            if (originalAbd.hasMethodOverrides()) {
                setMethodOverrides(new MethodOverrides(originalAbd.getMethodOverrides()));
            }
            setAutowireMode(originalAbd.getAutowireMode());
            setDependencyCheck(originalAbd.getDependencyCheck());
            setDependsOn(originalAbd.getDependsOn());
            setAutowireCandidate(originalAbd.isAutowireCandidate());
            setPrimary(originalAbd.isPrimary());
            copyQualifiersFrom(originalAbd);
            setInstanceSupplier(originalAbd.getInstanceSupplier());
            setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
            setLenientConstructorResolution(originalAbd.isLenientConstructorResolution());
            setInitMethodName(originalAbd.getInitMethodName());
            setEnforceInitMethod(originalAbd.isEnforceInitMethod());
            setDestroyMethodName(originalAbd.getDestroyMethodName());
            setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
            setSynthetic(originalAbd.isSynthetic());
            setResource(originalAbd.getResource());
        }
        else {
            setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
            setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
            setResourceDescription(original.getResourceDescription());
        }
    }

    public void overrideFrom(BeanDefinition other) {
        if (StringUtils.hasLength(other.getBeanClassName())) {
            setBeanClassName(other.getBeanClassName());
        }
        if (StringUtils.hasLength(other.getScope())) {
            setScope(other.getScope());
        }
        setAbstract(other.isAbstract());
        setLazyInit(other.isLazyInit());
        if (StringUtils.hasLength(other.getFactoryBeanName())) {
            setFactoryBeanName(other.getFactoryBeanName());
        }
        if (StringUtils.hasLength(other.getFactoryMethodName())) {
            setFactoryMethodName(other.getFactoryMethodName());
        }
        setRole(other.getRole());
        setSource(other.getSource());
        copyAttributesFrom(other);
        if (other instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition otherAbd = (AbstractBeanDefinition) other;
            if (otherAbd.hasBeanClass()) {
                setBeanClass(otherAbd.getBeanClass());
            }
            if (otherAbd.hasConstructorArgumentValues()) {
                getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
            }
            if (otherAbd.hasPropertyValues()) {
                getPropertyValues().addPropertyValues(other.getPropertyValues());
            }
            if (otherAbd.hasMethodOverrides()) {
                getMethodOverrides().addOverrides(otherAbd.getMethodOverrides());
            }
            setAutowireMode(otherAbd.getAutowireMode());
            setDependencyCheck(otherAbd.getDependencyCheck());
            setDependsOn(otherAbd.getDependsOn());
            setAutowireCandidate(otherAbd.isAutowireCandidate());
            setPrimary(otherAbd.isPrimary());
            copyQualifiersFrom(otherAbd);
            setInstanceSupplier(otherAbd.getInstanceSupplier());
            setNonPublicAccessAllowed(otherAbd.isNonPublicAccessAllowed());
            setLenientConstructorResolution(otherAbd.isLenientConstructorResolution());
            if (otherAbd.getInitMethodName() != null) {
                setInitMethodName(otherAbd.getInitMethodName());
                setEnforceInitMethod(otherAbd.isEnforceInitMethod());
            }
            if (otherAbd.getDestroyMethodName() != null) {
                setDestroyMethodName(otherAbd.getDestroyMethodName());
                setEnforceDestroyMethod(otherAbd.isEnforceDestroyMethod());
            }
            setSynthetic(otherAbd.isSynthetic());
            setResource(otherAbd.getResource());
        }
        else {
            getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
            getPropertyValues().addPropertyValues(other.getPropertyValues());
            setResourceDescription(other.getResourceDescription());
        }
    }

    @Override
    public void setBeanClassName(@Nullable String beanClassName) {
        this.beanClass = beanClassName;
    }

    @Override
    @Nullable
    public String getBeanClassName() {
        Object beanClassObject = this.beanClass;
        if (beanClassObject instanceof Class) {
            return ((Class) beanClassObject).getName();
        } else {
            return (String) beanClassObject;
        }
    }

    public void setBeanClass(@Nullable Object beanClass) {
        this.beanClass = beanClass;
    }

    public Class<?> getBeanClass() {
        Object beanClassObject = this.beanClass;
        if (beanClassObject == null) {
            throw new IllegalStateException("No bean class specified on bean definition");
        }
        if (!(beanClassObject instanceof Class)) {
            throw new IllegalStateException(
                    "Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
        }
        return (Class<?>) beanClassObject;
    }

    public boolean hasBeanClass() {
        return this.beanClass instanceof Class;
    }

    @Nullable
    public Class<?> resolveBeanClass(@Nullable ClassLoader classLoader) throws ClassNotFoundException {
        String className = getBeanClassName();
        if (className == null) {
            return null;
        }
        Class<?> resolvedClass = ClassUtils.forName(className,classLoader);
        this.beanClass = resolvedClass;
        return resolvedClass;
    }

    @Override
    public void setScope(@Nullable String scope) {
        this.scope = scope;
    }

    @Override
    @Nullable
    public String getScope() {
        return scope;
    }

    @Override
    public boolean isSingleton() {
        return SCOPE_SINGLETON.equals(this.scope) || SCOPE_DEFAULT.equals(this.scope);
    }

    @Override
    public boolean isPrototype() {
        return SCOPE_PROTOTYPE.equals(this.scope);
    }

    public void setAbstract(boolean abstractFlag) {
        this.abstractFlag = abstractFlag;
    }

    public boolean isAbstract() {
        return abstractFlag;
    }

    @Override
    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    @Override
    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setAutowireMode(int autowireMode) {
        this.autowireMode = autowireMode;
    }

    public int getAutowireMode() {
        return autowireMode;
    }

    public int getResolvedAutowireMode() {
        if (this.autowireMode == AUTOWIRE_AUTODETECT) {
            Constructor<?>[] constructors = getBeanClass().getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == 0) {
                    return AUTOWIRE_BY_TYPE;
                }
            }
            return AUTOWIRE_CONSTRUCTOR;
        } else {
            return this.autowireMode;
        }
    }

    public void setDependencyCheck(int dependencyCheck) {
        this.dependencyCheck = dependencyCheck;
    }

    public int getDependencyCheck() {
        return dependencyCheck;
    }

    @Override
    public void setDependsOn(@Nullable String... dependsOn) {
        this.dependsOn = dependsOn;
    }

    @Override
    @Nullable
    public String[] getDependsOn() {
        return dependsOn;
    }

    @Override
    public void setAutowireCandidate(boolean autowireCandidate) {
        this.autowireCandidate = autowireCandidate;
    }

    @Override
    public boolean isAutowireCandidate() {
        return autowireCandidate;
    }

    @Override
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public boolean isPrimary() {
        return primary;
    }

    public void addQualifier(AutowireCandidateQualifier qualifier) {
        this.qualifiers.put(qualifier.getTypeName(),qualifier);
    }

    public boolean hasQualifier(String typeName) {
        return this.qualifiers.keySet().contains(typeName);
    }

    public AutowireCandidateQualifier getQualifier(String typeName) {
        return this.qualifiers.get(typeName);
    }

    public Set<AutowireCandidateQualifier> getQualifiers() {
        return new LinkedHashSet<>(this.qualifiers.values());
    }

    public void copyQualifiersFrom(AbstractBeanDefinition source) {
        Assert.notNull(source, "Source must not be null");
        this.qualifiers.putAll(source.qualifiers);
    }

    public void setInstanceSupplier(@Nullable Supplier<?> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }

    @Nullable
    public Supplier<?> getInstanceSupplier() {
        return instanceSupplier;
    }

    public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
        this.nonPublicAccessAllowed = nonPublicAccessAllowed;
    }

    public boolean isNonPublicAccessAllowed() {
        return nonPublicAccessAllowed;
    }

    public void setLenientConstructorResolution(boolean lenientConstructorResolution) {
        this.lenientConstructorResolution = lenientConstructorResolution;
    }

    public boolean isLenientConstructorResolution() {
        return lenientConstructorResolution;
    }

    @Override
    public void setFactoryBeanName(@Nullable String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    @Override
    @Nullable
    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    @Override
    public void setFactoryMethodName(@Nullable String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    @Override
    @Nullable
    public String getFactoryMethodName() {
        return factoryMethodName;
    }

    public void setConstructorArgumentValues(@Nullable ConstructorArgumentValues constructorArgumentValues) {
        this.constructorArgumentValues = constructorArgumentValues;
    }

    @Override
    @Nullable
    public ConstructorArgumentValues getConstructorArgumentValues() {
        if (this.constructorArgumentValues == null) {
            this.constructorArgumentValues = new ConstructorArgumentValues();
        }
        return this.constructorArgumentValues;
    }

    @Override
    public boolean hasConstructorArgumentValues() {
        return this.constructorArgumentValues != null && !this.constructorArgumentValues.isEmpty();
    }

    public void setPropertyValues(@Nullable MutablePropertyValues propertyValues) {
        this.propertyValues = propertyValues;
    }

    @Override
    @Nullable
    public MutablePropertyValues getPropertyValues() {
        if (this.propertyValues == null) {
            this.propertyValues = new MutablePropertyValues();
        }
        return this.propertyValues;
    }

    @Override
    public boolean hasPropertyValues() {
        return this.propertyValues != null && !this.propertyValues.isEmpty();
    }

    public void setMethodOverrides(MethodOverrides methodOverrides) {
        this.methodOverrides = methodOverrides;
    }

    public MethodOverrides getMethodOverrides() {
        if (this.methodOverrides == null) {
            this.methodOverrides = new MethodOverrides();
        }
        return methodOverrides;
    }

    public boolean hasMethodOverrides() {
        return this.methodOverrides != null && !this.methodOverrides.isEmpty();
    }

    @Override
    public void setInitMethodName(@Nullable String initMethodName) {
        this.initMethodName = initMethodName;
    }

    @Override
    @Nullable
    public String getInitMethodName() {
        return initMethodName;
    }

    public void setEnforceInitMethod(boolean enforceInitMethod) {
        this.enforceInitMethod = enforceInitMethod;
    }

    public boolean isEnforceInitMethod() {
        return enforceInitMethod;
    }

    @Override
    public void setDestroyMethodName(@Nullable String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    @Override
    @Nullable
    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    public void setEnforceDestroyMethod(boolean enforceDestroyMethod) {
        this.enforceDestroyMethod = enforceDestroyMethod;
    }

    public boolean isEnforceDestroyMethod() {
        return enforceDestroyMethod;
    }

    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    @Override
    public void setRole(int role) {
        this.role = role;
    }

    @Override
    public int getRole() {
        return role;
    }

    @Override
    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Override
    @Nullable
    public String getDescription() {
        return description;
    }

    public void setResource(@Nullable Resource resource) {
        this.resource = resource;
    }

    @Nullable
    public Resource getResource() {
        return resource;
    }

    public void setResourceDescription(@Nullable String resourceDescription) {
        this.resource = (resourceDescription != null ?
                new DescriptiveResource(resourceDescription) : null);
    }

    @Override
    @Nullable
    public String getResourceDescription() {
        return this.resource != null ? this.resource.getDescription() : null;
    }

    public void setOriginatingBeanDefinition(BeanDefinition originatingBd) {
        this.resource = new BeanDefinitionResource(originatingBd);
    }

    @Override
    @Nullable
    public BeanDefinition getOriginatingBeanDefinition() {
        return this.resource instanceof BeanDefinitionResource ?
                ((BeanDefinitionResource) this.resource).getBeanDefinition() : null;
    }

    public void validate() throws BeanDefinitionValidationException {
        if (hasMethodOverrides() && getFactoryMethodName() != null) {
            throw new BeanDefinitionValidationException(
                    "Cannot combine static factory method with method overrides: " +
                            "the static factory method must create the instance");
        }

        if (hasBeanClass()) {
            prepareMethodOverrides();
        }
    }

    public void prepareMethodOverrides() throws BeanDefinitionValidationException {
        if (hasMethodOverrides()) {
            Set<MethodOverride> overrides = getMethodOverrides().getOverrides();
            synchronized (overrides) {
                for (MethodOverride mo : overrides) {
                    prepareMethodOverride(mo);
                }
            }
        }
    }

    protected void prepareMethodOverride(MethodOverride mo) throws BeanDefinitionValidationException {
        int count = ClassUtils.getMethodCountForName(getBeanClass(),mo.getMethodName());
        if (count == 0) {
            throw new BeanDefinitionValidationException(
                    "Invalid method override: no method with name '" + mo.getMethodName() +
                            "' on class [" + getBeanClassName() + "]");
        }
        else if (count == 1) {
            // Mark override as not overloaded, to avoid the overhead of arg type checking.
            mo.setOverloaded(false);
        }
    }

    @Override
    protected Object clone() {
        return cloneBeanDefinition();
    }

    public abstract AbstractBeanDefinition cloneBeanDefinition();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AbstractBeanDefinition)) {
            return false;
        }
        AbstractBeanDefinition that = (AbstractBeanDefinition) other;
        boolean rtn = ObjectUtils.nullSafeEquals(getBeanClassName(), that.getBeanClassName());
        rtn = rtn &= ObjectUtils.nullSafeEquals(this.scope, that.scope);
        rtn = rtn &= this.abstractFlag == that.abstractFlag;
        rtn = rtn &= this.lazyInit == that.lazyInit;
        rtn = rtn &= this.autowireMode == that.autowireMode;
        rtn = rtn &= this.dependencyCheck == that.dependencyCheck;
        rtn = rtn &= Arrays.equals(this.dependsOn, that.dependsOn);
        rtn = rtn &= this.autowireCandidate == that.autowireCandidate;
        rtn = rtn &= ObjectUtils.nullSafeEquals(this.qualifiers, that.qualifiers);
        rtn = rtn &= this.primary == that.primary;
        rtn = rtn &= this.nonPublicAccessAllowed == that.nonPublicAccessAllowed;
        rtn = rtn &= this.lenientConstructorResolution == that.lenientConstructorResolution;
        rtn = rtn &= ObjectUtils.nullSafeEquals(this.constructorArgumentValues, that.constructorArgumentValues);
        rtn = rtn &= ObjectUtils.nullSafeEquals(this.propertyValues, that.propertyValues);
        rtn = rtn &= ObjectUtils.nullSafeEquals(this.methodOverrides, that.methodOverrides);
        rtn = rtn &= ObjectUtils.nullSafeEquals(this.factoryBeanName, that.factoryBeanName);
        rtn = rtn &= ObjectUtils.nullSafeEquals(this.factoryMethodName, that.factoryMethodName);
        rtn = rtn &= ObjectUtils.nullSafeEquals(this.initMethodName, that.initMethodName);
        rtn = rtn &= this.enforceInitMethod == that.enforceInitMethod;
        rtn = rtn &= ObjectUtils.nullSafeEquals(this.destroyMethodName, that.destroyMethodName);
        rtn = rtn &= this.enforceDestroyMethod == that.enforceDestroyMethod;
        rtn = rtn &= this.synthetic == that.synthetic;
        rtn = rtn &= this.role == that.role;
        return rtn && super.equals(other);
    }

    @Override
    public int hashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(getBeanClassName());
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.scope);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.constructorArgumentValues);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.propertyValues);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryBeanName);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryMethodName);
        hashCode = 29 * hashCode + super.hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("class [");
        sb.append(getBeanClassName()).append("]");
        sb.append("; scope=").append(this.scope);
        sb.append("; abstract=").append(this.abstractFlag);
        sb.append("; lazyInit=").append(this.lazyInit);
        sb.append("; autowireMode=").append(this.autowireMode);
        sb.append("; dependencyCheck=").append(this.dependencyCheck);
        sb.append("; autowireCandidate=").append(this.autowireCandidate);
        sb.append("; primary=").append(this.primary);
        sb.append("; factoryBeanName=").append(this.factoryBeanName);
        sb.append("; factoryMethodName=").append(this.factoryMethodName);
        sb.append("; initMethodName=").append(this.initMethodName);
        sb.append("; destroyMethodName=").append(this.destroyMethodName);
        if (this.resource != null) {
            sb.append("; defined in ").append(this.resource.getDescription());
        }
        return sb.toString();
    }

}
