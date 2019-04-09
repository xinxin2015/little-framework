package cn.admin.beans;

import cn.admin.core.convert.ConversionService;
import cn.admin.lang.Nullable;
import cn.admin.util.ClassUtils;

import java.beans.PropertyEditor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PropertyEditorRegistrySupport implements PropertyEditorRegistry {

    @Nullable
    private ConversionService conversionService;

    private boolean defaultEditorsActive = false;

    private boolean configValueEditorActive = false;

    @Nullable
    private Map<Class<?>,PropertyEditor> defaultEditors;

    @Nullable
    private Map<Class<?>,PropertyEditor> overriddenDefaultEditors;

    @Nullable
    private Map<Class<?>,PropertyEditor> customEditors;

    @Nullable
    private Map<String,CustomEditorHolder> customEditorsForPath;

    @Nullable
    private Map<Class<?>,PropertyEditor> customEditorCache;

    public void setConversionService(@Nullable ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Nullable
    public ConversionService getConversionService() {
        return conversionService;
    }

    protected void registerDefaultEditors() {
        this.defaultEditorsActive = true;
    }

    public void useConfigValueEditors() {
        this.configValueEditorActive = true;
    }

    public void overrideDefaultEditor(Class<?> requiredType,PropertyEditor propertyEditor) {
        if (this.overriddenDefaultEditors == null) {
            this.overriddenDefaultEditors = new HashMap<>();
        }
        this.overriddenDefaultEditors.put(requiredType,propertyEditor);
    }

    @Nullable
    public PropertyEditor getDefaultEditor(Class<?> requiredType) {
        if (!this.defaultEditorsActive) {
            return null;
        }
        if (this.overriddenDefaultEditors != null) {
            PropertyEditor editor = this.overriddenDefaultEditors.get(requiredType);
            if (editor != null) {
                return editor;
            }
        }
        if (this.defaultEditors == null) {
            createDefaultEditors();
        }
        return this.defaultEditors.get(requiredType);
    }

    private void createDefaultEditors() {
        this.defaultEditors = new HashMap<>(64);
    }

    @Override
    public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {

    }

    @Override
    public void registerCustomEditor(Class<?> requiredType, String propertyPath,
                                     PropertyEditor propertyEditor) {

    }

    @Override
    public PropertyEditor findCustomEditor(Class<?> requiredType, String propertyPath) {
        return null;
    }

    private static final class CustomEditorHolder {

        private final PropertyEditor propertyEditor;

        @Nullable
        private final Class<?> registeredType;

        private CustomEditorHolder(PropertyEditor propertyEditor,
                                    @Nullable Class<?> registeredType) {
            this.propertyEditor = propertyEditor;
            this.registeredType = registeredType;
        }

        private PropertyEditor getPropertyEditor() {
            return propertyEditor;
        }

        @Nullable
        private Class<?> getRegisteredType() {
            return registeredType;
        }

        @Nullable
        private PropertyEditor getPropertyEditor(@Nullable Class<?> requiredType) {
            if (this.registeredType == null ||
                    (requiredType != null &&
                            (ClassUtils.isAssignable(this.registeredType,requiredType) ||
                            ClassUtils.isAssignable(requiredType,this.registeredType))) ||
                    (requiredType == null &&
                            (!Collection.class.isAssignableFrom(this.registeredType) && !this.registeredType.isArray()))) {
                return this.propertyEditor;
            } else {
                return null;
            }
        }
    }
}
