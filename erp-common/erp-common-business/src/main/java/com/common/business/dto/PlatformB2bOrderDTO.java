package com.common.business.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.validator.AddGroup;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
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
     * 是否已作废
     */
    private Boolean isInvalid;

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
    private LocalDateTime platformUpdateTime;

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

    /**
     * erp信息
     */
    private ErpInfoDTO erpInfoDTO;


    private List<PlatformB2bOrderDetailDTO> detail;

    /**
     * erp信息
     */
    @Data
    @NoArgsConstructor
    public static class ErpInfoDTO {
        /**
         * erp客户地址id
         */
        private String customerAddressId;
        /**
         * erp仓库Id
         */
        private String warehouseId;
        /**
         * erp客户id
         */
        private String customerId;
        /**
         * 组织id
         */
        private String salesOrgId;

        /**
         * 销售部门id
         */
        private String salesDeptId;

        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 收货国家id
         */
        private String countryId;
        /**
         * 收货国家
         */
        private String countryName;
        /**
         * 收货人
         */
        private String receiverName;

        /**
         * 币种
         */
        private String currency;
        /**
         * 币种符号
         */
        private String currencySymbol;
        /**
         * 收款条件
         */
        private String receiveCondition;

    }
}
