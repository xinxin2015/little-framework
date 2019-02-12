package cn.admin.beans.factory;

import cn.admin.beans.BeansException;

@FunctionalInterface
public interface ObjectFactory<T> {

    T getObject() throws BeansException;

}
