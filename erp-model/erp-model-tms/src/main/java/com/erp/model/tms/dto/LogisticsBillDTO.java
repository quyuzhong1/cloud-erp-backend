package com.erp.model.tms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
         * 来源  http://172.16.100.11:3002/project/128/interface/api/25522 key=salesOrderType
         */
        private List<String>  sourceTypeList;


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
         * 销售平台
         */
        private String salesPlatform;

        /**
         * 销售平台名
         */
        private String salesPlatformName;

        /**
         * 店铺 或者客户id
         */
        private String shopId;

        /**
         * 店铺 或者客户名
         */
        private String shopName;



        /**
         * 订单来源类型
         */
        private String sourceType;

        /**
         * 订单来源类型名称
         */
        private String sourceTypeName;


        /**
         * 订单号
         */
        private String sourceCode;


        /**
         * 出库单号
         */
        private String outstockCode;

        /**
         * 目的国家
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
         * 签收时间
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
         * 物流单
         */
        private String trackNo;

        /**
         * 运输天数
         */
        private Integer transportDays;

        /**
         * 运输状态
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
         * 来源类型名
         */
        private String sourceTypeName;

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
        @Size(max = 50, message = "运输单号最大长度不能超过50位")
        private String transportNo;


        /**
         * 币别
         */
        private String currency;

        private String orderType;


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
    public static class GenerateBillDTO{

       private String token;


        @NotBlank(message = "渠道不能为空")
        private String channelId;

        /**
         * 来源类型
         */
        private String sourceType;

        private String orderId;

        private String orderCode;


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
    public static class ReceiverDTO{

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

    }


    @Data
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

}