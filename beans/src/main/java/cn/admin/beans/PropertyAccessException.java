package cn.admin.beans;

import cn.admin.lang.Nullable;

import java.beans.PropertyChangeEvent;

public abstract class PropertyAccessException extends BeansException {

    @Nullable
    private final PropertyChangeEvent propertyChangeEvent;

    public PropertyAccessException(PropertyChangeEvent propertyChangeEvent, String msg, @Nullable Throwable cause) {
        super(msg, cause);
        this.propertyChangeEvent = propertyChangeEvent;
    }

    public PropertyAccessException(String msg, @Nullable Throwable cause) {
        super(msg, cause);
        this.propertyChangeEvent = null;
    }

    @Nullable
    public PropertyChangeEvent getPropertyChangeEvent() {
        return propertyChangeEvent;
    }

    @Nullable
    public String getPropertyName() {
        return this.propertyChangeEvent != null ? this.propertyChangeEvent.getPropertyName() : null;
    }

    @Nullable
    public Object getValue() {
        return this.propertyChangeEvent != null ? this.propertyChangeEvent.getNewValue() : null;
    }

    public abstract String getErrorCode();

}
