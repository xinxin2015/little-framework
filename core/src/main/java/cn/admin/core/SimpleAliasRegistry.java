package cn.admin.core;

import cn.admin.util.Assert;
import cn.admin.util.StringUtils;
import cn.admin.util.StringValueResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleAliasRegistry implements AliasRegistry {

    protected final Log logger = LogFactory.getLog(getClass());

    private final Map<String,String> aliasMap = new ConcurrentHashMap<>(16);

    @Override
    public void registerAlias(String name, String alias) {
        Assert.hasText(name,"'name' must not be empty");
        Assert.hasText(alias,"'alias' must not be empty");
        synchronized (this.aliasMap) {
            if (alias.equals(name)) {
                this.aliasMap.remove(alias);
                if (logger.isDebugEnabled()) {
                    logger.debug("Alias definition '" + alias + "' ignored since it points to same name");
                }
            } else {
                String registeredName = this.aliasMap.get(alias);
                if (registeredName != null) {
                    if (registeredName.equals(name)) {
                        return;
                    }
                    if (!allowAliasOverriding()) {
                        throw new IllegalStateException("Cannot define alias '" + alias + "' for name '" +
                                name + "': It is already registered for name '" + registeredName + "'.");
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Overriding alias '" + alias + "' definition for registered name '" +
                                registeredName + "' with new target name '" + name + "'");
                    }
                }
                checkForAliasCircle(name,alias);
                this.aliasMap.put(alias,name);
                if (logger.isTraceEnabled()) {
                    logger.trace("Alias definition '" + alias + "' registered for name '" + name + "'");
                }
            }
        }
    }

    protected boolean allowAliasOverriding() {
        return true;
    }

    public boolean hasAlias(String name,String alias) {
        for (Map.Entry<String,String> entry : this.aliasMap.entrySet()) {
            String registeredName = entry.getValue();
            if (registeredName.equals(name)) {
                String registeredAlias = entry.getKey();
                if (registeredAlias.equals(alias) || hasAlias(registeredAlias,alias)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void removeAlias(String alias) {
        synchronized (this.aliasMap) {
            String name = this.aliasMap.remove(alias);
            if (name == null) {
                throw new IllegalStateException("No alias '" + alias + "' registered");
            }
        }
    }

    private void retrieveAliases(String name, List<String> result) {
        this.aliasMap.forEach((alias,registeredName) -> {
            if (registeredName.equals(name)) {
                result.add(alias);
                retrieveAliases(alias,result);
            }
        });
    }

    public void resolveAliases(StringValueResolver valueResolver) {
        Assert.notNull(valueResolver,"StringValueResolver must not be null");
        synchronized (this.aliasMap) {
            Map<String,String> aliasCopy = new HashMap<>(this.aliasMap);
            aliasCopy.forEach((alias,registeredName) -> {
                String resolvedAlias = valueResolver.resolveStringValue(alias);
                String resolvedName = valueResolver.resolveStringValue(registeredName);
                if (resolvedAlias == null || resolvedName == null || resolvedAlias.equals(resolvedName)) {
                    this.aliasMap.remove(alias);
                } else if (!resolvedAlias.equals(alias)) {
                    String existingName = this.aliasMap.get(resolvedAlias);
                    if (existingName != null) {
                        if (existingName.equals(resolvedName)) {
                            // Pointing to existing alias - just remove placeholder
                            this.aliasMap.remove(alias);
                            return;
                        }
                        throw new IllegalStateException(
                                "Cannot register resolved alias '" + resolvedAlias + "' (original: '" + alias +
                                        "') for name '" + resolvedName + "': It is already registered for name '" +
                                        registeredName + "'.");
                    }
                    checkForAliasCircle(resolvedName, resolvedAlias);
                    this.aliasMap.remove(alias);
                    this.aliasMap.put(resolvedAlias, resolvedName);
                } else if (!registeredName.equals(resolvedName)) {
                    this.aliasMap.put(alias, resolvedName);
                }
            });
        }
    }

    @Override
    public boolean isAlias(String name) {
        return this.aliasMap.containsKey(name);
    }

    @Override
    public String[] getAliases(String name) {
        List<String> result = new ArrayList<>();
        synchronized (this.aliasMap) {
            retrieveAliases(name, result);
        }
        return StringUtils.toStringArray(result);
    }

    protected void checkForAliasCircle(String name,String alias) {
        if (hasAlias(name,alias)) {
            throw new IllegalStateException("Cannot register alias '" + alias +
                    "' for name '" + name + "': Circular reference - '" +
                    name + "' is a direct or indirect alias for '" + alias + "' already");
        }
    }

    public String canonicalName(String name) {
        String canonicalName = name;
        String resolvedName;
        do {
            resolvedName = this.aliasMap.get(canonicalName);
            if (resolvedName != null) {
                canonicalName = resolvedName;
            }
        } while (resolvedName != null);
        return canonicalName;
    }

}
