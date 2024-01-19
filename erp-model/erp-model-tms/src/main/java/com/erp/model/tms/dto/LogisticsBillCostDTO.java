package com.erp.model.tms.dto;

import java.math.BigDecimal;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 自发货费用请求响应实体
 * </p>
 *
 * @author Will
 * @since 2023-11-06
*/
@Data
@NoArgsConstructor
public class LogisticsBillCostDTO implements Serializable {

    /**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 物流单号
         */
        private List<String> codeList;
        /**
         * 对账状态 reconciliationStatus字典
         */
        private List<String> reconciliationStatusList;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 订单类型
         */
        private String orderType;
        /**
         * 店铺
         */
        private List<String> shopIdList;
        /**
         * 销售订单编码
         */
        private List<String> sourceCodeList;
        /**
         * 物流渠道
         */
        private List<String> channelIdList;
        /**
         * 运输状态
         */
        private List<String> transportStatusList;
        /**
         * 备注
         */
        private String remark;
        /**
         * 差异选项
         */
        private List<String>  diffOptionList;
        /**
         * 发货时间集合
         */
        private List<LocalDate> deliveryTimeList;
        /**
         * 下单时间集合
         */
        private List<LocalDate> orderTimeList;
    }

    /**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class ExportExcelParamDTO extends PagingParamDTO {

        /**
         * 主键ids
         */
        private List<String> ids;

    }


    /**
     * 列表数据
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
       private String id;
        /**
         * 来源类型名称
         */
        private String    sourceTypeName;

        /**
         * 订单类型
         */
        private String    orderType;

        /**
         * 订单类型名称
         */
        private String    orderTypeName;

        /**
         * 对账状态
         */
        private String    reconciliationStatus;

        /**
         * 对账状态
         */
        private String   reconciliationStatusName;

        /**
         * 渠道名称
         */
        private String  channelName;

        /**
         * 物流单号
         */
        private String  transportNo;

        /**
         * 运输状态
         */
        private String  transportStatus;

        /**
         * 运输状态
         */
        private String  transportStatusName;

        /**
         * 实重
         */
        private BigDecimal   actualWeight;

        /**
         * 体积重
         */
        private BigDecimal  volumeWeight;

       /**
         * 计费重
         */
        private BigDecimal  billingWeight;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
         * 预估运费
         */
        private BigDecimal estimatedShippingCost;

         /**
         * 计费重（物流商）
         */
        private BigDecimal  billingWeightLogistics;

        /**
         * 实际运费（物流商）
         */
        private BigDecimal actualShippingCost;

        /**
         * 运费
         */
        private BigDecimal diffShippingCost;

        /**
         * 平台
         */
        private String   salesPlatform;

        /**
         * 平台名称
         */
        private String   salesPlatformName;

        /**
         * 来来源id
         */
        private String   sourceId;

        /**
         *  来源单号
         */
        private String  sourceType;

        /**
         * 来源单号
         */
        private String  sourceCode;

        /**
         * 销售出库单编码
         */
        private String  outstockCode;

        /**
         * 目的国家
         */
        private String  toCountry;

        /**
         * 客户名称
         */
        private String  customerName;

        /**
         * 订单时间
         */
        private LocalDateTime  orderTime;

        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;

        /**
         * 备注
         */
        private String  remark;

        /**
         * 币种
         */
        private String currency;
        /**
         * 币种符号
         */
        private String currencySymbol;

    }

    /**
     * 列表tab
     */
    @Data
    @NoArgsConstructor
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
        * 对账状态（字典reconciliationStatus）
        */
        private String reconciliationStatus;

        /**
        * 物流渠道id
        */
        private String channelId;

        /**
        * 物流单id
        */
        private String logisticsBillId;

        /**
        * 实重
        */
        private BigDecimal actualWeight;

        /**
        * 体积重
        */
        private BigDecimal volumeWeight;

        /**
        * 计费重
        */
        private BigDecimal billingWeight;

        /**
        * 预估运费
        */
        private BigDecimal estimatedShippingCost ;

        /**
        * 计费重（物流商）
        */
        private BigDecimal billingWeightLogistics;

        /**
        * 实际运费（物流商）
        */
        private BigDecimal actualShippingCost;

        /**
        * 运费差异
        */
        private BigDecimal diffShippingCost;

        /**
        * 币别
        */
        private String currency;

        /**
        * 备注
        */
        private String remark;


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
    public static class UpdateDTO  {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 计费重（物流商）
         */
        private BigDecimal billingWeightLogistics;

        /**
         * 实际运费（物流商）
         */
        @Digits(integer = 12, fraction = 4, message = "实际运费（物流商）整数位不能超过12位，小数位不能超过4位")
        private BigDecimal actualShippingCost;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 物流单id
        */
        @NotBlank(message = "物流单id不能为空")
        @Size(max = 19,message = "物流单id最大长度不能超过19位")
        private String logisticsBillId;

        /**
        * 实重
        */
        private BigDecimal actualWeight;

        /**
        * 体积重
        */
        private BigDecimal volumeWeight;

        /**
        * 预估运费
        */
        @Digits(integer = 12, fraction = 4, message = "预估运费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal estimatedShippingCost ;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 32,message = "币别最大长度不能超过32位")
        private String currency;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }
    /**
     * 修改导入数据
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDataDTO {

        /**
         * 物流单id
         */
        private String logisticsBillId ;

        /**
         * 计费重[物流商]
         */
        private BigDecimal billingWeightLogistics;

        /**
         * 实际运费[物流商]
         */
        private BigDecimal actualShippingCost;

        /**
         * 币种
         */
        private String currency;
    }


    /**
     * 修改状态
     */
    @Data
    @NoArgsConstructor
    public static class UpdateStatusDTO {

        /**
         * ids
         */
        private List<String> ids;

        /**
         * 状态
         */
        private String reconciliationStatus;

    }

}