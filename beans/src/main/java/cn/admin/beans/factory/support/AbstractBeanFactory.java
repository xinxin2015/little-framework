package cn.admin.beans.factory.support;

import cn.admin.beans.BeansException;
import cn.admin.beans.PropertyEditorRegistrar;
import cn.admin.beans.TypeConverter;
import cn.admin.beans.TypeMismatchException;
import cn.admin.beans.factory.*;
import cn.admin.beans.factory.config.*;
import cn.admin.core.DecoratingClassLoader;
import cn.admin.core.NamedThreadLocal;
import cn.admin.core.convert.ConversionService;
import cn.admin.lang.Nullable;
import cn.admin.util.ClassUtils;
import cn.admin.util.ObjectUtils;
import cn.admin.util.StringUtils;
import cn.admin.util.StringValueResolver;

import java.beans.PropertyEditor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {

    @Nullable
    private BeanFactory parentBeanFactory;

    @Nullable
    private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    @Nullable
    private ClassLoader tempClassLoader;

    private boolean cacheBeanMetadata = true;

    @Nullable
    private BeanExpressionResolver beanExpressionResolver;

    @Nullable
    private ConversionService conversionService;

    private final Set<PropertyEditorRegistrar> propertyEditorRegistrars = new LinkedHashSet<>(4);

    private final Map<Class<?>,Class<? extends PropertyEditor>> customEditors = new HashMap<>(4);

    @Nullable
    private TypeConverter typeConverter;

    private final List<StringValueResolver> embeddedValueResolvers = new CopyOnWriteArrayList<>();

    private final List<BeanPostProcessor> beanPostProcessors = new CopyOnWriteArrayList<>();

    private volatile boolean hasInstantiationAwareBeanPostProcessors;

    private volatile boolean hasDestructionAwareBeanPostProcessors;

    private final Map<String, Scope> scopes = new LinkedHashMap<>();

    @Nullable
    private SecurityContextProvider securityContextProvider;

    private final Map<String,RootBeanDefinition> mergedBeanDefinitions =
            new ConcurrentHashMap<>(256);

    private final Set<String> alreadyCreated =
            Collections.newSetFromMap(new ConcurrentHashMap<>(256));

    private final ThreadLocal<Object> prototypesCurrentlyInCreation = new NamedThreadLocal<>(
            "Prototype beans currently in creation");

    public AbstractBeanFactory() {

    }

    public AbstractBeanFactory(@Nullable BeanFactory parentBeanFactory) {
        this.parentBeanFactory = parentBeanFactory;
    }

    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name,null,null,false);
    }

    @SuppressWarnings("unchecked")
    protected <T> T doGetBean(final String name,@Nullable final Class<T> requiredType,
                              @Nullable final Object[] args,boolean typeCheckOnly) throws BeansException {
        final String beanName = transformedBeanName(name);
        Object bean;
        Object sharedInstance = getSingleton(beanName);
        if (sharedInstance != null && args == null) {
            if (logger.isTraceEnabled()) {
                if (isSingletonCurrentlyInCreation(beanName)) {
                    logger.trace("Returning eagerly cached instance of singleton bean '" + beanName +
                            "' that is not fully initialized yet - a consequence of a circular reference");
                } else {
                    logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
                }
            }
            bean = getObjectForBeanInstance(sharedInstance,name,beanName,null);
        } else {
            if (isPrototypeCurrentlyInCreation(beanName)) {
                throw new BeanCurrentlyInCreationException(beanName);
            }

            BeanFactory parentBeanFactory = getParentBeanFactory();
            if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
                String nameToLookup = originalBeanName(name);
                if (parentBeanFactory instanceof AbstractBeanFactory) {
                    return ((AbstractBeanFactory) parentBeanFactory).doGetBean(nameToLookup,
                            requiredType,args,typeCheckOnly);
                } else if (args != null) {
                    return (T) parentBeanFactory.getBean(nameToLookup,args);
                } else if (requiredType != null) {
                    return parentBeanFactory.getBean(nameToLookup,requiredType);
                } else {
                    return (T) parentBeanFactory.getBean(nameToLookup);
                }
            }
            if (!typeCheckOnly) {
                markBeanAsCreated(beanName);
            }

            try {
                final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
                checkMergedBeanDefinition(mbd,beanName,args);

                String[] dependsOn = mbd.getDependsOn();
                if (dependsOn != null) {
                    for (String dep : dependsOn) {
                        if (isDependent(beanName,dep)) {
                            throw new BeanCreationException(mbd.getResourceDescription(),beanName,
                                    "Circular depends-on relationship between '" + beanName + "' " +
                                            "and '" + dep + "'");
                        }
                        registerDependentBean(beanName,dep);
                        try {
                            getBean(dep);
                        } catch (NoSuchBeanDefinitionException ex) {
                            throw new BeanCreationException(mbd.getResourceDescription(),beanName,
                                    "'" + beanName + "' depends on missing bean '" + dep + "'", ex);
                        }
                    }
                }

                if (mbd.isSingleton()) {
                    sharedInstance = getSingleton(beanName,() -> {
                       try {
                           return createBean(beanName,mbd,args);
                       } catch (BeansException ex) {
                           destroySingleton(beanName);
                           throw ex;
                       }
                    });
                    bean = getObjectForBeanInstance(sharedInstance,name,beanName,mbd);
                } else if (mbd.isPrototype()) {
                    Object prototypeInstance;
                    try {
                        beforePrototypeCreation(beanName);
                        prototypeInstance = createBean(beanName,mbd,args);
                    } finally {
                        afterPrototypeCreation(beanName);
                    }
                    bean = getObjectForBeanInstance(prototypeInstance,name,beanName,mbd);
                } else {
                    String scopeName = mbd.getScope();
                    final Scope scope = this.scopes.get(scopeName);
                    if (scope == null) {
                        throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
                    }
                    try {
                        Object scopeInstance = scope.get(beanName,() -> {
                           beforePrototypeCreation(beanName);
                           try {
                               return createBean(beanName,mbd,args);
                           } finally {
                             afterPrototypeCreation(beanName);
                           }
                        });
                        bean = getObjectForBeanInstance(scopeInstance,name,beanName,mbd);
                    } catch (IllegalStateException ex) {
                        throw new BeanCreationException(beanName,
                                "Scope '" + scopeName + "' is not active for the current thread; consider " +
                                        "defining a scoped proxy for this bean if you intend to refer to it from a singleton",
                                ex);
                    }
                }
            } catch (BeansException ex) {
                cleanupAfterBeanCreationFailure(beanName);
                throw ex;
            }

            if (requiredType != null && !requiredType.isInstance(bean)) {
                try {
                    T convertedBean = getTypeConverter().convertIfNecessary(bean,requiredType);
                    if (convertedBean == null) {
                        throw new BeanNotOfRequiredTypeException(name,requiredType,bean.getClass());
                    }
                    return convertedBean;
                } catch (TypeMismatchException ex) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Failed to convert bean '" + name + "' to required type '" +
                                ClassUtils.getQualifiedName(requiredType) + "'", ex);
                    }
                    throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
                }
            }
        }
        return (T) bean;
    }

    @Override
    public boolean containsBean(String name) {
        String beanName = transformedBeanName(name);
        if (containsSingleton(beanName) || containsBeanDefinition(beanName)) {
            return !BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(name);
        }
        BeanFactory parentBeanFactory = getParentBeanFactory();
        return parentBeanFactory != null && parentBeanFactory.containsBean(originalBeanName(name));
    }

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);

        Object beanInstance = getSingleton(beanName,false);
        if (beanInstance != null) {
            if (beanInstance instanceof BeanFactory) {
                return (BeanFactoryUtils.isFactoryDereference(name) || ((FactoryBean<?>) beanInstance).isSingleton());
            } else {
                return !BeanFactoryUtils.isFactoryDereference(name);
            }
        }

        BeanFactory parentBeanFactory = getParentBeanFactory();
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            return parentBeanFactory.isSingleton(originalBeanName(beanName));
        }

        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

        if (mbd.isSingleton()) {
            if (isFactoryBean(beanName,mbd)) {
                if (BeanFactoryUtils.isFactoryDereference(name)) {
                    return true;
                }
                FactoryBean<?> factoryBean = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
                return factoryBean.isSingleton();
            } else {
                return !BeanFactoryUtils.isFactoryDereference(name);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        String beanName = transformedBeanName(name);

        BeanFactory parentBeanFactory = getParentBeanFactory();
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            return parentBeanFactory.isPrototype(originalBeanName(name));
        }

        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        if (mbd.isPrototype()) {
            return !BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(beanName,mbd);
        }

        if (BeanFactoryUtils.isFactoryDereference(name)) {
            return false;
        }

        if (isFactoryBean(beanName,mbd)) {
            final FactoryBean<?> fb = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged((PrivilegedAction<Boolean>)() ->
                        ((fb instanceof SmartFactoryBean) && ((SmartFactoryBean<?>) fb).isPrototype())
                                || !fb.isSingleton(),getAccessControlContext());
            } else {
                return (fb instanceof SmartFactoryBean && ((SmartFactoryBean<?>) fb).isPrototype())
                        || !fb.isSingleton();
            }
        } else {
            return false;
        }
    }

    protected Object getObjectForBeanInstance(Object beanInstance, String name, String beanName,
                                              @Nullable RootBeanDefinition mbd) {
        if (BeanFactoryUtils.isFactoryDereference(name)) {
            if (beanInstance instanceof NullBean) {
                return beanInstance;
            }
            if (!(beanInstance instanceof FactoryBean)) {
                throw new BeanIsNotAFactoryException(transformedBeanName(name),
                        beanInstance.getClass());
            }
        }

        if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
            return beanInstance;
        }

        Object object = null;
        if (mbd == null) {
            object = getCachedObjectForFactoryBean(beanName);
        }
        if (object == null) {
            FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
            if (mbd == null && containsBeanDefinition(beanName)) {
                mbd = getMergedLocalBeanDefinition(beanName);
            }
            boolean synthetic = mbd != null && mbd.isSynthetic();
            object = getObjectFromFactoryBean(factory,beanName,!synthetic);
        }
        return object;
    }

    protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
        RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
        if (mbd != null) {
            return mbd;
        }
        return getMergedBeanDefinition(beanName,getBeanDefinition(beanName));
    }

    protected RootBeanDefinition getMergedBeanDefinition(String beanName,BeanDefinition bd)
            throws BeanDefinitionStoreException{
        return getMergedBeanDefinition(beanName,bd,null);
    }

    protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd,
                                                         @Nullable BeanDefinition containingBd)
            throws BeanDefinitionStoreException {
        synchronized (this.mergedBeanDefinitions) {
            RootBeanDefinition mbd = null;
            if (containingBd == null) {
                mbd = this.mergedBeanDefinitions.get(beanName);
            }

            if (mbd == null) {
                if (bd.getParentName() == null) {
                    if (bd instanceof RootBeanDefinition) {
                        mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
                    } else {
                        mbd = new RootBeanDefinition(bd);
                    }
                }
            } else {
                BeanDefinition pbd;
                try {
                    String parentBeanName = transformedBeanName(bd.getParentName());
                    if (!beanName.equals(parentBeanName)) {
                        pbd = getMergedBeanDefinition(parentBeanName);
                    } else {
                        BeanFactory parent = getParentBeanFactory();
                        if (parent instanceof ConfigurableBeanFactory) {
                            pbd = ((ConfigurableBeanFactory) parent).
                                    getMergedBeanDefinition(parentBeanName);
                        } else {
                            throw new NoSuchBeanDefinitionException(parentBeanName,
                                    "Parent name '" + parentBeanName + "' is equal to bean name '" + beanName +
                                            "': cannot be resolved without an AbstractBeanFactory parent");
                        }
                    }
                } catch (NoSuchBeanDefinitionException ex) {
                    throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
                            "Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
                }

                mbd = new RootBeanDefinition(bd);
                mbd.overrideFrom(pbd);
            }

            if (!StringUtils.hasLength(mbd.getScope())) {
                mbd.setScope(RootBeanDefinition.SCOPE_SINGLETON);
            }

            if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
                mbd.setScope(containingBd.getScope());
            }

            if (containingBd == null && isCacheBeanMetadata()) {
                this.mergedBeanDefinitions.put(beanName,mbd);
            }

            return mbd;
        }
    }

    protected void checkMergedBeanDefinition(RootBeanDefinition mbd,String beanName,
                                             @Nullable Object[] args) {
        if (mbd.isAbstract()) {
            throw new BeanIsAbstractException(beanName);
        }
    }

    protected void clearMergedBeanDefinition(String beanName) {
        this.mergedBeanDefinitions.remove(beanName);
    }

    protected void clearMetadataCache() {
        this.mergedBeanDefinitions.keySet().removeIf(bean -> !isBeanEligibleForMetadataCaching(bean));
    }

    @Nullable
    protected Class<?> resolveBeanClass(final RootBeanDefinition mbd,String beanName,
                                        final Class<?> ...typeToMatch) throws CannotLoadBeanClassException{
        try {
            if (mbd.hasBeanClass()) {
                return mbd.getBeanClass();
            }
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged((PrivilegedExceptionAction<Class<?>>) () ->
                        doResolveBeanClass(mbd,typeToMatch),getAccessControlContext());
            } else {
                return doResolveBeanClass(mbd,typeToMatch);
            }
        } catch (PrivilegedActionException pae) {
            ClassNotFoundException ex = (ClassNotFoundException) pae.getException();
            throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
        } catch (ClassNotFoundException ex) {
            throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
        } catch (LinkageError err) {
            throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), err);
        }
    }

    @Nullable
    private Class<?> doResolveBeanClass(RootBeanDefinition mbd,Class<?> ...typesToMatch)
            throws ClassNotFoundException {
        ClassLoader beanClassLoader = getBeanClassLoader();
        ClassLoader dynamicLoader = beanClassLoader;
        boolean freshResolve = false;

        if (!ObjectUtils.isEmpty(typesToMatch)) {
            ClassLoader tempClassLoader = getTempClassLoader();
            if (tempClassLoader != null) {
                dynamicLoader = tempClassLoader;
                freshResolve = true;
                if (tempClassLoader instanceof DecoratingClassLoader) {
                    DecoratingClassLoader dcl = (DecoratingClassLoader) tempClassLoader;
                    for (Class<?> typeToMatch : typesToMatch) {
                        dcl.excludeClass(typeToMatch.getName());
                    }
                }
            }
        }

        String className = mbd.getBeanClassName();
        if (className != null) {
            Object evaluated = evaluateBeanDefinitionString(className,mbd);
            if (!className.equals(evaluated)) {
                if (evaluated instanceof Class) {
                    return (Class<?>) evaluated;
                } else if (evaluated instanceof String) {
                    className = (String) evaluated;
                    freshResolve = true;
                } else {
                    throw new IllegalStateException("Invalid class name expression result: " + evaluated);
                }
            }
            if (freshResolve) {
                if (dynamicLoader != null) {
                    try {
                        return dynamicLoader.loadClass(className);
                    } catch (ClassNotFoundException ex) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Could not load class [" + className + "] from " + dynamicLoader + ": " + ex);
                        }
                    }
                }
                return ClassUtils.forName(className,dynamicLoader);
            }
        }
        return mbd.resolveBeanClass(beanClassLoader);
    }

    @Nullable
    protected Object evaluateBeanDefinitionString(@Nullable String value,
                                                  @Nullable BeanDefinition beanDefinition) {
        if (this.beanExpressionResolver == null) {
            return value;
        }

        Scope scope = null;
        if (beanDefinition != null) {
            String scopeName = beanDefinition.getScope();
            if (scopeName != null) {
                scope = getRegisteredScope(scopeName);
            }
        }
        return this.beanExpressionResolver.evaluate(value,new BeanExpressionContext(this,scope));
    }

    protected boolean isPrototypeCurrentlyInCreation(String beanName) {
        Object curVal = this.prototypesCurrentlyInCreation.get();
        return curVal != null &&
                (curVal.equals(beanName) || (curVal instanceof Set && ((Set<?>) curVal).contains(beanName)));
    }

    @SuppressWarnings("unchecked")
    protected void beforePrototypeCreation(String beanName) {
        Object curVal = this.prototypesCurrentlyInCreation.get();
        if (curVal == null) {
            this.prototypesCurrentlyInCreation.set(beanName);
        } else if (curVal instanceof String) {
            Set<String> beanNameSet = new HashSet<>(2);
            beanNameSet.add((String) curVal);
            beanNameSet.add(beanName);
            this.prototypesCurrentlyInCreation.set(beanNameSet);
        } else {
            Set<String> beanNameSet = (Set<String>) curVal;
            beanNameSet.add(beanName);
        }
    }

    @SuppressWarnings("unchecked")
    protected void afterPrototypeCreation(String beanName) {
        Object curVal = this.prototypesCurrentlyInCreation.get();
        if (curVal instanceof String) {
            this.prototypesCurrentlyInCreation.remove();
        } else if (curVal instanceof Set) {
            Set<String> beanNameSet = (Set<String>) curVal;
            beanNameSet.remove(beanName);
            if (beanNameSet.isEmpty()) {
                this.prototypesCurrentlyInCreation.remove();
            }
        }
    }

    @Override
    public void destroyBean(String beanName, Object beanInstance) {
        destroyBean(beanName,beanInstance,getMergedLocalBeanDefinition(beanName));
    }

    protected void destroyBean(String beanName,Object bean,RootBeanDefinition mbd) {
        //TODO
    }

    @Override
    public void destroyScopedBean(String beanName) {
        RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
        if (mbd.isSingleton() || mbd.isPrototype()) {
            throw new IllegalArgumentException(
                    "Bean name '" + beanName + "' does not correspond to an object in a mutable " +
                            "scope");
        }
        String scopeName = mbd.getScope();
        Scope scope = this.scopes.get(scopeName);
        if (scope == null) {
            throw new IllegalStateException("No Scope SPI registered for scope name '" + scopeName + "'");
        }

        Object bean = scope.remove(beanName);
        if (bean != null) {
            destroyBean(beanName,bean,mbd);
        }
    }

    protected String transformedBeanName(String name) {
        return canonicalName(BeanFactoryUtils.transformedBeanName(name));
    }

    protected String originalBeanName(String name) {
        String beanName = transformedBeanName(name);
        if (beanName.startsWith(FACTORY_BEAN_PREFIX)) {
            beanName = FACTORY_BEAN_PREFIX + beanName;
        }
        return beanName;
    }

    @Nullable
    protected Class<?> predictBeanType(String beanName,RootBeanDefinition mbd,
                                       Class<?> ...typesToMatch) {
        Class<?> targetType = mbd.getTargetType();
        if (targetType != null) {
            return targetType;
        }

        if (mbd.getFactoryMethodName() != null) {
            return null;
        }
        return resolveBeanClass(mbd,beanName,typesToMatch);
    }

    protected boolean isFactoryBean(String beanName,RootBeanDefinition mbd) {
        Class<?> beanType = predictBeanType(beanName,mbd,FactoryBean.class);
        return beanType != null && FactoryBean.class.isAssignableFrom(beanType);
    }

    @Nullable
    protected Class<?> getTypeForFactoryBean(String beanName,RootBeanDefinition mbd) {

    }

    protected void markBeanAsCreated(String beanName) {
        if (!this.alreadyCreated.contains(beanName)) {
            synchronized (this.mergedBeanDefinitions) {
                if (!this.alreadyCreated.contains(beanName)) {
                    clearMergedBeanDefinition(beanName);
                    this.alreadyCreated.add(beanName);
                }
            }
        }
    }

    protected void cleanupAfterBeanCreationFailure(String beanName) {
        synchronized (this.mergedBeanDefinitions) {
            this.alreadyCreated.remove(beanName);
        }
    }

    protected boolean isBeanEligibleForMetadataCaching(String beanName) {
        return this.alreadyCreated.contains(beanName);
    }

    protected boolean removeSingletonIfCreatedForTypeCheckOnly(String beanName) {
        if (!this.alreadyCreated.contains(beanName)) {
            removeSingleton(beanName);
            return true;
        } else {
            return false;
        }
    }

    protected abstract boolean containsBeanDefinition(String beanName);

    protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

    protected abstract Object createBean(String beanName,RootBeanDefinition mbd,
                                         @Nullable Object[] args) throws BeanCreationException;

}
