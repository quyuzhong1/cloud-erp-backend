package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author yl
 * @since 2022-08-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_base_dic")
public class SysBaseDicEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Integer id;

    /**
     * 字典属性
     */
    @TableField("dic_type")
    private String dicType;

    /**
     * 对应的值
     */
    @TableField("dic_value")
    private String dicValue;

    /**
     * 标题
     */
    @TableField("dic_title")
    private String dicTitle;


}
