package cn.admin.core;

import cn.admin.lang.Nullable;
import cn.admin.util.ConcurrentReferenceHashMap;

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

    interface SerializableTypeProxy {

        TypeProvider getTypeProvider();

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

        private final int paramterIndex;

        //TODO private transient

        @Override
        public Type getType() {
            return null;
        }
    }

}
