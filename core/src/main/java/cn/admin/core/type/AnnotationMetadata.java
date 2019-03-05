package cn.admin.core.type;

import java.util.Set;

public interface AnnotationMetadata extends ClassMetadata,AnnotatedTypeMetadata {

    Set<String> getAnnotationTypes();

    Set<String> getMetaAnnotationTypes(String annotationName);

    boolean hasAnnotation(String annotationName);

    boolean hasMetaAnnotation(String metaAnnotationName);

    boolean hasAnnotatedMethod(String annotationName);

    Set<MethodMetadata> getAnnotatedMethods(String annotationName);

}
