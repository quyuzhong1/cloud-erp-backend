package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 物流单请求响应实体
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Data
@NoArgsConstructor
public class LogisticsBillDTO implements Serializable {


    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {
        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名
         */
        private String tabName;

        /**
         * 数量
         */
        private Integer count;
    }


    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO{
        private List<String> ids;
    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {


        /**
         * 类型
         * 来源  http://172.16.100.11:3002/project/128/interface/api/25522 key=logisticTrackStatusGroup
         */
        @NotBlank(message = "类型不能为空")
        private String  type;


        /**
         * 订单号集合
         */
        private List<String> orderNoList;

        /**
         * 物流单号
         */
        private List<String>  trackNoList;
        /**
         * 销售平台
         * 来源 http://172.16.100.11:3002/project/110/interface/api/13435  key=salesPlatform
         */
        private List<String>  salesPlatformList;


        /**
         * 订单类型
         * 来源  http://172.16.100.11:3002/project/128/interface/api/25522 key=orderType
         */
        private List<String>  orderTypeList;


        /**
         * 店铺
         */
        private List<String>  shopIdList;

        /**
         * 下单时间
         */
        private List<LocalDate>  orderTimeList;


        /**
         * 发货时间
         */
        private List<LocalDate>  deliveryTimeList;

        /**
         * 物流渠道
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25999
         */
        private List<String> channelIdList;


        /**
         * 包裹状态
         * 来源  http://172.16.100.11:3002/project/128/interface/api/25522 key=logisticTrackStatus
         */
        private List<String> trackStatusList;

        /**
         * 排除的类型
         */
        private List<String> excludeOrderTypeList;

    }

    /**
     * 分页
     */
    @Data
    @NoArgsConstructor
    public static class PagingVO {

        /**
         * id
         */
        private String id;



        /**
         * 销售平台 [可排序]
         */
        private String salesPlatform;

        /**
         * 销售平台名
         */
        private String salesPlatformName;

        /**
         * 店铺 或者客户id [可排序]
         */
        private String shopId;

        /**
         * 店铺 或者客户名 [可排序]
         */
        private String shopName;



        /**
         * 订单类型[可排序]
         */
        private String orderType;

        /**
         * 订单类型名称
         */
        private String orderTypeName;


        /**
         * 订单号[可排序]
         */
        private String sourceCode;


        /**
         * 出库单号[可排序]
         */
        private String outstockCode;

        /**
         * 目的国家[可排序]
         */
        private String toCountry;

        /**
         * 下单时间[可排序]
         */
        private LocalDateTime orderTime;


        /**
         * 发货时间[可排序]
         */
        private LocalDateTime deliveryTime;


        /**
         * 签收时间[可排序]
         */
        private LocalDateTime signTime;

        /**
         * 物流渠道id
         */
        private String channelId;


        /**
         * 物流渠道名
         */
        private String channelName;


        /**
         * 物流单[可排序]
         */
        private String trackNo;

        /**
         * 运输天数
         */
        private Integer transportDays;

        /**
         * 运输状态 [可排序]
         */
        private String trackStatus;


        /**
         * 运输状态名
         */
        private String trackStatusName;

        /**
         * 轨迹描述
         */
        private String trackContent;

        /**
         * 最新更新时间
         */
        private LocalDateTime updateTime;

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
         * 销售平台
         */
        private String salesPlatform;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源id 销售订单
         */
        private String sourceId;

        /**
         * 来源code
         */
        private String sourceCode;

        /**
         * 出库id
         */
        private String outstockId;

        /**
         * 出库code
         */
        private String outstockCode;

        /**
         * 渠道id
         */
        private String channelId;

        /**
         * 下单时间
         */
        private LocalDateTime orderTime;

        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;

        /**
         * 运输单号
         */
        private String transportNo;

        /**
         * 跟踪号
         */
        private String trackNo;


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 物流明细信息
         */
        private List<LogisticsBillDetailDTO.AddDTO> detailList;
    }
    @Data
    @NoArgsConstructor
    public static class RemoveDTO{

        @NotNull(message = "出库单不能为空")
        @Size(min = 1,message = "出库单不能为空")
        private List<String> outstockIdList;

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
         * 物流明细信息
         */
        @NotNull(message = "物流明细信息不能为空")
        private List<LogisticsBillDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 销售平台
         */
        @Size(max = 30, message = "销售平台最大长度不能超过30位")
        private String salesPlatform;

        /**
         * 店铺id
         */
        @Size(max = 19, message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
         * 店铺名称
         */
        @Size(max = 50, message = "店铺名称最大长度不能超过50位")
        private String shopName;

        /**
         * 来源类型
         */
        @Size(max = 30, message = "来源类型最大长度不能超过30位")
        private String sourceType;


        /**
         * 来源id 销售订单
         */
        @Size(max = 19, message = "来源id 销售订单最大长度不能超过19位")
        private String sourceId;

        /**
         * 来源code
         */
        @Size(max = 30, message = "来源code最大长度不能超过30位")
        private String sourceCode;

        /**
         * 出库id
         */
        @Size(max = 19, message = "出库id最大长度不能超过19位")
        private String outstockId;

        /**
         * 出库code
         */
        @Size(max = 30, message = "出库code最大长度不能超过30位")
        private String outstockCode;

        /**
         * 渠道id
         */
        private String channelId;


        /**
         * 目的地
         */
        private String toCountry;

        /**
         * 下单时间
         */
        private LocalDateTime orderTime;

        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;

        /**
         * 运输单号
         */
        @Size(max = 50, message = "运输单号最大长度不能超过50位")
        private String transportNo;


        /**
         * 币别
         */
        private String currency;

        /**
         * 订单类型
         */
        private String orderType;



    }

    /**
     * 基础信息
     */
    @Data
    @NoArgsConstructor
    public static class BaseDTO {

        private String id;

        /**
         * 销售平台
         */
        private String salesPlatform;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 来源类型
         */
        private String sourceType;


        /**
         * 来源id 销售订单
         */
        private String sourceId;

        /**
         * 来源code
         */
        private String sourceCode;

        /**
         * 出库id
         */
        private String outstockId;

        /**
         * 出库code
         */
        private String outstockCode;

        /**
         * 渠道id
         */
        private String channelId;


        /**
         * 目的地
         */
        private String toCountry;

        /**
         * 下单时间
         */
        private LocalDateTime orderTime;

        /**
         * 发货时间
         */
        private LocalDate deliveryTime;

        /**
         * 运输单号
         */
        private String transportNo;


        /**
         * 币别
         */
        private String currency;

        /**
         * 订单类型
         */
        private String orderType;

        private String trackNo;

        private String detailId;



    }

    /**
     * 查询物流信息
     */
    @Data
    @NoArgsConstructor
    public static class LogisticsBillVo {
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源单号
         */
        private String sourceCode;
        /**
         * 运输单号
         */
        private String transportNo;
        /**
         * 运输状态
         */
        private String trackStatus;
        /**
         * 跟踪单号
         */
        private String trackNo;
    }


    /**
     * 生成物流单
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateBillDTO{

        private String token;


        /**
         * 速卖通 ISV用户唯一标识，一般为userId,最大长度为16个字符
         */
        private String topUserKey;

        /**
         *订单对应收货地址OAID 目前速卖通用到
         */
        private String oaid;

        @NotBlank(message = "渠道不能为空")
        private String channelId;

        private String shopId;

        private String shopName;

        /**
         * IOSS 税号
         */
        private String iossTaxNo;

        /**
         * 币别
         */
        private String currency;
        /**
         * 来源类型
         */
        private String sourceType;

        private String orderType;

        private LocalDateTime orderTime;

        private String orderId;

        private String orderCode;

        /**
         * 销售平台
         */
        private String salesPlatform;
        /**
         * 物流单号
         */
        private String trackNo;

        /**
         * 物流类型
         * 枚举：OrderLogisticTypeEnum
         */
        private String logisticType;

        /**
         * 收货人
         */
        private ReceiverDTO receiver;

        /**
         * 包裹信息
         */
        private PackageDTO packageInfo;

        /**
         * sku Id list
         */
        @Size(min = 1,message = "sku信息不能为空")
        @NotNull(message = "sku信息不能为空L")
        private List<SkuDTO> skuList;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerateBillResultDTO{

        /**
         * 运输单号
         */
        private String transportNo;

        /**
         * 跟踪单号
         */
        private List<String> trackNoList;


    }


    /**
     * 取消物流单
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CancelBillDTO{

        @NotBlank(message = "渠道不能为空")
        private String channelId;

        /**
         * 客户参考号 (erp 销售订单code)
         */
        @NotBlank(message = "客户参考号不能为空")
        private String referenceNumber;

        /**
         * 运单号（运单号和跟踪单号不能都为空）
         */
        private String transportNo;

        /**
         * 跟踪单号（运单号和跟踪单号不能都为空）
         */
        private String trackNo;

        /**
         *  订单id(erp 销售订单id)
         */
        private String orderId;

        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiverDTO{

        /**
         * 收件人
         */
        private String name;

        /**
         * 买价id
         */
        private String customerId;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 收货人名
         */
        private String receiverName;

        /**
         * 收货人电话号码
         */
        private String receiverTelNumber;


        /**
         * 街道详细地址
         */
        private String fullAddress;

        /**
         * 邮编
         */
        private String postCode;


        /**
         * 国家
         */
        private String country;

        /**
         * 国家名
         */
        private String countryName;

        /**
         * 省名
         */
        private String provinceName;

        /**
         * 城市名
         */
        private String cityName;

        /**
         * 区名
         */
        private String districtName;

        /**
         * 收货第一地址
         */
        private String firstAddress;

        /**
         * 收货第二地址
         */
        private String secondAddress;

        /**
         * 收货人税号
         */
        private String receiverTaxNo;

    }

    @Data
    @NoArgsConstructor
    public static class BatchUpdateTrackNoDTO{

        /**
         * 销售出单单
         */
        private SoOutstockEntity soOutstockEntity;

        /**
         * 渠道id
         */
        private String logisticsChannelId;

        /**
         * 跟踪单号
         */
        private List<String> trackNoList;

    }

    @Data
    @NoArgsConstructor
    public static class UpdateTrackNoDTO{

        /**
         * 销售出单单id 集合
         */
        private List<String> outstockIdList;

        /**
         * 运输单号
         */
        private String trackNo;

    }


    /**
     * 产品信息
     */
    @Data
    @NoArgsConstructor
    public static class SkuDTO{

        private String skuId;

        private String skuNo;

        private Integer qty;

        /**
         * 平台sku
         */
        private String platformSkuNo;

        /**
         * 平台产品
         */
        private String platformSpuNo;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PackageDTO{

        /**
         * 重量 单位 g
         */
        private BigDecimal weight;

        /**
         * 包裹长(单位:cm)
         */
        private BigDecimal length;

        /**
         * 包裹宽(单位:cm)
         */
        private BigDecimal width;

        /**
         * 包裹高(单位:cm)
         */
        private BigDecimal height;

        /**
         * 币别
         */
        private String currency;




    }

    /**
     * 批量更新状态
     */
    @Data
    @NoArgsConstructor
    public static class BatchUpdateStatusDTO{
        /**
         * ids
         */
        @Size(min = 1,message = "物流单不能为空")
        private List<String> ids;

        /**
         * 运输状态
         * 来源 http://172.16.100.11:3002/project/128/interface/api/25522 key=logisticTrackStatus
         */
        private String trackStatus;

    }

    /**
     * 打印物流面单参数
     */
    @Data
    @NoArgsConstructor
    public static class PrintLogisticsWaybillDTO {

        /**
         * 销售单id
         */
        private String b2cSoId;

        @NotBlank(message = "渠道不能为空")
        private String channelId;

        /**
         * 运单号
         */
        private String transportNo;

        /**
         * 发货单号
         */
        @NotBlank(message = "发货单号不能为空")
        private String deliveryNo;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 物流类型
         */
        private String logisticType;

    }


    /**
     * 打印物流面单参数
     */
    @Data
    @NoArgsConstructor
    public static class SoB2cLabelDTO {
        /**
         * 销售单id
         */
        private String soB2cId;

        /**
         * 物流面单base64格式
         */
        private List<String> logisticsBase64;
    }
}