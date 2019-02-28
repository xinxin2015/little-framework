package cn.admin.beans.factory.support;

import cn.admin.beans.BeanMetadataAttributeAccessor;
import cn.admin.beans.MutablePropertyValues;
import cn.admin.beans.factory.config.AutowireCapableBeanFactory;
import cn.admin.beans.factory.config.BeanDefinition;
import cn.admin.beans.factory.config.ConstructorArgumentValues;
import cn.admin.core.io.Resource;
import cn.admin.lang.Nullable;
import cn.admin.util.ClassUtils;

import java.util.LinkedHashMap;
import java.util.Map;
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

    @Nullable
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
        Class<?> resolvedClass = ClassUtils.
    }
}
