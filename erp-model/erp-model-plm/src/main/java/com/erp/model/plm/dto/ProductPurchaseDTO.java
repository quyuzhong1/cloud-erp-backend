package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String id;

    /**
     * sku表id
     */
    private String skuId;

    /**
     * EAN码
     */
    @Size(max = 200,message = "EAN码不能大于200字符")
    private String ean;

    /**
     * 计划首批下单量
     */
    @DecimalMax(value = "99999999",message ="计划首批下单量最大值为99999999" )
    @DecimalMin(value = "1",message ="最小值为1" )
    private Long planOrderQty;

    /**
     * 首批下单时间
     */
    private LocalDateTime placeOrderTime;

    /**
     * 预计首批到货时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planArrivalTime;

    /**
     * MOQ(最小起订量)
     */
    @DecimalMax(value = "99999999",message ="MOQ最大值为99999999" )
    @DecimalMin(value = "1",message ="最小值为1" )
    private Integer moq;

    /**
     * 交货周期(天)
     */
    @Digits(integer = 16,fraction = 4,message = "报关申报价格最大16字符，小数位不能大于4位")
    private BigDecimal deliveryCycle;

    /**
     * 实际首批到货时间
     */
    private LocalDate actualArrivalTime;

    /**
     * 首批到货状态：1.未到货 2.已到货 3.部分到货
     */
    private Integer arrivalState;

    /**
     * 采购员
     */
    private String purchaseUserId;

    /**
     * 一级供应商
     */
    @Size(max = 200,message = "一级供应商最大200字符")
    private String mainSupplier;

    /**
     * 二级供应商
     */
    @Size(max = 200,message = "二级供应商最大200字符")
    private String secondSupplier;

    /**
     * 单位表id
     */
    private String createUserId;

    /**
     * 单位表id
     */
    private String updateUserId;

    /**
     * 实际首批到货量
     */
    @DecimalMax(value = "99999999",message ="实际首批到货量最大值为99999999" )
    @DecimalMin(value = "1",message ="最小值为1" )
    private Long actualArrivalQty;

    /**
     * 试产数量
     */
    @DecimalMax(value = "99999999",message ="试产数量最大值为99999999" )
    @DecimalMin(value = "1",message ="最小值为1" )
    private Long trialProductionQty;

    /**
     * 首批量产数量
     */
    @DecimalMax(value = "99999999",message ="首批量产数量最大值为99999999" )
    @DecimalMin(value = "1",message ="最小值为1" )
    private Long firstMassQty;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}