package cn.admin.beans;

import cn.admin.lang.Nullable;

public interface Mergeable {

    boolean isMergeEnabled();

    Object merge(@Nullable Object parent);

}
