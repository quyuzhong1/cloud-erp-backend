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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 多渠道订单请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-05-30
*/
@Data
@NoArgsConstructor
public class SoMultiChannelDTO implements Serializable {


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
        * 单据编码
        */
        private String code;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 平台订单号
        */
        private String platformCode;

        /**
        * 销售平台
        */
        private String dictPlatform;

        /**
        * 店铺
        */
        private String shopId;

        /**
        * (手动)作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 订单状态
        */
        private String billStatus;

        /**
        * 付款状态（待付款、已付款）
        */
        private String payStatus;

        /**
        * 订单金额
        */
        private BigDecimal amount;

        /**
        * 币别（原币）
        */
        private String currency;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 运费收入
        */
        private BigDecimal shippingFee;

        /**
        * 付款时间
        */
        private LocalDateTime payTime;

        /**
        * 付款金额
        */
        private BigDecimal payAmount;

        /**
        * 付款方式
        */
        private String dictPayMethod;

        /**
        * 买家备注
        */
        private String buyerRemark;

        /**
        * 订单备注
        */
        private String remark;

        /**
        * 销售组织id
        */
        private String orgId;

        /**
        * 销售组织名称
        */
        private String orgName;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 标签json
        */
        private String labelJson;

        /**
        * 订单日期
        */
        private LocalDate billDate;

        /**
        * 作废类型（manual手动作废，automatic自动作废）
        */
        private String invalidType;

        /**
        * 平台创建时间
        */
        private LocalDateTime platformOrderCreateTime;

        /**
        * 第三方仓发货订单id
        */
        private String shippingOrderNo;

        /**
        * 是否冻结
        */
        private Boolean isFrozen;

        /**
        * 扩展的 值 当后续有需要扩展的类型的字段值存里面
        */
        private String extendData;

        /**
        * 平台订单状态
        */
        private String platformOrderStatus;

        /**
        * 平台是否取消
        */
        private Boolean isCancel;

        /**
        * 卖家订单编号
        */
        private String sellerOrderCode;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人姓名
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
        * 单据编码
        */
        private String code;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 平台订单号
        */
        private String platformCode;

        /**
        * 销售平台
        */
        private String dictPlatform;

        /**
        * 店铺
        */
        private String shopId;

        /**
        * (手动)作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 订单状态
        */
        private String billStatus;

        /**
        * 付款状态（待付款、已付款）
        */
        private String payStatus;

        /**
        * 订单金额
        */
        private BigDecimal amount;

        /**
        * 币别（原币）
        */
        private String currency;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 运费收入
        */
        private BigDecimal shippingFee;

        /**
        * 付款时间
        */
        private LocalDateTime payTime;

        /**
        * 付款金额
        */
        private BigDecimal payAmount;

        /**
        * 付款方式
        */
        private String dictPayMethod;

        /**
        * 买家备注
        */
        private String buyerRemark;

        /**
        * 订单备注
        */
        private String remark;

        /**
        * 销售组织id
        */
        private String orgId;

        /**
        * 销售组织名称
        */
        private String orgName;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 标签json
        */
        private String labelJson;

        /**
        * 订单日期
        */
        private LocalDate billDate;

        /**
        * 作废类型（manual手动作废，automatic自动作废）
        */
        private String invalidType;

        /**
        * 平台创建时间
        */
        private LocalDateTime platformOrderCreateTime;

        /**
        * 第三方仓发货订单id
        */
        private String shippingOrderNo;

        /**
        * 是否冻结
        */
        private Boolean isFrozen;

        /**
        * 扩展的 值 当后续有需要扩展的类型的字段值存里面
        */
        private String extendData;

        /**
        * 平台订单状态
        */
        private String platformOrderStatus;

        /**
        * 平台是否取消
        */
        private Boolean isCancel;

        /**
        * 卖家订单编号
        */
        private String sellerOrderCode;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人姓名
        */
        private String approveUserName;


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
        * 平台订单号
        */
        @NotBlank(message = "平台订单号不能为空")
        @Size(max = 100,message = "平台订单号最大长度不能超过100位")
        private String platformCode;

        /**
        * 销售平台
        */
        @NotBlank(message = "销售平台不能为空")
        @Size(max = 32,message = "销售平台最大长度不能超过32位")
        private String dictPlatform;

        /**
        * 店铺
        */
        @NotBlank(message = "店铺不能为空")
        @Size(max = 19,message = "店铺最大长度不能超过19位")
        private String shopId;

        /**
        * 订单状态
        */
        @NotBlank(message = "订单状态不能为空")
        @Size(max = 32,message = "订单状态最大长度不能超过32位")
        private String billStatus;

        /**
        * 付款状态（待付款、已付款）
        */
        @NotBlank(message = "付款状态（待付款、已付款）不能为空")
        @Size(max = 32,message = "付款状态（待付款、已付款）最大长度不能超过32位")
        private String payStatus;

        /**
        * 订单金额
        */
        @NotNull(message = "订单金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "订单金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal amount;

        /**
        * 币别（原币）
        */
        @NotBlank(message = "币别（原币）不能为空")
        @Size(max = 32,message = "币别（原币）最大长度不能超过32位")
        private String currency;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 10, fraction = 6, message = "汇率整数位不能超过10位，小数位不能超过6位")
        private BigDecimal exchangeRate;

        /**
        * 运费收入
        */
        @NotNull(message = "运费收入不能为空")
        @Digits(integer = 12, fraction = 4, message = "运费收入整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingFee;

        /**
        * 付款时间
        */
        private LocalDateTime payTime;

        /**
        * 付款金额
        */
        @NotNull(message = "付款金额不能为空")
        @Digits(integer = 12, fraction = 4, message = "付款金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal payAmount;

        /**
        * 付款方式
        */
        @NotBlank(message = "付款方式不能为空")
        @Size(max = 200,message = "付款方式最大长度不能超过200位")
        private String dictPayMethod;

        /**
        * 买家备注
        */
        @NotBlank(message = "买家备注不能为空")
        @Size(max = 255,message = "买家备注最大长度不能超过255位")
        private String buyerRemark;

        /**
        * 订单备注
        */
        @NotBlank(message = "订单备注不能为空")
        @Size(max = 255,message = "订单备注最大长度不能超过255位")
        private String remark;

        /**
        * 销售组织id
        */
        @NotBlank(message = "销售组织id不能为空")
        @Size(max = 19,message = "销售组织id最大长度不能超过19位")
        private String orgId;

        /**
        * 销售组织名称
        */
        @NotBlank(message = "销售组织名称不能为空")
        @Size(max = 100,message = "销售组织名称最大长度不能超过100位")
        private String orgName;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 500,message = "来源id最大长度不能超过500位")
        private String sourceId;

        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 标签json
        */
        @NotBlank(message = "标签json不能为空")
        private String labelJson;

        /**
        * 订单日期
        */
        private LocalDate billDate;

        /**
        * 作废类型（manual手动作废，automatic自动作废）
        */
        @NotBlank(message = "作废类型（manual手动作废，automatic自动作废）不能为空")
        @Size(max = 10,message = "作废类型（manual手动作废，automatic自动作废）最大长度不能超过10位")
        private String invalidType;

        /**
        * 平台创建时间
        */
        @NotNull(message = "平台创建时间不能为空")
        private LocalDateTime platformOrderCreateTime;

        /**
        * 第三方仓发货订单id
        */
        @NotBlank(message = "第三方仓发货订单id不能为空")
        @Size(max = 50,message = "第三方仓发货订单id最大长度不能超过50位")
        private String shippingOrderNo;

        /**
        * 是否冻结
        */
        @NotNull(message = "是否冻结不能为空")
        private Boolean isFrozen;

        /**
        * 扩展的 值 当后续有需要扩展的类型的字段值存里面
        */
        @NotBlank(message = "扩展的 值 当后续有需要扩展的类型的字段值存里面不能为空")
        private String extendData;

        /**
        * 平台订单状态
        */
        @NotBlank(message = "平台订单状态不能为空")
        @Size(max = 64,message = "平台订单状态最大长度不能超过64位")
        private String platformOrderStatus;

        /**
        * 平台是否取消
        */
        @NotNull(message = "平台是否取消不能为空")
        private Boolean isCancel;

        /**
        * 卖家订单编号
        */
        @NotBlank(message = "卖家订单编号不能为空")
        @Size(max = 255,message = "卖家订单编号最大长度不能超过255位")
        private String sellerOrderCode;


    }


}