package com.common.core.anno;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 正则校验
 *
 * @author yl
 * @Classname RegularValid
 * @Description TODO
 * @Date 2023-03-14 14:45
 * @Created by yl
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {RegularValidator.class})
public @interface RegularValid {

    //默认错误
    String message() default "格式不正确";

    //正则表达
    String formatPattern() default "";

    // 分组
    Class<?>[] groups() default {};

    // 负载
    Class<? extends Payload>[] payload() default {};
}
