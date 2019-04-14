package cn.admin.core.env;

import java.util.Map;

public interface ConfigurableEnvironment extends Environment,ConfigurablePropertyResolver {

    void setActiveProfiles(String ...profiles);

    void addActiveProfile(String profiles);

    void setDefaultProfiles(String ...profiles);

    MutablePropertySources getPropertySources();

    Map<String,Object> getSystemProperties();

    Map<String,Object> getSystemEnvironment();

    void merge(ConfigurableEnvironment parent);

}
