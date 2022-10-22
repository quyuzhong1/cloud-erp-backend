package com.erp.common.annotation;

import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.poi.ss.formula.functions.T;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
    /**
     * 部门表的别名
     */
    String tableField() default "";

    /**
     * 用户表的别名
     */
    String menuCode() default "";

    int index() default 0;

    /**
     * 拼接的sql
     */
    String param() default "param";

    /**
     * 操作类型 查询：query 修改：update 删除：delete
     */
    String operationType() default "operationType";

    /**
     * 服务类Class,一般用于DELETE
     */
    Class<? extends IService> serviceClass() default IService.class;


}
