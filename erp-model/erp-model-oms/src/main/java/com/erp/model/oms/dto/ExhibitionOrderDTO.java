package com.erp.model.oms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import java.util.List;

import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 展会订单信息请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-08-29
*/
@Data
@NoArgsConstructor
public class ExhibitionOrderDTO implements Serializable {


     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
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
     * 分页列表查询参数
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

     }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * code
         */
        private String code;
        /**
         * 展会主题
         */
        private String exhibitionTitle;
        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 客户id
         */
        private String customerId;
        private String customerName;

        /**
         * 收货国家id
         */
        private String countryId;

        /**
         * 收货国家
         */
        private String countryName;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 分区id
         */
        private String partitionId;
        /**
         * 军区编码
         */
        private String partitionCode;
        private String partitionName;



        /**
         * 销售组织id
         */
        private String salesOrgId;

        /**
         * 销售组织名
         */
        private String salesOrgName;

        /**
         * 销售部门id
         */
        private String salesDeptId;
        private String salesDeptName;

        /**
         * 销售员id
         */
        private String sellerId;

        /**
         * 销售员
         */
        private String sellerName;

        /**
         * 仓库id
         */
        private String warehouseId;

        private String warehouseName;


        /**
         * 明细id
         */
        private String  detailId;

        /**
         * sku id
         */
        private String skuId;


        /**
         * sku no
         */
        private String skuNo;
        private String productName;

        /**
         * SPU ID
         */
        private String spuId;

        /**
         * SPU编号
         */
        private String spuNo;

        /**
         * SPU名称
         */
        private String spuName;

        /**
         * 销售数量
         */
        private Integer qty;

        /**
         * 单位
         */
        private String unit;

        /**
         * 销售金额
         */
        private BigDecimal amount;

        /**
         * 销售单价
         */
        private BigDecimal price;

        /**
         * 销售单价（本位币
         */
        private BigDecimal priceLc;
        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 含税单价(本位币)
         */
        private BigDecimal taxPriceLc;
        /**
         * 价税合计
         */
        private BigDecimal taxAmount;
        /**
         * 汇率
         */
        private BigDecimal exchangeRate;

        /**
         * 销售金额（本位币）
         */
        private BigDecimal amountLocalCurrency;

        /**
         * 价税合计（本位币）
         */
        private BigDecimal allAmountLocalCurrency;
        /**
         * 总价税合计（本位币）
         */
        private BigDecimal allAmountLc;
        /**
         * 收款金额
         */
        private BigDecimal receiveAmount;

        /**
         * 备注
         */
        private String remark;

        /**
         * 明细备注
         */
        private String detailRemark;

        /**
         * 审批完成时间
         */
        private LocalDateTime approveTime;

        /**
         * 审批人ID
         */
        private String approveUserId;

        /**
         * 审批人姓名
         */
        private String approveUserName;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;
        /**
         * 收款方式
         */
        private String receiveMethod;
        private String receiveMethodName;
        /**
         * 收款条件
         */
        private String receiveCondition;
        private String receiveConditionName;
        /**
         * 收款账号
         */
        private String receiveAccount;
        private String receiveAccountName;
        /**
         * 地址类型
         */
        private String addressType;
        private String addressTypeName;
        /**
         * 交货方式
         */
        private String deliveryMode;

        /**
         * 交货方式
         */
        private String deliveryModeName;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 主表id
         */
        private String mainId;
        /**
         * 主表id
         */
        private String sourceDetailId;

    }


    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;
        private String invalidStatusName;

        /**
        * code
        */
        private String code;

        /**
        * 展会主题
        */
        private String exhibitionTitle;

        /**
        * 审核状态
        */
        private String approveStatus;
        private String approveStatusName;

        /**
        * 销售组织id
        */
        private String salesOrgId;
        private String salesOrgName;

        /**
        * 销售部门id
        */
        private String salesDeptId;
        private String salesDeptName;

        /**
        * 销售员id
        */
        private String sellerId;

        /**
        * 销售员
        */
        private String sellerName;

        /**
        * 领用人id
        */
        private String recipientUserId;

        /**
        * 领用人
        */
        private String recipientUserName;

        /**
        * 是否收取运费
        */
        private Boolean isCollectShippingFee;

        /**
        * 仓库id
        */
        private String warehouseId;
        private String warehouseName;

        /**
        * 仓库组织id
        */
        private String warehouseOrgId;
        private String warehouseOrgName;

        /**
        * 银行手续费
        */
        private BigDecimal bankServiceFee;

        /**
        * 运费
        */
        private BigDecimal shippingFee;

        /**
        * 客户id
        */
        private String customerId;
        private String customerName;

        /**
        * 收货人
        */
        private String receiverName;

        /**
        * 联系人电话
        */
        private String telNumber;

        /**
        * 收货地址
        */
        private String receiveAddress;

        /**
        * 交货方式
        */
        private String deliveryMode;
        private String deliveryModeName;

        /**
        * 币种
        */
        private String currency;

        /**
        * 币种符号
        */
        private String currencySymbol;

        /**
        * 是否含税
        */
        private Boolean isTax;

        /**
        * 地址类型
        */
        private String addressType;
        private String addressTypeName;

        /**
        * 收货地址id
        */
        private String receiveAddressId;

        /**
        * 审核人
        */
        private String approveUserName;

        /**
        * 收款条件
        */
        private String receiveCondition;
        private String receiveConditionName;

        /**
        * 收款账号
        */
        private String receiveAccount;
        private String receiveAccountName;

        /**
        * 收款金额
        */
        private BigDecimal receiveAmount;

        /**
        * 收款日期
        */
        private LocalDate receiveDate;

        /**
        * 收款方式
        */
        private String receiveMethod;
        private String receiveMethodName;

        /**
        * 备注
        */
        private String remark;

        /**
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * 贸易条款
        */
        private String tradeTerm;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 折扣总额
        */
        private BigDecimal discountAmount;

        /**
        * 总价税合计本位币
        */
        private BigDecimal allAmountLc;

        /**
        * 收货国家id
        */
        private String countryId;

        /**
        * 收货国家
        */
        private String countryName;

        /**
        * 分区id
        */
        private String partitionId;
        private String partitionCode;
        private String partitionName;

        /**
         * 附件集合
         */
        private List<String> attachmentNameList;
        private List<String> attachmentUrlList;


        /**
         * 订单产品详情
         */
        private List<ExhibitionOrderDetailDTO.ViewDTO> detailList;

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        @NotEmpty(message = "明细不能为空" )
        private List<ExhibitionOrderDetailDTO.AddDTO> detailList;


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        @NotEmpty(message = "明细不能为空" )
        private List<ExhibitionOrderDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 展会主题
        */
        @NotBlank(message = "展会主题不能为空")
        @Size(max = 200,message = "展会主题最大长度不能超过200位")
        private String exhibitionTitle;

        /**
        * 销售组织id
        */
        @NotBlank(message = "销售组织不能为空")
        private String salesOrgId;

        /**
        * 销售部门id
        */
        @NotBlank(message = "销售部门不能为空")
        private String salesDeptId;

        /**
        * 销售员id
        */
        @NotBlank(message = "销售员不能为空")
        private String sellerId;

        /**
        * 销售员
        */
        private String sellerName;

        /**
        * 领用人id
        */
        @NotBlank(message = "领用人不能为空")
        private String recipientUserId;

        /**
        * 领用人
        */
        private String recipientUserName;

        /**
        * 是否收取运费
        */
        private Boolean isCollectShippingFee;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;

        /**
        * 仓库组织id
        */
        @NotBlank(message = "仓库组织不能为空")
        private String warehouseOrgId;

        /**
        * 银行手续费
        */
        private BigDecimal bankServiceFee;

        /**
        * 运费
        */
        private BigDecimal shippingFee;

        /**
        * 客户id
        */
        @NotBlank(message = "客户不能为空")
        private String customerId;

        /**
        * 收货人
        */
        private String receiverName;

        /**
        * 联系人电话
        */
        private String telNumber;

        /**
        * 收货地址
        */
        @NotBlank(message = "收货地址不能为空")
        private String receiveAddress;

        /**
        * 交货方式
        */
        @NotBlank(message = "交货方式不能为空")
        private String deliveryMode;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
        * 币种符号
        */
        private String currencySymbol;

        /**
        * 是否含税
        */
        private Boolean isTax;

        /**
        * 地址类型
        */
        @NotBlank(message = "地址类型不能为空")
        private String addressType;

        /**
        * 销售组织名
        */
        private String salesOrgName;

        /**
        * 仓库组织名称
        */
        private String warehouseOrgName;
        /**
        * 收货地址id
        */
        @NotBlank(message = "收货地址不能为空")
        private String receiveAddressId;

        /**
        * 收款条件
        */
        @NotBlank(message = "收款条件不能为空")
        private String receiveCondition;

        /**
        * 收款账号
        */
        @NotBlank(message = "收款账号不能为空")
        private String receiveAccount;

        /**
        * 收款金额
        */
        @NotNull(message = "收款金额不能为空")
        private BigDecimal receiveAmount;

        /**
        * 收款日期
        */
        @NotNull(message = "收款日期不能为空")
        private LocalDate receiveDate;

        /**
        * 收款方式
        */
        @NotBlank(message = "收款方式不能为空")
        private String receiveMethod;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 单据日期
        */
        @NotNull(message = "单据日期不能为空")
        private LocalDate billDate;

        /**
        * 贸易条款
        */
        private String tradeTerm;

        /**
        * 折扣总额
        */
        private BigDecimal discountAmount;

        /**
        * 总价税合计本位币
        */
        private BigDecimal allAmountLc;

        /**
        * 收货国家id
        */
        @NotBlank(message = "收货国家不能为空")
        private String countryId;

        /**
        * 收货国家
        */
        private String countryName;

        /**
        * 分区id
        */
        private String partitionId;

        /**
         * 附件集合
         */
        private List<String> attachmentNameList;
        private List<String> attachmentUrlList;

    }


    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class SearchDTO {

        private String childId;

        private List<String> skuIds;
    }
    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class FreezeQtyBySku {

        private String skuId;

        private Integer freezeQty;

        /**
         * 历史最高
         */
        private BigDecimal maxPrice;

        /**
         * 历史最低
         */
        private BigDecimal minPrice;

        /**
         * 平均价格
         */
        private BigDecimal avgPrice;
    }

    /**
     *
     * 下推其他入库单和销售出库单
     *
     */
    @Data
    @NoArgsConstructor
    public static class DownstreamDTO {

        private List<SoOutstockDTO.GenerateSoOutstockViewDTO> generateSoOutstockViewDTOList;

        private OtherInstockDTO.AddDTO otherInstockAddDTO;

        private String soId;

    }


    /**
     *
     * 其他入库单和销售出库单查询字段
     *
     */
    @Data
    @NoArgsConstructor
    public static class DownstreamListDTO {

        /**
         * id
         */

        private String id;


        private String code;

        /**
         * 审批状态
         */
        private String approveStatus;
        private String approveStatusName;

        /**
         * 审批完成时间
         */
        private LocalDateTime approveTime;

        /**
         * 审批人ID
         */
        private String approveUserId;

        /**
         * 审批人姓名
         */
        private String approveUserName;

        /**
         * 是否作废
         */
        private Boolean invalidStatus;
        private String invalidStatusName;

        /**
         * 单据日期
         */
        private LocalDate billDate;


        /**
         * 明细id
         */
        private String  detailId;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 仓库id
         */
        private String warehouseId;

        private String warehouseName;
        /**
         * 数量
         */
        private Integer qty;

        /**
         * 备注
         */
        private String remark;
        /**
         * 明细备注
         */
        private String detailRemark;



    }





}