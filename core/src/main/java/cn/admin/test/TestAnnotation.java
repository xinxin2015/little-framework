package cn.admin.test;

import cn.admin.core.annotation.AliasFor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;

public class TestAnnotation implements Serializable {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(AliasFor.class.getDeclaredMethods()));
    }

    private void readObject(ObjectInputStream inputStream) throws IOException,
            ClassNotFoundException {
        inputStream.defaultReadObject();
        System.out.println("hello");
    }

}
