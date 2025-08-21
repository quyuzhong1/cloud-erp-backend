package com.erp.model.oms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
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
 * @since 2025-08-20
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
        private String  id;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;

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
        * 销售组织id
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

        /**
        * 仓库组织id
        */
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
        private String receiveAddress;

        /**
        * 交货方式
        */
        private String deliveryMode;

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

        /**
        * 销售组织名
        */
        private String salesOrgName;

        /**
        * 仓库组织名称
        */
        private String warehouseOrgName;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;

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

        /**
        * 收款账号
        */
        private String receiveAccount;

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
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;
    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
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
        * 销售组织id
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

        /**
        * 仓库组织id
        */
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
        private String receiveAddress;

        /**
        * 交货方式
        */
        private String deliveryMode;

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

        /**
        * 销售组织名
        */
        private String salesOrgName;

        /**
        * 仓库组织名称
        */
        private String warehouseOrgName;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;

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

        /**
        * 收款账号
        */
        private String receiveAccount;

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


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


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
        @NotBlank(message = "销售组织id不能为空")
        @Size(max = 19,message = "销售组织id最大长度不能超过19位")
        private String salesOrgId;

        /**
        * 销售部门id
        */
        @NotBlank(message = "销售部门id不能为空")
        @Size(max = 19,message = "销售部门id最大长度不能超过19位")
        private String salesDeptId;

        /**
        * 销售员id
        */
        @NotBlank(message = "销售员id不能为空")
        @Size(max = 19,message = "销售员id最大长度不能超过19位")
        private String sellerId;

        /**
        * 销售员
        */
        @NotBlank(message = "销售员不能为空")
        @Size(max = 50,message = "销售员最大长度不能超过50位")
        private String sellerName;

        /**
        * 领用人id
        */
        @NotBlank(message = "领用人id不能为空")
        @Size(max = 19,message = "领用人id最大长度不能超过19位")
        private String recipientUserId;

        /**
        * 领用人
        */
        @NotBlank(message = "领用人不能为空")
        @Size(max = 50,message = "领用人最大长度不能超过50位")
        private String recipientUserName;

        /**
        * 是否收取运费
        */
        @NotNull(message = "是否收取运费不能为空")
        private Boolean isCollectShippingFee;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 仓库组织id
        */
        @NotBlank(message = "仓库组织id不能为空")
        @Size(max = 19,message = "仓库组织id最大长度不能超过19位")
        private String warehouseOrgId;

        /**
        * 银行手续费
        */
        @NotNull(message = "银行手续费不能为空")
        @Digits(integer = 12, fraction = 4, message = "银行手续费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal bankServiceFee;

        /**
        * 运费
        */
        @NotNull(message = "运费不能为空")
        @Digits(integer = 12, fraction = 4, message = "运费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingFee;

        /**
        * 客户id
        */
        @NotBlank(message = "客户id不能为空")
        @Size(max = 19,message = "客户id最大长度不能超过19位")
        private String customerId;

        /**
        * 收货人
        */
        @NotBlank(message = "收货人不能为空")
        @Size(max = 50,message = "收货人最大长度不能超过50位")
        private String receiverName;

        /**
        * 联系人电话
        */
        @NotBlank(message = "联系人电话不能为空")
        @Size(max = 50,message = "联系人电话最大长度不能超过50位")
        private String telNumber;

        /**
        * 收货地址
        */
        @NotBlank(message = "收货地址不能为空")
        @Size(max = 500,message = "收货地址最大长度不能超过500位")
        private String receiveAddress;

        /**
        * 交货方式
        */
        @NotBlank(message = "交货方式不能为空")
        @Size(max = 30,message = "交货方式最大长度不能超过30位")
        private String deliveryMode;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        @Size(max = 30,message = "币种最大长度不能超过30位")
        private String currency;

        /**
        * 币种符号
        */
        @NotBlank(message = "币种符号不能为空")
        @Size(max = 10,message = "币种符号最大长度不能超过10位")
        private String currencySymbol;

        /**
        * 是否含税
        */
        @NotNull(message = "是否含税不能为空")
        private Boolean isTax;

        /**
        * 地址类型
        */
        @NotBlank(message = "地址类型不能为空")
        @Size(max = 30,message = "地址类型最大长度不能超过30位")
        private String addressType;

        /**
        * 销售组织名
        */
        @NotBlank(message = "销售组织名不能为空")
        @Size(max = 50,message = "销售组织名最大长度不能超过50位")
        private String salesOrgName;

        /**
        * 仓库组织名称
        */
        @NotBlank(message = "仓库组织名称不能为空")
        @Size(max = 50,message = "仓库组织名称最大长度不能超过50位")
        private String warehouseOrgName;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 20,message = "来源id最大长度不能超过20位")
        private String sourceId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 30,message = "来源类型最大长度不能超过30位")
        private String sourceType;

        /**
        * 收货地址id
        */
        @NotBlank(message = "收货地址id不能为空")
        @Size(max = 19,message = "收货地址id最大长度不能超过19位")
        private String receiveAddressId;

        /**
        * 收款条件
        */
        @NotBlank(message = "收款条件不能为空")
        @Size(max = 50,message = "收款条件最大长度不能超过50位")
        private String receiveCondition;

        /**
        * 收款账号
        */
        @NotBlank(message = "收款账号不能为空")
        @Size(max = 50,message = "收款账号最大长度不能超过50位")
        private String receiveAccount;

        /**
        * 收款金额
        */
        @NotNull(message = "收款金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "收款金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal receiveAmount;

        /**
        * 收款日期
        */
        private LocalDate receiveDate;

        /**
        * 收款方式
        */
        @NotBlank(message = "收款方式不能为空")
        @Size(max = 50,message = "收款方式最大长度不能超过50位")
        private String receiveMethod;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 单据日期
        */
        private LocalDate billDate;

        /**
        * 贸易条款
        */
        @NotBlank(message = "贸易条款不能为空")
        @Size(max = 255,message = "贸易条款最大长度不能超过255位")
        private String tradeTerm;

        /**
        * 折扣总额
        */
        private BigDecimal discountAmount;

        /**
        * 总价税合计本位币
        */
        @NotNull(message = "总价税合计本位币不能为空")
        @Digits(integer = 12, fraction = 4, message = "总价税合计本位币整数位不能超过12位，小数位不能超过4位")
        private BigDecimal allAmountLc;

        /**
        * 收货国家id
        */
        @NotBlank(message = "收货国家id不能为空")
        @Size(max = 10,message = "收货国家id最大长度不能超过10位")
        private String countryId;

        /**
        * 收货国家
        */
        @NotBlank(message = "收货国家不能为空")
        @Size(max = 255,message = "收货国家最大长度不能超过255位")
        private String countryName;


    }


}