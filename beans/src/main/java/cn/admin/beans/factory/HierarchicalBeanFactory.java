package cn.admin.beans.factory;

import cn.admin.lang.Nullable;

public interface HierarchicalBeanFactory extends BeanFactory {

    @Nullable
    BeanFactory getParentBeanFactory();

    boolean containsLocalBean(String name);

}
