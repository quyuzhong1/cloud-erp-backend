package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * bi系统字典表(BiDict)实体类
 *
 * @author yl
 * @since 2022-12-08 12:31:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bi_dict")
@Accessors(chain = true)
public class BiDictEntity implements Serializable {
    private static final long serialVersionUID = -91581713878172579L;
    /**
     * 表id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 值
     */
    private String value;
    /**
     * 类型
     */
    private String type;
    /**
     * 名
     */
    private String name;
    /**
     * 备注
     */
    private String remark;
    /**
     * 序号
     */
    private Integer order_index;
    /**
     * 状态0 未开启 1 已开启
     */
    private Integer state;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;



}

