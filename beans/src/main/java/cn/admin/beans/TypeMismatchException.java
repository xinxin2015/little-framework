package cn.admin.beans;

import cn.admin.lang.Nullable;
import cn.admin.util.Assert;
import cn.admin.util.ClassUtils;

import java.beans.PropertyChangeEvent;

public class TypeMismatchException extends PropertyAccessException {

    public static final String ERROR_CODE = "typeMismatch";

    @Nullable
    private String propertyName;

    @Nullable
    private transient Object value;

    @Nullable
    private Class<?> requiredType;

    public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, Class<?> requiredType) {
        this(propertyChangeEvent, requiredType, null);
    }

    public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, @Nullable Class<?> requiredType,
                                 @Nullable Throwable cause) {

        super(propertyChangeEvent,
                "Failed to convert property value of type '" +
                        ClassUtils.getDescriptiveType(propertyChangeEvent.getNewValue()) + "'" +
                        (requiredType != null ?
                                " to required type '" + ClassUtils.getQualifiedName(requiredType) + "'" : "") +
                        (propertyChangeEvent.getPropertyName() != null ?
                                " for property '" + propertyChangeEvent.getPropertyName() + "'" : ""),
                cause);
        this.propertyName = propertyChangeEvent.getPropertyName();
        this.value = propertyChangeEvent.getNewValue();
        this.requiredType = requiredType;
    }

    public TypeMismatchException(@Nullable Object value, @Nullable Class<?> requiredType) {
        this(value, requiredType, null);
    }

    public TypeMismatchException(@Nullable Object value, @Nullable Class<?> requiredType, @Nullable Throwable cause) {
        super("Failed to convert value of type '" + ClassUtils.getDescriptiveType(value) + "'" +
                        (requiredType != null ? " to required type '" + ClassUtils.getQualifiedName(requiredType) + "'" : ""),
                cause);
        this.value = value;
        this.requiredType = requiredType;
    }

    public void initPropertyName(String propertyName) {
        Assert.state(this.propertyName == null, "Property name already initialized");
        this.propertyName = propertyName;
    }

    @Override
    @Nullable
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    @Nullable
    public Object getValue() {
        return value;
    }

    @Nullable
    public Class<?> getRequiredType() {
        return requiredType;
    }

    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}
