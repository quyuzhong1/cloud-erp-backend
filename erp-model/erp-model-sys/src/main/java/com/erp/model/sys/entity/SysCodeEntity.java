package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.util.Date;
import lombok.EqualsAndHashCode;

/**
 * @author Will
 * @version 1.0
 * @description: 业务编码表
 * @date 2022/11/21 11:24
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_code")
public class SysCodeEntity extends BaseEntity<SysCodeEntity> {

    /**
     * 类目
     */
    @TableField("category")
    private String category;

    /**
     * 顺序码
     */
    @TableField("num")
    private Integer num;

    /**
     * 编码类型 (枚举SysNoEnum，1:sku,2:spu)
     * 注：实际数据库列类型是 character varying（PostgreSQL 严禁 varchar = integer 隐式比较），
     *     这里显式指定 jdbcType=VARCHAR，使 INSERT/UPDATE/select-by-entity 走 VARCHAR 绑定，
     *     避免 BadSqlGrammarException: operator does not exist: character varying = integer。
     */
    @TableField(value = "type")
    private Integer type;

}
