package cn.admin.beans.factory;

public class BeanIsNotAFactoryException extends BeanNotOfRequiredTypeException {

    public BeanIsNotAFactoryException(String name, Class<?> actualType) {
        super(name, FactoryBean.class, actualType);
    }

}
