package cn.admin.beans.factory;

public class BeanIsAbstractException extends BeanCreationException {

    public BeanIsAbstractException(String beanName) {
        super(beanName,"Bean Definition is abstract");
    }

}
