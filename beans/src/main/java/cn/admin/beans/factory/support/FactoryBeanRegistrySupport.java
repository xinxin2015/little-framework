package cn.admin.beans.factory.support;

import cn.admin.beans.BeansException;
import cn.admin.beans.factory.BeanCreationException;
import cn.admin.beans.factory.BeanCurrentlyInCreationException;
import cn.admin.beans.factory.FactoryBean;
import cn.admin.beans.factory.FactoryBeanNotInitializedException;
import cn.admin.lang.Nullable;

import java.security.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {

    private final Map<String,Object> factoryBeanObjectCache = new ConcurrentHashMap<>(16);

    @Nullable
    protected Class<?> getTypeForFactoryBean(final FactoryBean<?> factoryBean) {
        try {
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged((PrivilegedAction<Class<?>>)
                        factoryBean::getObjectType, getAccessControlContext());
            } else {
                return factoryBean.getObjectType();
            }
        } catch (Throwable ex) {
            logger.info("FactoryBean threw exception from getObjectType, despite the contract saying " +
                    "that it should return null if the type of its object cannot be determined yet", ex);
            return null;
        }
    }

    protected Object getCachedObjectForFactoryBean(String beanName) {
        return this.factoryBeanObjectCache.get(beanName);
    }

    protected Object getObjectFromFactoryBean(FactoryBean<?> factory,String beanName,
                                              boolean shouldPostProcess) {
        if (factory.isSingleton() && containsSingleton(beanName)) {
            synchronized (getSingletonMutex()) {
                Object object = this.factoryBeanObjectCache.get(beanName);
                if (object == null) {
                    object = doGetObjectFromFactoryBean(factory,beanName);
                    Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
                    if (alreadyThere != null) {
                        object = alreadyThere;
                    }
                } else {
                    if (shouldPostProcess) {
                        if (isSingletonCurrentlyInCreation(beanName)) {
                            return object;
                        }
                    }
                    beforeSingletonCreation(beanName);
                    try {
                        object = postProcessObjectFromFactoryBean(object,beanName);
                    } catch (Throwable ex) {
                        throw new BeanCreationException(beanName,
                                "Post-processing of FactoryBean's singleton object failed", ex);
                    } finally {
                        afterSingletonCreation(beanName);
                    }
                }
                if (containsSingleton(beanName)) {
                    this.factoryBeanObjectCache.put(beanName,object);
                }
                return object;
            }
        } else {
            Object object = doGetObjectFromFactoryBean(factory,beanName);
            if (shouldPostProcess) {
                try {
                    object = postProcessObjectFromFactoryBean(object, beanName);
                } catch (Throwable ex) {
                    throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
                }
            }
            return object;
        }
    }

    private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory,final String beanName) throws BeanCreationException {
        Object object;
        try {
            if (System.getSecurityManager() != null) {
                AccessControlContext acc = getAccessControlContext();
                try {
                    object = AccessController.doPrivileged((PrivilegedExceptionAction<Object>)factory::getObject,acc);
                } catch (PrivilegedActionException e) {
                    throw e.getException();
                }
            } else {
                object = factory.getObject();
            }
        } catch (FactoryBeanNotInitializedException ex) {
            throw new BeanCurrentlyInCreationException(beanName, ex.toString());
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName,"FactoryBean threw exception on object " +
                    "creation", ex);
        }
        if (object == null) {
            if (isSingletonCurrentlyInCreation(beanName)) {
                throw new BeanCurrentlyInCreationException(
                        beanName, "FactoryBean which is currently in creation returned null from getObject");
            }
            object = new NullBean();
        }
        return object;
    }

    protected Object postProcessObjectFromFactoryBean(Object object,String beanName) throws BeansException {
        return object;
    }

    protected FactoryBean<?> getFactoryBean(String beanName,Object beanInstance) throws BeansException {
        if (!(beanInstance instanceof FactoryBean)) {
            throw new BeanCreationException(beanName,
                    "Bean instance of type [" + beanInstance.getClass() + "] is not a FactoryBean");
        }
        return (FactoryBean<?>) beanInstance;
    }

    @Override
    protected void removeSingleton(String beanName) {
        synchronized (getSingletonMutex()) {
            super.removeSingleton(beanName);
            this.factoryBeanObjectCache.remove(beanName);
        }
    }

    @Override
    protected void clearSingletonCache() {
        synchronized (getSingletonMutex()) {
            super.clearSingletonCache();
            this.factoryBeanObjectCache.clear();
        }
    }

    protected AccessControlContext getAccessControlContext() {
        return AccessController.getContext();
    }

}
