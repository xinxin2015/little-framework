package cn.admin.core.convert;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class TypeDescriptor implements Serializable {

    private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

    private static final Map<Class<?>,TypeDescriptor> commonTypesCache = new HashMap<>(32);

    private static final Class<?>[] CACHED_COMMON_TYPES = {
            boolean.class,Boolean.class,byte.class,Byte.class,char.class,Character.class,
            double.class,Double.class,float.class,Float.class,int.class,Integer.class,
            long.class,Long.class,short.class,Short.class,String.class,Object.class
    };

    static {
        for (Class<?> preCachedClass : CACHED_COMMON_TYPES) {
            //TODO commonTypesCache.put(preCachedClass,)
        }
    }

    private final Class<?> type = null;

    //TODO

}
