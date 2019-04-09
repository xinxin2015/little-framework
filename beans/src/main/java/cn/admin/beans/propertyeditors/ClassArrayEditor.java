package cn.admin.beans.propertyeditors;

import cn.admin.lang.Nullable;
import cn.admin.util.ClassUtils;
import cn.admin.util.ObjectUtils;
import cn.admin.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.util.StringJoiner;

public class ClassArrayEditor extends PropertyEditorSupport {

    @Nullable
    private final ClassLoader classLoader;

    public ClassArrayEditor() {
        this(null);
    }

    public ClassArrayEditor(@Nullable ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            String classNames[] = StringUtils.commaDelimitedListToStringArray(text);
            Class<?>[] classes = new Class<?>[classNames.length];
            for (int i = 0;i < classNames.length;i ++) {
                String className = classNames[i].trim();
                classes[i] = ClassUtils.resolveClassName(className,classLoader);
            }
            setValue(classes);
        }
    }

    @Override
    public String getAsText() {
        Class<?>[] classes = (Class<?>[]) getValue();
        if (ObjectUtils.isEmpty(classes)) {
            return "";
        }
        StringJoiner sj = new StringJoiner(",");
        for (Class<?> klass : classes) {
            sj.add(ClassUtils.getQualifiedName(klass));
        }
        return sj.toString();
    }
}
