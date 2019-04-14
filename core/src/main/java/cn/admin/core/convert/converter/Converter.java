package cn.admin.core.convert.converter;


import cn.admin.lang.Nullable;

@FunctionalInterface
public interface Converter<S,T> {

    @Nullable
    T convert(S source);

}
