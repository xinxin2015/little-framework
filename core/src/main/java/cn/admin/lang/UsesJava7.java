package cn.admin.lang;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD,ElementType.CONSTRUCTOR,ElementType.TYPE})
@Documented
@Deprecated
public @interface UsesJava7 {
}
