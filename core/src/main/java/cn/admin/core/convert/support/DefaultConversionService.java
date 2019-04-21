package cn.admin.core.convert.support;

import cn.admin.core.convert.ConversionService;
import cn.admin.core.convert.converter.ConverterRegistry;
import cn.admin.lang.Nullable;

public class DefaultConversionService extends GenericConversionService {

    @Nullable
    private static volatile DefaultConversionService sharedInstance;

    public DefaultConversionService() {
        addDefaultConverters(this);
    }

    public static ConversionService getSharedInstance() {
        DefaultConversionService cs = sharedInstance;
        if (cs == null) {
            synchronized (DefaultConversionService.class) {
                cs = sharedInstance;
                if (cs == null) {
                    cs = new DefaultConversionService();
                    sharedInstance = cs;
                }
            }
        }
        return cs;
    }

    public static void addDefaultConverters(ConverterRegistry converterRegistry) {
        //TODO
    }

    public static void addCollectionConverters(ConverterRegistry converterRegistry) {
        //TODO
    }

    private static void addScalarConverters(ConverterRegistry converterRegistry) {
        //TODO
    }

}
