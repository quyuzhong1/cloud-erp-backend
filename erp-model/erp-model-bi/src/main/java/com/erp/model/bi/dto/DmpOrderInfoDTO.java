package com.erp.model.bi.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/13 15:57
 */
@Data
@NoArgsConstructor
public class DmpOrderInfoDTO {

    /**
     * 主键id
     */
    private String id;

    /**
     * 平台订单id
     */
    private String platformOrderId;

    /**
     * 平台名称
     */
    private String sourcePlatform;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 订单销售额[原币种]
     */
    private BigDecimal itemTotal;

    /**
     * 订单销售额[RMB-实时]
     */
    private String saleTotal;

    /**
     * 订单销售额[RMB-实时]
     */
    private String saleSettlementTotal;

    /**
     * 买家姓名（下单人）
     */
    private String buyerName;

    /**
     * 买家电话1（下单电话）
     */
    private String manPhone;

    /**
     * 买家电话2（下单电话）
     */
    private String secondPhone;

    /**
     * 买家地址1（下单地址）
     */
    private String manStreet;

    /**
     * 买家地址2（下单地址）
     */
    private String secondStreet;

    /**
     * 国家名称
     */
    private String countryNameCn;

    /**
     * 订单状态 2.配货中 3.已发货 4.已完成 5.已作废
     */
    private Integer orderState;

    /**
     * 订单下单时间
     */
    private Date createTime;

    /**
     * 订单发货时间
     */
    private Date deliveryDate;

    /**
     * 销售员
     */
    private String salesManName;
}
