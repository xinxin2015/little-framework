package cn.admin.core.type;

import cn.admin.core.annotation.AnnotatedElementUtils;
import cn.admin.util.MultiValueMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class StandardAnnotationMetadata extends StandardClassMetadata implements AnnotationMetadata {

    private final Annotation[] annotations;

    private final boolean nestedAnnotationsAsMap;

    public StandardAnnotationMetadata(Class<?> introspectedClass) {
        this(introspectedClass,false);
    }

    public StandardAnnotationMetadata(Class<?> introspectedClass,boolean nestedAnnotationsAsMap) {
        super(introspectedClass);
        this.annotations = introspectedClass.getAnnotations();
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }

    @Override
    public Set<String> getAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Annotation ann : this.annotations) {
            types.add(ann.annotationType().getName());
        }
        return types;
    }

    @Override
    public Set<String> getMetaAnnotationTypes(String annotationName) {
        return this.annotations.length > 0 ?
                AnnotatedElementUtils.getMetaAnnotationTypes(getIntrospectedClass(),
                        annotationName) : Collections.emptySet();
    }

    @Override
    public boolean hasAnnotation(String annotationName) {
        for (Annotation ann : this.annotations) {
            if (ann.annotationType().getName().equals(annotationName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasMetaAnnotation(String metaAnnotationName) {
        return this.annotations.length > 0 && AnnotatedElementUtils.hasMetaAnnotationTypes(getIntrospectedClass(),metaAnnotationName);
    }

    @Override
    public boolean hasAnnotatedMethod(String annotationName) {
        return this.annotations.length > 0 && AnnotatedElementUtils.isAnnotated(getIntrospectedClass(),annotationName);
    }

    @Override
    public Set<MethodMetadata> getAnnotatedMethods(String annotationName) {
        try {
            Method[] methods = getIntrospectedClass().getDeclaredMethods();
            Set<MethodMetadata> annotatedMethods = new LinkedHashSet<>(4);
            for (Method method : methods) {
                if (!method.isBridge() && method.getAnnotations().length > 0 && AnnotatedElementUtils.isAnnotated(method,annotationName)) {
                    annotatedMethods.add(null);//TODO
                }
            }
            return annotatedMethods;
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to introspect annotated methods on " + getIntrospectedClass(), ex);
        }
    }

    @Override
    public boolean isAnnotated(String annotationName) {
        return false;
    }

    @Override
    public Map<String, Object> getAnnotationAttributes(String annotationName) {
        return getAnnotationAttributes(annotationName,false);
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
