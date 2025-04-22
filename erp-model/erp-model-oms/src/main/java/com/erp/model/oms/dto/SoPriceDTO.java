package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 销售价目表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-03-24
*/
@Data
@NoArgsConstructor
public class SoPriceDTO implements Serializable {

    /**
     * 下推销售订单View
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class PushDownSoView {
        /**
         * id
         */
        @NotBlank(message = "id不能为空")
        private String id;

        private String approveStatus;
        /**
         * detailId
         */
        @NotBlank(message = "detailId不能为空")
        private String detailId;

        /**
         * 销售日期
         */
        private LocalDate soDate;
        /**
         * 销售员id
         */
        private String soUserId;
        /**
         * soId
         */
        private String soId;
        /**
         * 单号
         */
        @NotBlank(message = "单号不能为空")
        private String code;
        /**
         * 客户表id
         */
        @NotBlank(message = "客户不能为空")
        private String customerId;

        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 销售组织
         */
        @NotBlank(message = "销售组织不能为空")
        private String soOrgId;

        /**
         * 销售组织名
         */
        @NotBlank(message = "销售组织不能为空")
        private String soOrgName;
        /**
         * 交货仓库
         */
        @NotBlank(message = "交货仓库不能为空")
        private String deliveryWarehouseId;
        /**
         * 交货仓库名称
         */
        @NotBlank(message = "交货仓库不能为空")
        private String deliveryWarehouseName;
        /**
         * 收料组织
         */
        @NotBlank(message = "收料组织不能为空")
        private String receiveOrgId;
        /**
         * 收料组织名称
         */
        @NotBlank(message = "收料组织不能为空")
        private String receiveOrgName;

        /**
         * sku
         */
        @NotBlank(message = "sku不能为空")
        private String skuId;

        /**
         * 扣款数量
         */
        private Integer deductAmountQty;
        /**
         * 币别
         */
        private String currency;

        private String warehouseLocation;
        /**
         * 币别符号
         */
        private String currencySymbol;
        /**
         * skuNo
         */
        @NotBlank(message = "skuNo不能为空")
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;
        /**
         * 含税单价
         */
        @NotNull(message = "含税单价不能为空")
        private BigDecimal taxPrice;
        /**
         * 税率
         */
        @NotNull(message = "税率不能为空")
        private BigDecimal taxRate;

        /**
         * 销售数量
         */
        @NotNull(message = "销售数量不能为空")
        @Min(value = 0,message = "销售数量最小值为0")
        @Max(value = 999999999,message = "销售数量最大值为999999999")
        private Integer soQty;

        private Integer replenishQty;

        private Integer oldSoQty;

        /**
         * 价税合计
         */
        @NotNull(message = "价税合计不能为空")
        private BigDecimal totalTaxAmount;

        /**
         * 预计交货日期
         */
        @NotNull(message = "预计交货日期不能为空")
        private LocalDate planDeliveryDate;
        /**
         * 是否赠品：true/false
         */
        @NotNull(message = "是否赠品不能为空")
        private Boolean isGift = false;
        /**
         * 是否加急（false否，true是）
         */
        @NotNull(message = "是否加急不能为空")
        private Boolean isUrgent = false;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO  extends PermissionsDTO {
        /**
         * 客户表id
         */
        @NotBlank(message = "客户不能为空")
        private String customerId;

        /**
         * 报价日期
         */
        @NotNull(message = "报价日期不能为空")
        private LocalDate quotedDate;

        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
         * 报价人id
         */
        private String pricingUserId;


        /**
         * 销售组织id
         */
        @NotBlank(message = "销售组织不能为空")
        private String soOrgId;

        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 备注
         */
        private String remark;

        /**
         * 报价明细
         */
        @Valid
        @NotEmpty(message = "销售价目表明细不能为空")
        private List<SoPriceDetailDTO.AddDTO> soPriceDetailList;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 表id
         */
        private String id;

        /**
         * code
         */
        private String code;
        /**
         * 客户表id
         */
        @NotBlank(message = "客户不能为空")
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 报价日期
         */
        @NotNull(message = "报价日期不能为空")
        private LocalDate quotedDate;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 报价人id
         */
        private String pricingUserId;

        /**
         * 销售组织id
         */
        private String soOrgId;

        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名称
         */
        private List<String> attachmentNameList;

        /**
         * 结算币种
         */
        private String currency;

        /**
         * 备注
         */
        private String remark;

        /**
         * 报价明细
         */
        @Valid
        private List<SoPriceDetailDTO.ViewDTO> soPriceDetailList;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO   extends PermissionsDTO{

        /**
         * 表id
         */
        @NotBlank(message = "销售价目表id不能为空")
        private String id;

        /**
         * code
         */
        private String code;
        /**
         * 客户表id
         */
        @NotBlank(message = "客户不能为空")
        private String customerId;

        /**
         * 报价日期
         */
        @NotNull(message = "报价日期不能为空")
        private LocalDate quotedDate;

        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;


        /**
         * 销售组织id
         */
        @NotBlank(message = "销售组织不能为空")
        private String soOrgId;

        /**
         * 报价人id
         */
        private String pricingUserId;


        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名称
         */
        private List<String> attachmentNameList;


        /**
         * 报价明细
         */
        @Valid
        private List<SoPriceDetailDTO.UpdateDTO> soPriceDetailList;

    }


    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * 表id
         * 对应
         * SoPriceId
         */
        private String id;


        /**
         * 销售价目详情id
         */
        private String soPriceDetailId;


        /**
         * code，可排序
         */
        private String code;

        /**
         * 客户id，可排序
         */
        private String customerId;

        /**
         * 客户名
         */
        private String customerName;

        /**
         * sku id，可排序
         */
        private String skuId;


        /**
         * sku no，可排序
         */
        private String skuNo;


        /**
         * 产品名称
         */
        private String productName;

        /**
         * 最小数量，可排序
         */
        private Integer minQty;


        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据状态code
         */
        private String approveStatusCode;

        /**
         * 单据状态名
         */
        private String approveStatusName;

        /**
         * 最大数量，可排序
         */
        private Integer maxQty;

        /**
         * 币种，可排序
         */
        private String currency;

        /**
         * 币种 符号
         */
        private String currencySymbol;

        /**
         * 含税单价，可排序
         */
        private BigDecimal taxPrice;

        /**
         * 税率，可排序
         */
        private BigDecimal taxRate;

        /**
         * 生效时间，可排序
         */
        private LocalDate effectiveDate;

        /**
         * 失效时间，可排序
         */
        private LocalDate expireDate;

        /**
         * 销售组织，可排序
         */
        private String soOrgId;

        /**
         * 销售组织名
         */
        private String soOrgName;

        /**
         * 最新审核人名称
         */
        private String approveUserName;

        /**
         * 审核完成时间，可排序
         */
        private LocalDateTime approveTime;

        /**
         * 创建人名称，可排序
         */
        private String createUserName;

        /**
         * 创建时间，可排序
         */
        private LocalDateTime createTime;

        /**
         *  禁用启用状态
         *  true 禁用
         *  fase 启用
         */
        private Boolean disabled;

        /**
         * 明细备注
         */
        private String detailRemark;
    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
        /**
         * 审核状态集合
         */
        private List<String> approveStatusList;
        /**
         * 客户id集合
         */
        private List<String> customerIdList;
        /**
         * 销售组织id集合
         */
        private List<String> soOrgIdList;
        /**
         * 价目表编码
         */
        private String code;
        /**
         * sku
         */
        private List<String> skuNoList;
    }


    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型 ,(approveIng待我审核,reject不通过,approveEnable已审核启用,approveDisabled已审核停用)
         */
        private String tabFlag;

        /**
         * 类型
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }


    /**
     * 导出销售价目
     */
    @Data
    @NoArgsConstructor
    public static class CustomerSkuPrice {
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 销售组织Id
         */
        private String soOrgId;
        /**
         * 销售组织名称
         */
        private String soOrgName;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 含税单价
         */
        private BigDecimal taxPrice;
        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 最小数量
         */
        private Integer minQty;

        /**
         * 最大数量
         */
        private Integer maxQty;

        /**
         * 币制
         */
        private String currency;

        /**
         * 币制符号
         */
        private String currencySymbol;
    }




    /**
     * 导入DTO
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class ImportAddDTO {

        /**
         * 销售价目主表id（更新时使用）
         */
        private String id;

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 客户名称
         */
        @NotBlank(message = "客户名称不能为空")
        @Size(max = 50, message = "客户名称最大50字符")
        private String customerName;

        /**
         * 报价日期
         */
        private LocalDate quotedDate;

        /**
         * 销售组织id
         */
        private String soOrgId;

        /**
         * 销售组织
         */
        @Size(max = 50, message = "销售组织名称最大50字符")
        private String soOrgName;

        /**
         * 定价员id
         */
        private String pricingUserId;

        /**
         * 定价员
         */
        @Size(max = 50, message = "定价员名称最大50字符")
        private String pricingUserName;

        /**
         * 币种
         */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
         * 明细信息
         */
        @Valid
        private List<SoPriceDetailDTO.ImportSaveDTO> detailList;



    }

    @Data
    @NoArgsConstructor
    public static class OutPlatformCodeDTO extends BaseIdsDTO.IdsDTO {

        @NotBlank(message = "外部平台单号")
        @Size(max = 255,message = "填写信息不能超过255字符")
        private String voucherNo;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceParamDTO {
        /**
         * SKU
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 组织id
         */
        private String soOrgId;
        /**
         * 店铺
         */
        private String shopId;
        /**
         * 数量
         */
        private Integer qty;
        /**
         * 时间
         */
        @NotNull(message = "时间不能为空")
        private LocalDate date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceDTO {
        /**
         * 销售组织
         */
        private String soOrgId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 销售数量
         */
        private Integer qty;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 价税合计
         */
        private String amount;
        /**
         * 币种
         */
        private String currency;
        /**
         * 币种符号
         */
        private String currencySymbol;


        public PriceDTO(Integer soQty, String skuId, String customerId, String soOrgId) {
            this.qty = soQty;
            this.skuId = skuId;
            this.customerId = customerId;
            this.soOrgId = soOrgId;
        }
    }


}