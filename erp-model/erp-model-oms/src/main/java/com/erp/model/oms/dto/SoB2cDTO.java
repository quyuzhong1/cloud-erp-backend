package com.erp.model.oms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * B2C销售订单表请求响应实体
 * </p>
 *
 * @author Will
 * @since 2023-08-18
*/
@Data
@NoArgsConstructor
public class SoB2cDTO implements Serializable {


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
         * 搜索类型
         */
         private String  tabFlag;
         /**
          * 销售单号
          */
         private String code;
         /**
          * 平台订单号
          */
         private String platformCode;
         /**
          * 平台集合
          */
         private List<String> platformList;
         /**
          * 店铺id集合
          */
         private List<String> shopIdList;
         /**
          * 国家集合
          */
         private List<String> countryList;
         /**
          * 平台sku
          */
         private String platformSkuNo;
         /**
          * 卖家sku
          */
         private String sellerSkuNo;
         /**
          * 订单状态
          */
         private List<String> billStatusList;
         /**
          * 付款状态
          */
         private List<String> payStatusList;
         /**
          * 分类集合
          */
         private List<String> categoryList;
         /**
          * 标签集合
          */
         private List<String> labelList;
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
         * 销售平台
         */
        private String dictPlatform;

        /**
         * 平台订单号
         */
        private String platformCode;

        /**
         * 店铺
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;


        /**
        * 审核状态
        */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * 国家id
         */
        private String countryId;

        /**
         * 国家
         */
        private String countryName;

        /**
         * 物流方式
         */
        private String dictLogisticsMethod;

        /**
         * 物流方式名称
         */
        private String dictLogisticsMethodName;

        /**
         * 实际运费(优先实际、没有取预估)
         */
        private BigDecimal shippingCost;

        /**
         * 实际运费币别
         */
        private String shippingCostCurrency;

        /**
         * 总利润
         */
        private BigDecimal totalProfit;

        /**
         * 利润币别（列表默认人民币）
         */
        private String profitCurrency;

        /**
         * 利润率
         */
        private BigDecimal profitRate;


        /**
        * 订单状态（审核状态、订单状态，取最后一级状态）
        */
        private String status;

        /**
         * 订单状态名称
         */
        private String statusName;

        /**
         * 买家名称
         */
        private String buyerName;

        /**
         * 物流单号
         */
        private String logisticsCode;

        /**
         * 订单金额
         */
        private BigDecimal amount;

        /**
        * 币别（原币）
        */
        private String currency;

        /**
         * 付款时间
         */
        private LocalDateTime payTime;

        /**
        * 买家备注
        */
        private String buyerRemark;

        /**
        * 订单备注
        */
        private String remark;

        /**
        * 异常原因（1、订单规则审核不通过；2、配货规则匹配失败；3、人工审核不通过）
        */
        private String abnormalType;

        /**
         * 异常原因名称
         */
        private String abnormalTypeName;

        /**
         * 标签
         */
        private LabelDTO labelDTO;

        /**
         * b2c销售订单明细信息
         */
        private List<SoB2cDetailDTO.ListDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class LabelDTO {

        /**
         * 速卖通风控
         */
         private String aliexpressRisk;
        /**
         * 组合产品（映射SKU为组合产品）
         */
         private Boolean isCombination;
        /**
         * FBA（亚马逊订单FulfillmentChannel=AFN-亚马逊配送时）
         */
         private String FulfillmentChannel;
        /**
         * 手工订单（在ERP手动创建的订单）
         */
         private String isManual;
        /**
         * 拦截订单（ERP发货拦截中，拦截成功，拦截失败的订单）
         */
         private Boolean isIntercept;
        /**
         * 1、拆分生成的子订单
         * 2、合并生成的新订单
         */
        private String refType;
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
    public static class ViewDTO extends CommonDTO{

        /**
        * 主键id
        */
        private String  id;

        /**
         * 物流信息
         */
        private SoB2cLogisticsDTO.ViewDTO logisticsDTO;
        /**
         * 买家信息
         */
        @Valid
        private SoB2cReceiverDTO.ViewDTO receiverDTO;

        /**
         * 明细信息
         */
        @NotNull(message = "明细信息不能为空")
        @Valid
        private List<SoB2cDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 物流信息
         */
        private SoB2cLogisticsDTO.AddDTO logisticsDTO;
        /**
         * 买家信息
         */
        @NotNull(message = "买家信息不能为空")
        @Valid
        private SoB2cReceiverDTO.AddDTO receiverDTO;

        /**
         * 明细信息
         */
        @NotNull(message = "明细信息不能为空")
        @Valid
        private List<SoB2cDetailDTO.AddDTO> detailList;
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

        /**
         * 物流信息
         */
        private SoB2cLogisticsDTO.UpdateDTO logisticsDTO;
        /**
         * 买家信息
         */
        @NotNull(message = "买家信息不能为空")
        @Valid
        private SoB2cReceiverDTO.UpdateDTO receiverDTO;

        /**
         * 明细信息
         */
        @NotNull(message = "明细信息不能为空")
        @Valid
        private List<SoB2cDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 平台订单号
        */
        @Size(max = 32,message = "平台订单号最大长度不能超过32位")
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
        * 付款时间
        */
        @NotNull(message = "付款时间不能为空")
        private LocalDateTime payTime;

        /**
        * 付款方式
        */
        @Size(max = 32,message = "付款方式最大长度不能超过32位")
        private String dictPayMethod;

        /**
        * 买家备注
        */
        @Size(max = 255,message = "买家备注最大长度不能超过255位")
        private String buyerRemark;

        /**
        * 订单备注
        */
        @Size(max = 255,message = "订单备注最大长度不能超过255位")
        private String remark;

        /**
         * 订单分类
         */
        private List<String> categoryIdList;
    }


}