package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 业务编码表
 * @date 2022/11/21 11:24
 */
@Data
@TableName("sys_code")
public class SysCodeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

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
    @TableField(value = "type", jdbcType = JdbcType.VARCHAR)
    private Integer type;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id" , fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name" , fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 更新人id
     */
    @TableField(value = "update_user_id" , fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 更新人
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;
}
