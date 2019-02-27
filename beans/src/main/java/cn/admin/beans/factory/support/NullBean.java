package cn.admin.beans.factory.support;

import cn.admin.lang.Nullable;

final class NullBean {

    NullBean() {
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        return (this == obj || obj == null);
    }

    @Override
    public int hashCode() {
        return NullBean.class.hashCode();
    }

    @Override
    public String toString() {
        return "null";
    }

}