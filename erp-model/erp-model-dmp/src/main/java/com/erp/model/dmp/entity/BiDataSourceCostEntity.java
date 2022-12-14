package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 15:20
 */
@TableName(value ="bi_data_source_cost")
@Data
public class BiDataSourceCostEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableField("id")
    private String id;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private String createTime;

    /**
     * 修改时间
     */
    @TableField("update_time")
    private String updateTime;

    /**
     * 创建人名称
     */
    @TableField("create_user_name")
    private String createUserName;

    /**
     * 创建人id
     */
    @TableField("create_user_id")
    private String createUserId;

    /**
     * 修改人名称
     */
    @TableField("update_user_name")
    private String updateUserName;

    /**
     * 修改人id
     */
    @TableField("update_user_id")
    private String updateUserId;

    /**
     * 月份
     */
    @TableField("month")
    private String month;

    /**
     * 店铺名称
     */
    @TableField("shop_name")
    private String shopName;

    /**
     * 站点
     */
    @TableField("site")
    private String site;

}
