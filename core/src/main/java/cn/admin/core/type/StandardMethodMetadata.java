package cn.admin.core.type;

import cn.admin.core.annotation.AnnotatedElementUtils;
import cn.admin.util.Assert;
import cn.admin.util.MultiValueMap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

public class StandardMethodMetadata implements MethodMetadata {

    private final Method introspectedMethod;

    private final boolean nestedAnnotationsAsMap;

    public StandardMethodMetadata(Method introspectMethod) {
        this(introspectMethod,false);
    }

    public StandardMethodMetadata(Method introspectMethod,boolean nestedAnnotationsAsMap) {
        Assert.notNull(introspectMethod,"Method must not be null");
        this.introspectedMethod = introspectMethod;
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }

    @Override
    public String getMethodName() {
        return this.introspectedMethod.getName();
    }

    @Override
    public String getDeclaringClassName() {
        return this.introspectedMethod.getDeclaringClass().getName();
    }

    @Override
    public String getReturnTypeName() {
        return this.introspectedMethod.getReturnType().getName();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(this.introspectedMethod.getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(this.introspectedMethod.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(this.introspectedMethod.getModifiers());
    }

    @Override
    public boolean isOverridable() {
        return (!isStatic() && !isFinal() && !Modifier.isPrivate(this.introspectedMethod.getModifiers()));
    }

    @Override
    public boolean isAnnotated(String annotationName) {
        return AnnotatedElementUtils.isAnnotated(this.introspectedMethod,annotationName);
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName) {
        return getAnnotationAttributes(annotationName,false);
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        return AnnotatedElementUtils.getMergedAnnotationAttributes(this.introspectedMethod,
                annotationName,classValuesAsString,this.nestedAnnotationsAsMap);
    }

    @Override
    public MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName) {
        return getAllAnnotationAttributes(annotationName,false);
    }

    @Override
    public MultiValueMap<String, Object> getAllAnnotationAttributes(String annotationName, boolean classValuesAsString) {
        return AnnotatedElementUtils.getAllAnnotationAttributes(this.introspectedMethod,
                annotationName,classValuesAsString,this.nestedAnnotationsAsMap);
    }
}
