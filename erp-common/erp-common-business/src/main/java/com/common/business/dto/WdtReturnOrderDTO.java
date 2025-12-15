package com.common.business.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class WdtReturnOrderDTO extends UniqueDto {

    /**
     * 审核状态
     */
    private String approveStatus;
    /**
     * 单据类型
     */
    private String type;
    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 入库日期
     */
    private LocalDate billDate;
    /**
     * 库存组织id
     */
    private String inventoryOrgId;
    /**
     * 作废状态
     */
    private Boolean invalidStatus;
    /**
     * 审核人名称
     */
    private String approveUserName;
    /**
     * 审核时间
     */
    private LocalDateTime approveTime;
    /**
     * 来源id
     */
    private String sourceId;
    /**
     * 来源编号
     */
    private String sourceCode;
    /**
     * 来源类型
     */
    private String sourceType;
    /**
     * 仓库id
     */
    private String warehouseId;
    /**
     * 仓库名称
     */
    private String warehouseName;
    /**
     * 第三方单据编号
     */
    private String thirdCode;
    private String logisticsNo;

    /**
     * 创建人名称
     */
    private String createUserName;


    private String shopName;

    private String shopNo;

    private LocalDateTime created;
    
    private LocalDateTime modified;
    /**
     * 平台订单编号
     */
    private String platformOrderCode;

    private List<WdtReturnOrderDetailDTO> detailList;
}
