package cn.admin.beans.factory.support;

import cn.admin.beans.factory.*;
import cn.admin.beans.factory.config.SingletonBeanRegistry;
import cn.admin.core.SimpleAliasRegistry;
import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {

    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);

    private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

    private final Set<String> registeredSingletons = new LinkedHashSet<>(256);

    private final Set<String> singletonCurrentlyInCreation =
            Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    private final Set<String> inCreationCheckExclusions =
            Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    @Nullable
    private Set<Exception> suppressedExceptions;

    private boolean singletonCurrentlyInDestruction = false;

    private final Map<String, Object> disposableBeans = new LinkedHashMap<>();

    private final Map<String, Set<String>> containedBeanMap = new ConcurrentHashMap<>(16);

    private final Map<String, Set<String>> dependentBeanMap = new ConcurrentHashMap<>(64);

    private final Map<String, Set<String>> dependenciesForBeanMap = new ConcurrentHashMap<>(64);

    @Override
    public void registerSingleton(String beanName, Object singletonObject) {
        Assert.notNull(beanName, "Bean name must not be null");
        Assert.notNull(singletonObject, "Singleton object must not be null");
        synchronized (this.singletonObjects) {
            Object oldObject = this.singletonObjects.get(beanName);
            if (oldObject != null) {
                throw new IllegalStateException("Could not register object [" + singletonObject +
                        "] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
            }
            addSingleton(beanName, singletonObject);
        }
    }

    protected void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.add(beanName);
        }
    }

    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(singletonFactory, "Singleton factory myst not be null");
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
                this.earlySingletonObjects.remove(beanName);
                this.registeredSingletons.add(beanName);
            }
        }
    }

    @Override
    public Object getSingleton(String beanName) {
        return getSingleton(beanName,true);
    }

    @Nullable
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            synchronized (this.singletonObjects) {
                singletonObject = this.earlySingletonObjects.get(beanName);
                if (singletonObject == null && allowEarlyReference) {
                    ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        singletonObject = singletonFactory.getObject();
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return singletonObject;
    }

    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        Assert.notNull(beanName, "Bean name must not be null");
        synchronized (this.singletonObjects) {
            Object singletonObject = this.singletonObjects.get(beanName);
            if (singletonObject == null) {
                if (this.singletonCurrentlyInDestruction) {
                    throw new BeanCreationNotAllowedException(beanName,
                            "Singleton bean creation not allowed while singletons of this factory" +
                                    " are in destruction " +
                                    "(Do not request a bean from a BeanFactory in a destroy " +
                                    "method implementation!)");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
                }
                beforeSingletonCreation(beanName);
                boolean newSingleton = false;
                boolean recordSuppressedExceptions = this.suppressedExceptions == null;
                if (recordSuppressedExceptions) {
                    this.suppressedExceptions = new LinkedHashSet<>();
                }
                try {
                    singletonObject = singletonFactory.getObject();
                    newSingleton = true;
                } catch (IllegalStateException ex) {
                    singletonObject = this.singletonObjects.get(beanName);
                    if (singletonObject == null) {
                        throw ex;
                    }
                } catch (BeanCreationException ex) {
                    if (recordSuppressedExceptions) {
                        for (Exception suppressedException : this.suppressedExceptions) {
                            ex.addRelatedCause(suppressedException);
                        }
                    }
                    throw ex;
                } finally {
                    if (recordSuppressedExceptions) {
                        this.suppressedExceptions = null;
                    }
                    afterSingletonCreation(beanName);
                }
                if (newSingleton) {
                    addSingleton(beanName, singletonObject);
                }
            }
            return singletonObject;
        }
    }

    protected void onSuppressedException(Exception ex) {
        synchronized (this.singletonObjects) {
            if (this.suppressedExceptions != null) {
                this.suppressedExceptions.add(ex);
            }
        }
    }

    protected void removeSingleton(String beanName) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.remove(beanName);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
            this.registeredSingletons.remove(beanName);
        }
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return this.singletonObjects.containsKey(beanName);
    }

    @Override
    public String[] getSingletonNames() {
        synchronized (this.singletonObjects) {
            return StringUtils.toStringArray(this.registeredSingletons);
        }
    }

    @Override
    public int getSingletonCount() {
        synchronized (this.singletonObjects) {
            return this.registeredSingletons.size();
        }
    }

    public void setCurrentlyInCreation(String beanName, boolean inCreation) {
        Assert.notNull(beanName, "Bean name must not be null");
        if (!inCreation) {
            this.inCreationCheckExclusions.add(beanName);
        } else {
            this.inCreationCheckExclusions.remove(beanName);
        }
    }

    public boolean isCurrentlyInCreation(String beanName) {
        Assert.notNull(beanName, "Bean name must not be null");
        return (!this.inCreationCheckExclusions.contains(beanName) && isActuallyInCreation(beanName));
    }

    protected boolean isActuallyInCreation(String beanName) {
        return isSingletonCurrentlyInCreation(beanName);
    }

    public boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonCurrentlyInCreation.contains(beanName);
    }

    protected void beforeSingletonCreation(String beanName) {
        if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonCurrentlyInCreation.add(beanName)) {
            throw new BeanCurrentlyInCreationException(beanName);
        }
    }

    protected void afterSingletonCreation(String beanName) {
        if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonCurrentlyInCreation.add(beanName)) {
            throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in " +
                    "creation");
        }
    }

    public void registerDisposableBean(String beanName, DisposableBean disposableBean) {
        synchronized (this.disposableBeans) {
            this.disposableBeans.put(beanName, disposableBean);
        }
    }

    public void registerContainedBean(String containedBeanName,String containingBeanName) {
        synchronized (this.containedBeanMap) {
            Set<String> containedBeans = this.containedBeanMap.computeIfAbsent(containingBeanName
                    ,k -> new LinkedHashSet<>());
            if (!containedBeans.add(containedBeanName)) {
                return;
            }
        }
        registerDependentBean(containedBeanName,containingBeanName);
    }

    public void registerDependentBean(String beanName,String dependentBeanName) {
        String canonicalName = canonicalName(beanName);

        synchronized (this.dependentBeanMap) {
            Set<String> dependentBeans = this.dependentBeanMap.computeIfAbsent(canonicalName,
                    k -> new LinkedHashSet<>(8));
            if (!dependentBeans.add(dependentBeanName)) {
                return;
            }
        }

        synchronized (this.dependenciesForBeanMap) {
            Set<String> dependenciesForBean =
                    this.dependenciesForBeanMap.computeIfAbsent(dependentBeanName, k -> new LinkedHashSet<>(8));
            dependenciesForBean.add(canonicalName);
        }
    }

    protected boolean isDependent(String beanName,String dependentBeanName) {
        synchronized (this.dependentBeanMap) {
            return isDependent(beanName,dependentBeanName,null);
        }
    }

    private boolean isDependent(String beanName,String dependentBeanName,
                                @Nullable Set<String> alreadySeen) {
        if (alreadySeen != null && alreadySeen.contains(beanName)) {
            return false;
        }
        String canonicalName = canonicalName(beanName);
        Set<String> dependentBeans = this.dependentBeanMap.get(canonicalName);
        if (dependentBeans == null) {
            return false;
        }
        if (dependentBeans.contains(dependentBeanName)) {
            return true;
        }
        for (String transitiveDependency : dependentBeans) {
            if (alreadySeen == null) {
                alreadySeen = new HashSet<>();
            }
            alreadySeen.add(beanName);
            if (isDependent(transitiveDependency,dependentBeanName,alreadySeen)) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasDependentBean(String beanName) {
        return this.dependentBeanMap.containsKey(beanName);
    }

    public String[] getDependentBeans(String beanName) {
        Set<String> dependentBeans = this.dependentBeanMap.get(beanName);
        if (dependentBeans == null) {
            return new String[0];
        }
        synchronized (this.dependentBeanMap) {
            return StringUtils.toStringArray(dependentBeans);
        }
    }

    @Override
    public Object getSingletonMutex() {
        return null;
    }

}
