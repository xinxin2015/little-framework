package cn.admin.test;

import cn.admin.core.annotation.AliasFor;

import java.util.Arrays;

public class TestAnnotation {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(AliasFor.class.getDeclaredMethods()));
    }

}
