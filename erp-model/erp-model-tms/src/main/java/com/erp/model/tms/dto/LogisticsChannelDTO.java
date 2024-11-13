package com.erp.model.tms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.enums.UnitEnum;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 物流渠道表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Data
@NoArgsConstructor
public class LogisticsChannelDTO implements Serializable {


    /**
     * 基础信息
     */
    @Data
    @NoArgsConstructor
    public static class BaseDTO {
        /**
         * 渠道id
         */
        private String id;


        private String mainId;

        private String sourceId;

        /**
         * 渠道名
         */
        private String name;
        /**
         * 物流商简称
         */
        private String logisticsSupplierShortName;
        /**
         * 物流商名
         */
        private String logisticsSupplierName;

        private String logisticsSupplierId;
        private String supplierId;

        /**
         * 渠道代码
         */
        private String code;

        /**
         * 渠道分拣码
         */
        private String sortingCode;

        /**
         * 渠道时效
         */
        private String effectiveTimeStr;


        /**
         * 运费模板名
         */
        private String shippingTemplateName;

        private Boolean disabled;

        /**
         * 平台编码
         */
        private String logisticsPlatform;

        /**
         * 平台是否允许打印
         */
        private Boolean isPrintPlatform;

        /**
         * 纸张大小
         */
        private String paperSize;

        /**
         * 配送方式/发货方式（上门揽收DOOR_PICKUP, 自寄SELF_POST, 自送SELF_SEND）
         * DeliveryTypeEnum
         * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = deliveryType
         */
        private String deliveryType;
        /**
         * 不可达处理 退回:return/销毁:return 默认 return销毁
         * UnDeliverableDecisionEnum
         * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = undeliverableDecision
         */
        private String undeliverableDecision;
    }


    /**
     * 约束规则DTO
     */
    @Data
    @NoArgsConstructor
    public static class LogisticsChannelConstraintDTO {
        /**
         * 渠道id
         */
        private String  channelId;
        /**
         * 物流平台（下单平台）
         */
        private String  logisticsPlatform;

        /**
         * 国家二字码
         */
        private String country;
        /**
         * 国家名称
         */
        private String countryName;

        /**
         * 最高报关金额
         */
        private BigDecimal maxCustomsAmount = BigDecimal.ZERO;

        /**
         * 最高报关币别
         */
        private String maxCustomsCurrency = CurrencyEnum.USD.getCurrencyCode();

        /**
         * 最低报关金额
         */
        private BigDecimal minCustomsAmount = BigDecimal.ZERO;

        /**
         * 最低报关币种
         */
        private String minCustomsCurrency = CurrencyEnum.USD.getCurrencyCode();;

        /**
         * 重量上限
         */
        private BigDecimal maxWeight = BigDecimal.ZERO;;

        /**
         * 重量单位
         */
        private String weightUnit = UnitEnum.WeightUnitEnum.G.getCode();

        /**
         * 长度上限
         */
        private BigDecimal maxLength = BigDecimal.ZERO;

        /**
         * 宽度上限
         */
        private BigDecimal maxWidth = BigDecimal.ZERO;

        /**
         * 高度上限
         */
        private BigDecimal maxHeight = BigDecimal.ZERO;

        /**
         * 尺寸单位
         */
        private String sizeUnit = "cm";

    }

    /**
     * 基础信息
     */
    @Data
    @NoArgsConstructor
    public static class LogisticsPlatformDTO {

        /**
         * 渠道id
         */
        private String  channelId;

        /**
         * 对应平台
         */
        private String logisticsPlatform;

        /**
         * 授权id
         */
        private String authId;

    }


    /**
     * 标记发货所需要的参数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignShipDTO{

        /**
         * logistics_sale_channel销售渠道ID
         */
        private String logisticsSaleChannelId;

        /**
         * logistics_channel渠道ID
         */
        private String logisticsChannelId;
        /**
         * logistics_channel渠道名称
         */
        private String logisticsChannelName;

        /**
         * 销售渠道代号
         */
        private String code;

        /**
         * 渠道供应商名称 logistics_sale_channel
         */
        private String saleChannelSupplierName;

        /**
         * 标记发货订单类型（transportNo运单号、trackNo跟踪号）
         */
        private String orderDeliveryMarkType;

        /**
         * 承运商代号(部分速卖通物流渠道必填)
         */
        private String carrierCode;

        /**
         * 承运商轨迹查询地址(部分速卖通物流渠道必填)
         */
        private String logisticsTrackUrl;

        public String checkAndGetOrderDeliveryMarkType(){
            if (StringUtils.isBlank(this.getOrderDeliveryMarkType())){
                throw new ServiceException("操作失败，渠道标发单号配置为空");
            }
            return this.getOrderDeliveryMarkType();
        }

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
        private String id;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 渠道名
         */
        private String name;

        /**
         * 渠道代码
         */
        private String code;

        /**
         * 物流商id
         */
        private String mainId;

        /**
         * 时效
         */
        private String effectiveTime;

        /**
         * 时效单位
         */
        private String effectiveTimeUnit;

        /**
         * 物流轨迹查询方式
         */
        private String trackQueryMode;

        /**
         * 纸张长
         */
        private Integer paperLength;

        /**
         * 纸张宽
         */
        private Integer paperWidth;

        /**
         * 纸张大小
         */
        private String paperSize;

        /**
         * 分拣码
         */
        private String sortingCode;

        /**
         * 运费模板id
         */
        private String shippingTemplateId;

        /**
         * 运费模板名称
         */
        private String shippingTemplateName;

        /**
         * 费用规则
         */
        private String feeRule;



        /**
         * 最高报关金额
         */
        private BigDecimal maxCustomsAmount;

        /**
         * 最高报关币别
         */
        private String maxCustomsCurrency;

        /**
         * 最低报关金额
         */
        private BigDecimal minCustomsAmount;

        /**
         * 最低报关币种
         */
        private String minCustomsCurrency;

        /**
         * 重量上限
         */
        private BigDecimal maxWeight;

        /**
         * 重量单位
         */
        private String weightUnit;
        /**
         * 长度上限
         */
        private BigDecimal maxLength;
        /**
         * 尺寸单位
         */
        private String sizeUnit;
        /**
         * 宽度上限
         */
        private BigDecimal maxWidth;
        /**
         * 高度上限
         */
        private BigDecimal maxHeight;
        /**
         * 税费模式
         */
        private String taxModel;

        /**
         * 是否ioss 预交
         */
        private Boolean isIossPrepay;

        /**
         * 是否签名服务
         */
        private Boolean isApiSign;

        /**
         * 是否保险
         */
        private Boolean isApiInsurance;

        /**
         * 保宏单号（报关单号类型）
         */
        private String declareCodeType;
        /**
         * 配送方式/发货方式（上门揽收DOOR_PICKUP, 自寄SELF_POST, 自送SELF_SEND）
         * DeliveryTypeEnum
         * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = deliveryType
         */
        private String deliveryType;
        /**
         * 不可达处理 退回:return/销毁:return 默认 return销毁
         * UnDeliverableDecisionEnum
         * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = undeliverableDecision
         */
        private String undeliverableDecision;
        /**
         * 轨迹查询单号（运单号transportNo跟踪号trackNo）
         * TrackQueryTypeEnum
         * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = trackQueryType
         */
        private String trackQueryType;
        /**
         * 材积设置
         */
        private Integer volumeSetting;

        /**
         * 物流映射列表
         */
        private List<LogisticsMappingDTO.ViewDTO> mappingList;

        /**
         * 打印标签类型
         */
        private List<LogisticsPrintTypeDTO.ViewDTO> printTypeList;


        /**
         * 地址列表
         */
        private List<LogisticsChannelAddressDTO.ViewDTO> addressList;

        /**
         * 发货限制列表
         */
        private List<LogisticsChannelBlacklistDTO.ViewDTO> blackList;

        /**
         * 仓库设置
         */
        private LogisticsChannelWarehouseDTO.ViewDTO warehouseDTO;
    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 物流映射列表
         */
        private List<LogisticsMappingDTO.AddDTO> mappingList;

        /**
         * 打印标签类型
         */
        private List<LogisticsPrintTypeDTO.AddDTO> printTypeList;


        /**
         * 地址设置列表
         */
        private List<LogisticsChannelAddressDTO.AddDTO> addressList;

        /**
         * 发货限制列表
         */
        private List<LogisticsChannelBlacklistDTO.AddDTO> blackList;

        /**
         * 仓库设置
         */
        @NotNull(message = "仓库设置不能为空")
        private LogisticsChannelWarehouseDTO.BatchUpdateDTO warehouseDTO;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 渠道
         */
        @NotBlank(message = "渠道不能为空")
        private String id;


        /**
         * 物流映射列表
         */
        private List<LogisticsMappingDTO.UpdateDTO> mappingList;

        /**
         * 打印标签类型
         */
        private List<LogisticsPrintTypeDTO.UpdateDTO> printTypeList;


        /**
         * 地址设置列表
         */
        private List<LogisticsChannelAddressDTO.UpdateDTO> addressList;

        /**
         * 发货限制列表
         */
        private List<LogisticsChannelBlacklistDTO.AddDTO> blackList;

        /**
         * 仓库设置
         */
        @NotNull(message = "仓库设置不能为空")
        private LogisticsChannelWarehouseDTO.BatchUpdateDTO warehouseDTO;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 物流商
         *
         */
        @NotBlank(message = "物流商不能为空")
        private String mainId;



        /**
         * 渠道名称
         */
        @NotBlank(message = "渠道名称不能为空")
        @Size(max = 100, message = "渠道名称最大长度不能超过100位")
        private String name;


        /**
         * 渠道代码
         */
        private String code;

        /**
         * 时效
         */
        private String effectiveTime;

        /**
         * 时效单位
         * 默认day
         */
        @NotBlank(message = "时效单位不能为空")
        private String effectiveTimeUnit;

        /**
         * 物流轨迹查询方式
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25522  key=trackQueryMode
         */
        @NotBlank(message = "物流轨迹查询方式不能为空")
        @Size(max = 30, message = "物流轨迹查询方式最大长度不能超过30位")
        private String trackQueryMode;


        /**
         * 纸张大小
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25522  key=paperSize
         */
        @NotBlank(message = "纸张大小不能为空")
        private String paperSize;


        /**
         * 分拣码
         */
        @Size(max = 30, message = "分拣码最大长度不能超过10位")
        private String sortingCode;


        /**
         * 运费模板id
         */
        private String shippingTemplateId;

        /**
         * 费用规则
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25522  key=shippingFeeRule
         */
        private String feeRule;

        /**
         * 最高报关金额
         */
        @Digits(integer = 12, fraction = 4, message = "最高报关金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal maxCustomsAmount;

        /**
         * 最高报关金额币别
         */
        @NotBlank(message = "最高报关金额币别不能为空")
        private String maxCustomsCurrency;

        /**
         * 最低报关金额
         */
        @Digits(integer = 12, fraction = 4, message = "最低报关金额整数位不能超过12位，小数位不能超过4位")
        private BigDecimal minCustomsAmount;

        /**
         * 最低报关金额币种
         */
        @NotBlank(message = "最低报关金额币种不能为空")
        private String minCustomsCurrency;

        /**
         * 重量上限
         */
        private BigDecimal maxWeight;

        /**
         * 重量单位
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25522  key=weightUnit
         */
        @NotBlank(message = "重量单位不能为空")
        private String weightUnit;

        /**
         * 长度上限
         */
        @Digits(integer = 12, fraction = 4, message = "长度上限整数位不能超过12位，小数位不能超过4位")
        private BigDecimal maxLength;
        /**
         * 宽度上限
         */
        @Digits(integer = 12, fraction = 4, message = "宽度上限整数位不能超过12位，小数位不能超过4位")
        private BigDecimal maxWidth;
        /**
         * 高度上限
         */
        @Digits(integer = 12, fraction = 4, message = "高度上限整数位不能超过12位，小数位不能超过4位")
        private BigDecimal maxHeight;

        /**
         * 税费模式
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25522  key=taxModel
         */
        private String taxModel;

        /**
         * 是否ioss 预交
         */
        private Boolean isIossPrepay;

        /**
         * 是否签名服务
         */
        private Boolean isApiSign;

        /**
         * 是否保险
         */
        private Boolean isApiInsurance;

        /**
         * 保宏单号（报关单号类型） /tms/drop/down/dict/list?key=declareCodeType
         */
        private String declareCodeType;

        /**
         * 配送方式（上门揽收DOOR_PICKUP, 自寄SELF_POST, 自送SELF_SEND）
         */
        private String deliveryType;
        /**
         * 不可达处理 退回:return/销毁:return 默认 return销毁
         * UnDeliverableDecisionEnum
         * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = undeliverableDecision
         */
        private String undeliverableDecision;
        /**
         * 轨迹查询单号（运单号transportNo跟踪号trackNo）
         * TrackQueryTypeEnum
         * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = trackQueryType
         */
        private String trackQueryType;
        /**
         * 材积设置
         */
        private Integer volumeSetting;
    }

    @Data
    @NoArgsConstructor
    public static class ListSelectDTO {

        /**
         * 物流商Id
         */
        private String logisticsSupplierId;

        /**
         * 物流商名称
         */
        private String supplierName;

        /**
         * 渠道名称
         */
        private String name;
        /**
         * 渠道编码
         */
        private String code;

        /**
         * 渠道id
         */
        private String id;

        /**
         * 是否禁用
         */
        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamDTO {

        /**
         * 物流商ids
         */
        private List<String> ids;

        /**
         * 渠道名称
         */
        private String name;

        public ParamDTO(List<String> ids) {
            this.ids = ids;
        }
    }

    @Data
    @NoArgsConstructor
    public static class ProvideChannelDTO {

        /**
         * 渠道id
         */
        private String channelId;

        /**
         * 渠道code
         */
        private String channelCode;

        /**
         * 物流供应商id
         */
        private String logisticsSupplierId;

        /**
         * 物流供应商名称
         */
        private String logisticsSupplierName;
    }
    @Data
    @NoArgsConstructor
    public static class PagingSelectDTO {
        /**
         * 渠道id
         */
        private String id;
        /**
         * 启用/禁用
         */
        private Boolean disabled;
        /**
         * 编码
         */
        private String code;
        /**
         * 值
         */
        private String value;
        /**
         * 物流供应商id
         */
        private String logisticsSupplierId;

        /**
         * 物流供应商名称
         */
        private String logisticsSupplierName;
    }
    @Data
    @NoArgsConstructor
    public static class SelectDTO {

        /**
         * 关键词
         */
        private String searchKeyword;
        /**
         * 禁用状态
         */
        private Boolean disabled;
        /**
         * 是否展示物流商
         */
        private Boolean showSupplier=false;
    }

    @Data
    @NoArgsConstructor
    public static class DeliveryDTO{
        /**
         * 渠道id
         */
        @NotBlank(message = "渠道id不能为空")
        private String id;

        /**
         * 配送方式/发货方式（上门揽收DOOR_PICKUP, 自寄SELF_POST, 自送SELF_SEND）
         * DeliveryTypeEnum
         * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = deliveryType
         */
        private String deliveryType;
        /**
         * 不可达处理 退回:return/销毁:return 默认 return销毁
         * UnDeliverableDecisionEnum
         * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = undeliverableDecision
         */
        private String undeliverableDecision;
    }

    /**
     * 飞书预警汇总
     */
    @Data
    @NoArgsConstructor
    public static class WarnReportDTO {
    }
}