package org.apache.commons.logging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
public class LogFactoryService extends LogFactory {

    private final Map<String,Object> attributes = new ConcurrentHashMap<>();

    @Override
    public Log getInstance(Class<?> clazz) {
        return getInstance(clazz.getName());
    }

    @Override
    public Log getInstance(String name) {
        return LogAdapter.createLog(name);
    }

    public void setAttribute(String name, Object value) {
        if (value != null) {
            this.attributes.put(name, value);
        }
        else {
            this.attributes.remove(name);
        }
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public String[] getAttributeNames() {
        return this.attributes.keySet().toArray(new String[0]);
    }

    public void release() {
    }
}
