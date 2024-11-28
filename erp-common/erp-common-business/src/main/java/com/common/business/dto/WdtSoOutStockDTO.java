package com.common.business.dto;

import com.common.business.enums.ApproveStatusEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class WdtSoOutStockDTO extends UniqueDto {

    /**
     * code
     */
    private String code;

    /**
     * 审核状态
     */
    private ApproveStatusEnum approveStatus;


    /**
     * 销售订单id
     */
    private String soId;

    /**
     * 承运商 来源供应商
     */
    private String carrierId;

    /**
     * 作废状态
     * true 作废
     * false 未作废
     */
    private Boolean invalidStatus;

    /**
     * 销售员id
     */
    private String sellerId;

    /**
     * 销售员
     */
    private String sellerName;


    /**
     * 销售订单code
     */
    private String soCode;

    /**
     * 库存组织
     */
    private String warehouseOrgId;

    /**
     * 库存组织名
     */
    private String warehouseOrgName;



    /**
     * 预计发货日期
     */
    private LocalDate planDeliveryDate;

    /**
     * 打包日期
     */
    private LocalDate packDate;

    /**
     * 整单折扣额
     */
    private BigDecimal totalDiscountAmount;

    /**
     * 实际发货日期
     */
    private LocalDateTime actualDeliveryDate;

    /**
     * 运输单号
     */
    private String trackNo;

    /**
     * 仓管员
     */
    private String warehouseKeeperId;

    /**
     * 仓管员
     */
    private String warehouseKeeperName;


    /**
     * 来源id
     */
    private String sourceId;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 来源code
     */
    private String sourceCode;


    /**
     * type
     * 单据类型 冗余
     */
    private String orderType;

    /**
     * 仓库id
     */
    private String warehouseId;

    /**
     * 仓库名称
     */
    private String warehouseName;


    /**
     * 审核人
     */
    private String approveUserName;

    /**
     * 审核时间
     */
    private LocalDateTime approveTime;

    /**
     * 客户id
     */
    private String customerId;

    /**
     * 同步金蝶id
     */
    private String syncKingdeeId;

    /**
     * 出库日期
     */
    private LocalDate billDate;

    /**
     * 客户订单号
     */
    private String customerOrderNo;


    /**
     * 销售部门id
     */
    private String salesDeptId;


    /**
     * 销售组织id
     */
    private String salesOrgId;


    /**
     * 销售组织名
     */
    private String salesOrgName;


    /**
     * 客户名
     */
    private String customerName;

    /**
     * 国家
     */
    private String country;


    /**
     * 第三方单据编号
     */
    private String thirdCode;

    /**
     * 物流渠道id
     */
    private String logisticsChannelId;

    /**
     * 装箱状态 notPacking：未装箱，packing：已装箱
     * 枚举：PackingStatusEnum
     */
    private String packingStatus;


    /**
     * 报关状态
     * 枚举：DeclareStatusEnum
     */
    private String declareStatus;

    private String shopId;

    private String shopName;

    private String shopNo;

    private LocalDateTime created;
    /**
     * 创建人名称
     */
    private String createUserName;
    
    /**
     * 物流公司代码
    */
    private String logisticsCompanyCode;
    
    /**
     * 物流公司名称
     */
    private String logisticsCompanyName;
    
    /**
     * 订单标签
     */
    private String tradeLabel;


    private List<WdtSoOutStockDetailDTO> detailList;
}
