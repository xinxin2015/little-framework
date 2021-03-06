package cn.admin.core;

import cn.admin.lang.Nullable;
import cn.admin.util.ConcurrentReferenceHashMap;
import cn.admin.util.ObjectUtils;
import cn.admin.util.ReflectionUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.*;

final class SerializableTypeWrapper {

    private static final Class<?>[] SUPPORTED_SERIALIZABLE_TYPES = {
            GenericArrayType.class, ParameterizedType.class, TypeVariable.class,
            WildcardType.class};

    static final ConcurrentReferenceHashMap<Type,Type> cache =
            new ConcurrentReferenceHashMap<>(256);

    private SerializableTypeWrapper() {

    }

    @Nullable
    public static Type forField(Field field) {
        return forTypeProvider(new FieldTypeProvider(field));
    }

    @Nullable
    public static Type forMethodParameter(MethodParameter methodParameter) {
        return forTypeProvider(new MethodParameterTypeProvider(methodParameter));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Type> T unwrap(T type) {
        Type unwrapped = type;
        while (unwrapped instanceof SerializableTypeProxy) {
            unwrapped = ((SerializableTypeProxy)type).getTypeProvider().getType();
        }
        return unwrapped != null ? (T) unwrapped : type;
    }

    @Nullable
    static Type forTypeProvider(TypeProvider provider) {
        Type providerType = provider.getType();
        if (providerType == null || providerType instanceof Serializable) {
            return providerType;
        }
        if (GraalDetector.inImageCode() || !Serializable.class.isAssignableFrom(Class.class)) {
            return providerType;
        }
        Type cached = cache.get(providerType);
        if (cached != null) {
            return cached;
        }
        for (Class<?> type : SUPPORTED_SERIALIZABLE_TYPES) {
            if (type.isInstance(providerType)) {
                ClassLoader classLoader = provider.getClass().getClassLoader();
                Class<?>[] interfaces = new Class<?>[] {type,SerializableTypeProxy.class,
                        Serializable.class};
                InvocationHandler handler = new TypeProxyInvocationHandler(provider);
                cached = (Type) Proxy.newProxyInstance(classLoader,interfaces,handler);
                cache.put(providerType,cached);
                return cached;
            }
        }
        throw new IllegalArgumentException("Unsupported Type class: " + providerType.getClass().getName());
    }

    interface SerializableTypeProxy {

        TypeProvider getTypeProvider();

    }

    private static class TypeProxyInvocationHandler implements InvocationHandler,Serializable {

        private final TypeProvider provider;

        TypeProxyInvocationHandler(TypeProvider provider) {
            this.provider = provider;
        }

        @Override
        @Nullable
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("equals") && args != null) {
                Object other = args[0];
                if (other instanceof Type) {
                    other = unwrap((Type)other);
                }
                return ObjectUtils.nullSafeEquals(this.provider.getType(),other);
            } else if (method.getName().equals("hashCode")) {
                return ObjectUtils.nullSafeHashCode(this.provider.getType());
            } else if (method.getName().equals("getTypeProvider")) {
                return this.provider;
            }

            if (Type.class == method.getReturnType() && args == null) {
                return forTypeProvider(new MethodInvokeTypeProvider(this.provider,method,-1));
            } else if (Type[].class == method.getReturnType() && args == null) {
                Type[] result = new Type[((Type[]) method.invoke(this.provider.getType())).length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = forTypeProvider(new MethodInvokeTypeProvider(this.provider, method, i));
                }
                return result;
            }
            try {
                return method.invoke(this.provider.getType(), args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }

    interface TypeProvider extends Serializable {

        @Nullable
        Type getType();

        @Nullable
        default Object getSource() {
            return null;
        }

    }

    static class FieldTypeProvider implements TypeProvider {

        private final String fieldName;

        private final Class<?> declaringClass;

        private transient Field field;

        public FieldTypeProvider(Field field) {
            this.fieldName = field.getName();
            this.declaringClass = field.getDeclaringClass();
            this.field = field;
        }

        @Override
        public Type getType() {
            return this.field.getGenericType();
        }

        @Override
        public Object getSource() {
            return this.field;
        }

        private void readObject(ObjectInputStream inputStream) throws IOException,
                ClassNotFoundException {
            inputStream.defaultReadObject();
            try {
                this.field = this.declaringClass.getDeclaredField(this.fieldName);
            } catch (Throwable ex) {
                throw new IllegalStateException("Could not find original class structure",ex);
            }
        }
    }

    static class MethodParameterTypeProvider implements TypeProvider {

        @Nullable
        private final String methodName;

        private final Class<?>[] parameterTypes;

        private final Class<?> declaringClass;

        private final int parameterIndex;

        private transient MethodParameter methodParameter;

        public MethodParameterTypeProvider(MethodParameter methodParameter) {
            this.methodName = methodParameter.getMethod() != null ?
                    methodParameter.getMethod().getName() : null;
            this.parameterTypes = methodParameter.getExecutable().getParameterTypes();
            this.declaringClass = methodParameter.getDeclaringClass();
            this.parameterIndex = methodParameter.getParameterIndex();
            this.methodParameter = methodParameter;
        }

        @Override
        public Type getType() {
            return this.methodParameter.getGenericParameterType();
        }

        @Override
        public Object getSource() {
            return this.methodParameter;
        }

        private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
            inputStream.defaultReadObject();
            try {
                if (this.methodName != null) {
                    this.methodParameter = new MethodParameter(
                            this.declaringClass.getDeclaredMethod(this.methodName, this.parameterTypes), this.parameterIndex);
                }
                else {
                    this.methodParameter = new MethodParameter(
                            this.declaringClass.getDeclaredConstructor(this.parameterTypes), this.parameterIndex);
                }
            } catch (Throwable ex) {
                throw new IllegalStateException("Could not find original class structure", ex);
            }
        }
    }

    static class MethodInvokeTypeProvider implements TypeProvider {

        private final TypeProvider provider;

        private final String methodName;

        private final Class<?> declaringClass;

        private final int index;

        private transient Method method;

        @Nullable
        private transient volatile Object result;

        public MethodInvokeTypeProvider(TypeProvider provider,Method method,int index) {
            this.provider = provider;
            this.method = method;
            this.methodName = method.getName();
            this.declaringClass = method.getDeclaringClass();
            this.index = index;
        }

        @Override
        @Nullable
        public Type getType() {
            Object result = this.result;
            if (result == null) {
                // Lazy invocation of the target method on the provided type
                result = ReflectionUtils.invokeMethod(this.method, this.provider.getType());
                // Cache the result for further calls to getType()
                this.result = result;
            }
            return (result instanceof Type[] ? ((Type[]) result)[this.index] : (Type) result);
        }

        @Override
        @Nullable
        public Object getSource() {
            return null;
        }

        private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
            inputStream.defaultReadObject();
            Method method = ReflectionUtils.findMethod(this.declaringClass, this.methodName);
            if (method == null) {
                throw new IllegalStateException("Cannot find method on deserialization: " + this.methodName);
            }
            if (method.getReturnType() != Type.class && method.getReturnType() != Type[].class) {
                throw new IllegalStateException(
                        "Invalid return type on deserialized method - needs to be Type or Type[]: " + method);
            }
            this.method = method;
        }
    }

}
