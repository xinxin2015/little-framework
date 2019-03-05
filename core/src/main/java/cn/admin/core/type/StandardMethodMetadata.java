package cn.admin.core.type;

import cn.admin.util.Assert;
import cn.admin.util.MultiValueMap;

import java.lang.reflect.Method;
import java.util.Map;

public class StandardMethodMetadata implements MethodMetadata {

    private final Method introspectMethod;

    private final boolean nestedAnnotationsAsMap;

    public StandardMethodMetadata(Method introspectMethod) {
        this(introspectMethod,false);
    }

    public StandardMethodMetadata(Method introspectMethod,boolean nestedAnnotationsAsMap) {
        Assert.notNull(introspectMethod,"Method must not be null");
        this.introspectMethod = introspectMethod;
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }

    @Override
    public String getMethodName() {
        return this.introspectMethod.getName();
    }

    @Override
    public String getDeclaringClassName() {
        return this.introspectMethod.getDeclaringClass().getName();
    }

    @Override
    public String getReturnTypeName() {
        return this.introspectMethod.getReturnType().getName();
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isOverridable() {
        return false;
    }

    @Override
    public boolean isAnnotated(String annotationName) {
        return false;
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName) {
        return null;
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        return null;
    }

    @Override
    public MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName) {
        return null;
    }

    @Override
    public MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        return null;
    }
}
