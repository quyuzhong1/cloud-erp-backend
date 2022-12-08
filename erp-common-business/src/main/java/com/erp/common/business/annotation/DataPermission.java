package com.erp.common.business.annotation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.enums.DataAttributeEnum;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
    /**
     * 表别名：mapper.xml里写sql给表设置的别名
     */
    String tableAlias() default "";

    /**
     * 校验权限的字段 例如 ：tableField = create_user_id
     * 根据数据库表的create_user_id字段查询是否有权限操作数据
     */
    String tableField() default "";

    /**
     * 菜单权限编码
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
    DataAttributeEnum operationType();

    /**
     * 服务类Class,一般用于DELETE
     */
    Class<? extends IService> serviceClass() default IService.class;

    /**
     * 业务数据实体类名,用于Update
     * 如:entityName = "ProductInfoDTO"
     */
    String entityName() default "";

    /**
     * 入参主键id的名称
     */
    String keyIdName() default "";








}
