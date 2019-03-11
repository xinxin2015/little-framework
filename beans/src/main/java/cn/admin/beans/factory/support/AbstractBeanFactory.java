package cn.admin.beans.factory.support;

import cn.admin.beans.PropertyEditorRegistrar;
import cn.admin.beans.TypeConverter;
import cn.admin.beans.factory.BeanFactory;
import cn.admin.beans.factory.config.BeanExpressionResolver;
import cn.admin.beans.factory.config.BeanPostProcessor;
import cn.admin.beans.factory.config.ConfigurableBeanFactory;
import cn.admin.beans.factory.config.Scope;
import cn.admin.core.convert.ConversionService;
import cn.admin.lang.Nullable;
import cn.admin.util.ClassUtils;
import cn.admin.util.StringValueResolver;

import java.beans.PropertyEditor;
import java.util.*;
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
    BeanExpressionResolver beanExpressionResolver;

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

}
