package cn.admin.beans.factory.config;

import cn.admin.beans.BeansException;
import cn.admin.lang.Nullable;

public interface BeanExpressionResolver {

    @Nullable
    Object evaluate(@Nullable String value,BeanExpressionContext evalContext) throws BeansException;

}
