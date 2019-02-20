package cn.admin.beans.factory.config;

import cn.admin.beans.PropertyEditorRegistrar;
import cn.admin.beans.PropertyEditorRegistry;
import cn.admin.beans.TypeConverter;
import cn.admin.beans.factory.BeanDefinitionStoreException;
import cn.admin.beans.factory.BeanFactory;
import cn.admin.beans.factory.HierarchicalBeanFactory;
import cn.admin.core.convert.ConversionService;
import cn.admin.lang.Nullable;
import cn.admin.util.StringValueResolver;

import java.beans.PropertyEditor;
import java.security.AccessControlContext;

public interface ConfigurableBeanFactory extends HierarchicalBeanFactory,SingletonBeanRegistry {

    String SCOPE_SINGLETON = "singleton";

    String SCOPE_PROTOTYPE = "prototype";

    void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;

    void setBeanClassLoader(@Nullable ClassLoader beanClassLoader);

    @Nullable
    ClassLoader getBeanClassLoader();

    void setTempClassLoader(@Nullable ClassLoader tempClassLoader);

    @Nullable
    void getTempClassLoader();

    void setCacheBeanMetadata(boolean cacheBeanMetadata);

    boolean isCacheBeanMetadata();

    void setBeanExpressionResolver(@Nullable BeanExpressionResolver resolver);

    @Nullable
    BeanExpressionResolver getBeanExpressionResolver();

    void setConversionService(@Nullable ConversionService conversionService);

    @Nullable
    ConversionService getConversionService();

    void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);

    void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass);

    void copyRegisteredEditorsTo(PropertyEditorRegistry registry);

    void setTypeConvert(TypeConverter typeConverter);

    TypeConverter getTypeConverter();

    void addEmbeddedValueResolver(StringValueResolver valueResolver);

    boolean hasEmbeddedValueResolver();

    @Nullable
    String resolveEmbeddedValue(String value);

    void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);

    int getBeanPostProcessorCount();

    void registerScope(String scopeName,Scope scope);

    String[] getRegisteredScopeNames();

    @Nullable
    Scope getRegisteredScope(String scopeName);

    AccessControlContext getAccessControllerContext();

    void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);

    void registerAlias(String beanName,String alias) throws BeanDefinitionStoreException;

    void resolveAliases(StringValueResolver valueResolver);

}
