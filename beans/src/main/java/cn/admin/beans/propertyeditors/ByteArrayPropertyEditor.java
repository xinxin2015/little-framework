package cn.admin.beans.propertyeditors;

import cn.admin.lang.Nullable;

import java.beans.PropertyEditorSupport;

public class ByteArrayPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(@Nullable String text) throws IllegalArgumentException {
        setValue(text != null ? text.getBytes() : null);
    }

    @Override
    public String getAsText() {
        byte[] value = (byte[]) getValue();
        return value != null ? new String(value) : "";
    }
}
