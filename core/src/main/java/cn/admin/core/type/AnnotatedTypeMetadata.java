package cn.admin.core.type;

import cn.admin.lang.Nullable;
import cn.admin.util.MultiValueMap;

import java.util.Map;

public interface AnnotatedTypeMetadata {

    boolean isAnnotated(String annotationName);

    @Nullable
    Map<String,Object> getAnnotationAttributes(String annotationName);

    @Nullable
    Map<String,Object> getAnnotationAttributes(String annotationName,boolean classValuesAsString);

    @Nullable
    MultiValueMap<String,Object> getAllAnnotationAttributes(String annotationName);

    @Nullable
    MultiValueMap<String,Object> getAllAnnotationAttributes(String annotationName,
                                                            boolean classValuesAsString);

}
