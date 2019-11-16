package xyz.phanta.jxmlrpc;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XmlRpcRoutine {

    String name() default "";

}
