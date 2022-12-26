package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/21 16:45
 */
@Data
@TableName(value ="bi_target_management")
public class BiTargetManagementEntity {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 平台名称
     */
    @TableField(value = "platform_name")
    private String platformName;

    /**
     * 品类
     */
    @TableField(value = "category")
    private String category;

    /**
     * 品类Id
     */
    @TableField(value = "category_id")
    private String categoryId;

    /**
     * 销售类型（0销量，1销售额）
     */
    @TableField(value = "sale_type")
    private Integer saleType;

    /**
     * 产品类型（0新品，1老品）
     */
    @TableField(value = "product_type")
    private Integer productType;

    /**
     * 产品定位
     */
    @TableField(value = "product_position")
    private String productPosition;

    /**
     * sku/spu
     */
    @TableField(value = "product_no")
    private String productNo;

    /**
     * 品名
     */
    @TableField(value = "product_name")
    private String productName;

    /**
     * 客单价
     */
    @TableField(value = "per_customer_transaction")
    private BigDecimal perCustomerTransaction;

    /**
     * 一月
     */
    @TableField(value = "january")
    private BigDecimal january;

    /**
     * 二月
     */
    @TableField(value = "february")
    private BigDecimal february;


    /**
     * 三月
     */
    @TableField(value = "march")
    private BigDecimal march;


    /**
     * 四月
     */
    @TableField(value = "april")
    private BigDecimal april;

    /**
     * 五月
     */
    @TableField(value = "may")
    private BigDecimal may;

    /**
     * 六月
     */
    @TableField(value = "june")
    private BigDecimal june;

    /**
     * 七月
     */
    @TableField(value = "july")
    private BigDecimal july;

    /**
     * 八月
     */
    @TableField(value = "august")
    private BigDecimal august;

    /**
     * 九月
     */
    @TableField(value = "september")
    private BigDecimal september;

    /**
     * 十月
     */
    @TableField(value = "october")
    private BigDecimal october;

    /**
     * 十一月
     */
    @TableField(value = "november")
    private BigDecimal november;

    /**
     * 十二月
     */
    @TableField(value = "december")
    private BigDecimal december;

}
