package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *收款单消费DTO
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class PlatformB2bOrderDTO {

    /**
     * 单据编码
     */
    private String code;

    /**
     * 单据日期
     */
    private LocalDate billDate;

    /**
     * 收货地址
     */
    private String receiveAddress;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 第三方仓库id
     */
    private String platformWarehouseId;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 客户编号
     */
    private String customerCode;

    /**
     * 折扣总额
     */
    private BigDecimal discountAmount;

    /**
     * 平台更新时间
     */
    private BigDecimal platformUpdateTime;

    /**
     * 平台创建时间
     */
    private LocalDateTime platformCreateTime;

    /**
     * 账户抵扣金额
     */
    private BigDecimal accountDeductAmount;
    /**
     * 返利抵扣金额
     */
    private BigDecimal rebateDeductAmount;
    /**
     * 授信抵扣金额
     */
    private BigDecimal creditDeductAmount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 平台订单id
     */
    private String platformId;
    /**
     * 第三方系统
     */
    private String thirdSystem;

    /**
     * 附件列表
     */
    private List<AttachDTO> attachmentList;

    private List<PlatformB2bOrderDetailDTO> detail;
}
