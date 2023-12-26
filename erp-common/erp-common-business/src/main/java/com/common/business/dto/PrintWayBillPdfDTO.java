package com.common.business.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 填充html生成pfd面单
 */
@Data
@Accessors(chain = true)
public class PrintWayBillPdfDTO {
    /**
     * 销售单号
     */
    private String soCode;
    /**
     * 打印时间
     */
    private LocalDateTime printTime;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 买家id
     */
    private String customerId;
    /**
     * 订单金额
     */
    private BigDecimal amount;
    /**
     * 实重
     */
    private BigDecimal weight;
    /**
     * 渠道名称
     */
    private String channelName;
    /**
     * 运单号
     */
    private String transportNo;
    /**
     * 备注
     */
    private String remark;
    /**
     * 明细信息
     */
    private List<PrintWayBillPdfDetailDTO> detailList;



}
