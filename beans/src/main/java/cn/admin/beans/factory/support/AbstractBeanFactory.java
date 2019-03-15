package cn.admin.beans.factory.support;

import cn.admin.beans.BeansException;
import cn.admin.beans.PropertyEditorRegistrar;
import cn.admin.beans.TypeConverter;
import cn.admin.beans.factory.BeanFactory;
import cn.admin.beans.factory.BeanFactoryUtils;
import cn.admin.beans.factory.BeanIsNotAFactoryException;
import cn.admin.beans.factory.FactoryBean;
import cn.admin.beans.factory.config.BeanExpressionResolver;
import cn.admin.beans.factory.config.BeanPostProcessor;
import cn.admin.beans.factory.config.ConfigurableBeanFactory;
import cn.admin.beans.factory.config.Scope;
import cn.admin.core.NamedThreadLocal;
import cn.admin.core.convert.ConversionService;
import cn.admin.lang.Nullable;
import cn.admin.util.ClassUtils;
import cn.admin.util.StringValueResolver;

import java.beans.PropertyEditor;
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
            bean =
        }
    }

    protected String transformedBeanName(String name) {
        return canonicalName(BeanFactoryUtils.transformedBeanName(name));
    }

    protected Object getObjectForBeanInstance(Object beanInstance,String name,String beanName,
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
                if (mbd == null && containsBeanDefinition(beanName)) {
                    mbd =
                }
            }
        }
    }

    protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
        RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
        if (mbd != null) {
            return mbd;
        }
        return getMergedBeanDefinition()
    }

    protected abstract boolean containsBeanDefinition(String beanName);

}
