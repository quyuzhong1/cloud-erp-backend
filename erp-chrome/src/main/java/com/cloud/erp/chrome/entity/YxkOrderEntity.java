package com.cloud.erp.chrome.entity;

import java.math.BigDecimal;

import cn.hutool.core.annotation.Alias;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 云星空
 * </p>
 *
 * @author yl
 * @since 2022-08-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sales_order_yunxingkong")
public class YxkOrderEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 出货日期
     */
    @TableField("shipment_date")
    private String shipmentDate;

    /**
     * 单据编号
     */

    @TableField("document_no")
    private String documentNo;

    /**
     * 客户名
     */
    @TableField("customers")
    private String customers;

    /**
     * 销售部门
     */
    @TableField("sales_departments")
    private String salesDepartments;

    /**
     * sku 编号
     */
    @TableField("sku")
    private String sku;

    /**
     * 商品名
     */
    @TableField("trade_name")
    private String tradeName;

    /**
     * 数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 仓库
     */
    @TableField("warehouse")
    private String warehouse;

    /**
     * 单价
     */
    @TableField("unit_price")
    private Double unitPrice;

    /**
     * 含税单价
     */
    @TableField("tax_unit_price")
    private Double taxUnitPrice;

    /**
     * 金额
     */
    @TableField("money")
    private Double money;

    /**
     * 含税金额
     */
    @TableField("tax_money")
    private Double taxMoney;

    /**
     * 订单编号
     */
    @TableField("order_number")
    private String orderNumber;

    /**
     * 货币
     */
    @TableField("currency")
    private String currency;

    @TableField("order_date")
    private Date orderDate;


}
