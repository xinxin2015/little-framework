package cn.admin.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

public class CharArrayPropertyEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(text != null ? text.toCharArray() : null);
    }

    @Override
    public String getAsText() {
        char[] value = (char[]) getValue();
        return value != null ? new String(value) : "";
    }
}
