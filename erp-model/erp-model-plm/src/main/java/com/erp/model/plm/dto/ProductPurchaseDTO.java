package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.erp.common.annotation.StateEnumValue;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Description 产品采购信息表请求参数
 * @TableName product_purchase
 * @Author Luo_WG
 * @Date 2022/9/23 12:14
 **/
@Data
@NoArgsConstructor
public class ProductPurchaseDTO implements Serializable {

    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    private String id;

    /**
     * sku表id
     */
    @ApiModelProperty(value = "sku表id")
    private String skuId;

    /**
     * EAN码
     */
    @ApiModelProperty(value = "EAN码")
    private Integer ean;

    /**
     * 计划首批下单量
     */
    @ApiModelProperty(value = "计划首批下单量")
    private Integer planOrderQty;

    /**
     * 首批下单时间
     */
    @ApiModelProperty(value = "首批下单时间")
    private Date placeOrderTime;

    /**
     * 预计首批到货时间
     */
    @ApiModelProperty(value = "预计首批到货时间")
    private Date planArrivalTime;

    /**
     * MOQ(最小起订量)
     */
    @ApiModelProperty(value = "MOQ(最小起订量)")
    private Integer moq;

    /**
     * 交货周期(天)
     */
    @ApiModelProperty(value = "交货周期(天)")
    private BigDecimal deliveryCycle;

    /**
     * 实际首批到货时间
     */
    @ApiModelProperty(value = "实际首批到货时间")
    private Date actualArrivalTime;

    /**
     * 首批到货状态：1.未到货 2.已到货 3.部分到货
     */
    @ApiModelProperty(value = "首批到货状态：1.未到货 2.已到货 3.部分到货")
    private Integer arrivalState;

    /**
     * 采购员
     */
    @ApiModelProperty(value = "采购员")
    private Integer purchaseUserId;

    /**
     * 一级供应商
     */
    @ApiModelProperty(value = "一级供应商")
    private Integer mainSupplier;

    /**
     * 二级供应商
     */
    @ApiModelProperty(value = "二级供应商")
    private Integer secondSupplier;

    /**
     * 单位表id
     */
    @ApiModelProperty(value = "单位表id")
    private String createUserId;

    /**
     * 单位表id
     */
    @ApiModelProperty(value = "单位表id")
    private String updateUserId;

    /**
     * 实际首批到货量
     */
    @ApiModelProperty(value = "实际首批到货量")
    private Integer actualArrivalQty;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}