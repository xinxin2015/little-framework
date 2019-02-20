package cn.admin.beans.factory.config;

import cn.admin.beans.factory.InjectionPoint;
import cn.admin.core.MethodParameter;
import cn.admin.core.ResolvableType;
import cn.admin.core.convert.TypeDescriptor;
import cn.admin.lang.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;

public class DependencyDescriptor extends InjectionPoint implements Serializable {

    private final Class<?> declaringClass;

    @Nullable
    private String methodName;

    @Nullable
    private Class<?>[] parameterTypes;

    private int parameterIndex;

    @Nullable
    private String fieldName;

    private final boolean required;

    private final boolean eager;

    private int nestingLevel = 1;

    @Nullable
    private Class<?> containingClass;

    @Nullable
    private transient volatile ResolvableType resolvableType;

    @Nullable
    private transient volatile TypeDescriptor typeDescriptor;

    public DependencyDescriptor(MethodParameter methodParameter,boolean required) {
        this(methodParameter,required,true);
    }

    public DependencyDescriptor(MethodParameter methodParameter,boolean required,boolean eager) {
        super(methodParameter);
        this.declaringClass = methodParameter.getDeclaringClass();
        if (methodParameter.getMethod() != null) {
            this.methodName = methodParameter.getMethod().getName();
        }
        this.parameterTypes = methodParameter.getExecutable().getParameterTypes();
        this.parameterIndex = methodParameter.getParameterIndex();
        this.containingClass = methodParameter.getContainingClass();
        this.required = required;
        this.eager = eager;
    }

    public DependencyDescriptor(Field field,boolean required,boolean eager) {
        super(field);
        this.declaringClass = field.getDeclaringClass();
        this.fieldName = field.getName();
        this.required = required;
        this.eager = eager;
    }

}
