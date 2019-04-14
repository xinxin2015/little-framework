package cn.admin.core.env;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractEnvironment implements ConfigurableEnvironment {

    public static final String IGNORE_GETENV_PROPERTY_NAME = "spring.getenv.ignore";

    public static final String ACTIVE_PROFILES_PROPERTY_NAME = "spring.profiles.active";

    public static final String DEFAULT_PROFILES_PROPERTY_NAME = "spring.profiles.default";

    protected static final String RESERVED_DEFAULT_PROFILE_NAME = "default";

    protected final Log logger = LogFactory.getLog(getClass());

    private final Set<String> activeProfiles = new LinkedHashSet<>();

    private final Set<String> defaultProfiles = new LinkedHashSet<>();

    private final MutablePropertySources propertySources = new MutablePropertySources();

    private final ConfigurablePropertyResolver propertyResolver =

}
