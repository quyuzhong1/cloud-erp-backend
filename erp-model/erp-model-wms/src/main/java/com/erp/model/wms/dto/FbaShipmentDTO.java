package com.erp.model.wms.dto;

import java.time.LocalDateTime;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * FBI货件表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Data
@NoArgsConstructor
public class FbaShipmentDTO implements Serializable {

    /**
     * 导出
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO {
        /**
         * 详情Id
         */
        @ExcelIgnore
        private String detailId;
        /**
         * 货件单号
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "货件单号", index = 0)
        private String code;
        /**
         * 店铺名称
         */
        @ColumnWidth(15)
        @ExcelProperty(value = "店铺名称", index = 1)
        private String shopName;
        /**
         * 国家名称
         */
        @ColumnWidth(15)
        @ExcelProperty(value = "国家名称", index = 2)
        private String countryName;
        /**
         * 平台物流中心
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "平台物流中心", index = 3)
        private String fulfillmentCenter;
        /**
         * 发货状态名称
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "发货状态名称", index = 4)
        private String deliveryStatusName;
        /**
         * 发货单号
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "发货单号", index = 5)
        private String deliveryCode;

        /**
         * 装箱清单状态
         */
        @ColumnWidth(20)
        @ExcelIgnore
        private String packingDownload;
        /**
         * 平台货件状态
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "平台货件状态", index = 6)
        private String platformShipmentStatus;
        /**
         * ASIN
         */
        @ColumnWidth(30)
        @ExcelProperty(value = "ASIN", index = 7)
        private String asin;

        /**
         * MSKU
         */
        @ColumnWidth(30)
        @ExcelProperty(value = "MSKU", index = 8)
        private String msku;
        /**
         * FNSKU
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "FNSKU", index = 9)
        private String fnSku;
        /**
         * sku编号
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "sku编号", index = 10)
        private String skuNo;
        /**
         * 产品名称
         */
        @ColumnWidth(30)
        @ExcelProperty(value = "产品名称", index = 11)
        private String productName;
        /**
         * 申报数量
         */
        @ColumnWidth(20)
        @ExcelProperty(value = "申报数量", index = 12)
        private Integer declareQty;
        /**
         * 发货数量
         */
        @ColumnWidth(15)
        @ExcelProperty(value = "发货数量", index = 13)
        private Integer deliveryQty;
        /**
         * 签收数量
         */
        @ColumnWidth(15)
        @ExcelProperty(value = "签收数量", index = 14)
        private String receiveQty;
        /**
         * 在途数量
         */
        @ColumnWidth(15)
        @ExcelProperty(value = "在途数量", index = 15)
        private Integer transportQty;
        /**
         * 收发差异
         */
        @ColumnWidth(15)
        @ExcelProperty(value = "收发差异", index =16)
        private Integer diffQty;
        /**
         * 创建时间
         */
        @ColumnWidth(30)
        @ExcelProperty(value = "创建时间", index = 17)
        private LocalDateTime shipmentCreateTime;
        /**
         * 签收时间（拉取签收数据的日期）
         */
        @ColumnWidth(30)
        @ExcelProperty(value = "签收时间", index = 18)
        private LocalDateTime shipmentReceiveTime;
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
         * 单据编号
         */
        private String code;

        /**
         * FBA货件名称
         */
        private String name;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 国家二字码
         */
        private String countryId;

        /**
         * 国家名称
         */
        private String countryName;

        /**
         * 平台物流中心
         */
        private String fulfillmentCenter;

        /**
         * 发货状态
         */
        private String deliveryStatus;

        /**
         * 平台货件状态
         */
        private String platformShipmentStatus;

        /**
         * 创建时间（拉取数据的日期）
         */
        private LocalDateTime shipmentCreateTime;

        /**
         * 签收时间（拉取签收数据的日期）
         */
        private LocalDateTime shipmentReceiveTime;

        /**
         * 标签类型（NO_LABEL、SELLER_LABEL、AMAZON_LABEL）
         */
        private String labelType;

        /**
         * 包装类型（混装商品、原厂包装商品）
         */
        private String packType;

        /**
         * 发货地址
         */
        private String deliveryFromAddress;

        /**
         * 配送地址
         */
        private String deliveryToAddress;

        /**
         * 详情
         */
        private List<FbaShipmentDetailDTO.ViewDTO> detailList;
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
         * FBA货件名称
         */
        @NotBlank(message = "FBA货件名称不能为空")
        @Size(max = 255,message = "FBA货件名称最大长度不能超过255位")
        private String name;

        /**
         * 店铺id
         */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 64,message = "店铺id最大长度不能超过64位")
        private String shopId;

        /**
         * 店铺名称
         */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255,message = "店铺名称最大长度不能超过255位")
        private String shopName;

        /**
         * 国家二字码
         */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 10,message = "国家二字码最大长度不能超过10位")
        private String countryId;

        /**
         * 国家名称
         */
        @NotBlank(message = "国家名称不能为空")
        @Size(max = 64,message = "国家名称最大长度不能超过64位")
        private String countryName;

        /**
         * 平台物流中心
         */
        @NotBlank(message = "平台物流中心不能为空")
        @Size(max = 64,message = "平台物流中心最大长度不能超过64位")
        private String fulfillmentCenter;

        /**
         * 发货状态
         */
        @NotBlank(message = "发货状态不能为空")
        @Size(max = 255,message = "发货状态最大长度不能超过255位")
        private String deliveryStatus;

        /**
         * 平台货件状态
         */
        @NotBlank(message = "平台货件状态不能为空")
        @Size(max = 255,message = "平台货件状态最大长度不能超过255位")
        private String platformShipmentStatus;

        /**
         * 创建时间（拉取数据的日期）
         */
        private LocalDateTime shipmentCreateTime;

        /**
         * 签收时间（拉取签收数据的日期）
         */
        private LocalDateTime shipmentReceiveTime;

        /**
         * 标签类型（NO_LABEL、SELLER_LABEL、AMAZON_LABEL）
         */
        @NotBlank(message = "标签类型（NO_LABEL、SELLER_LABEL、AMAZON_LABEL）不能为空")
        @Size(max = 64,message = "标签类型（NO_LABEL、SELLER_LABEL、AMAZON_LABEL）最大长度不能超过64位")
        private String labelType;

        /**
         * 包装类型（混装商品、原厂包装商品）
         */
        @NotBlank(message = "包装类型（混装商品、原厂包装商品）不能为空")
        @Size(max = 64,message = "包装类型（混装商品、原厂包装商品）最大长度不能超过64位")
        private String packType;

        /**
         * 发货地址
         */
        @NotBlank(message = "发货地址不能为空")
        private String deliveryFromAddress;

        /**
         * 配送地址
         */
        @NotBlank(message = "配送地址不能为空")
        private String deliveryToAddress;


    }

    /**
     * 拉取货件信息DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PullShipmentDTO {
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 货件单号集合
         */
        private List<String> shipmentCodeList;
    }

    /**
     * sku映射参数
     */
    @Data
    @NoArgsConstructor
    public static class SkuMappingParamDTO {

        /**
         * SkuMapping的ID
         */
        private String id;
        /**
         * 详情id
         */
        private String detailId;
        /**
         * erp下拉的sku编号
         */
        private String skuNo;
        /**
         * 卖家sku
         */
        private String msku;
        /**
         * 店铺id（后端用）
         */
        private String shopId;
        /**
         * 平台（后端用）
         */
        private String platform;
        /**
         * erp下拉的sku id (比skuNo优先)
         */
        private String skuId;
    }

    /**
     * 列表查询参数
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
        private Map<String, String> sqlMap;


    }

    /**
     * 列表查询返回值
     */
    @Data
    @NoArgsConstructor
    public static class SearchResultDTO {
        private String id;
        private String code;
    }

    /**
     * 列表查询返回值
     */
    @Data
    @NoArgsConstructor
    public static class SearchDTO {
        private String code;
        private String id;

        /**
         * 要货申请id
         */
        private String requisitionId;
        private String shopId;
    }

    /**
     * 列表查询返回值
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 详情Id
         */
        private String detailId;
        /**
         * 货件单号
         */
        private String code;
        /**
         * 店铺Id
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 国家Id
         */
        private String countryId;
        /**
         * 国家名称
         */
        private String countryName;
        /**
         * 平台物流中心
         */
        private String fulfillmentCenter;
        /**
         * 发货状态编码
         */
        private String deliveryStatus;
        /**
         * 发货状态名称
         */
        private String deliveryStatusName;
        /**
         * 发货单号
         */
        private String deliveryCode;
        /**
         * 平台货件状态
         */
        private String platformShipmentStatus;
        /**
         * 平台产品id
         */
        private String asin;

        /**
         * 平台sku
         */
        private String msku;
        /**
         * FNSKU
         */
        private String fnSku;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 申报数量
         */
        private Integer declareQty;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 签收数量
         */
        private Integer receiveQty;
        /**
         * 收发差异
         */
        private Integer diffQty;
        /**
         * 在途数量
         */
        private Integer transportQty;
        /**
         * 创建时间（拉取数据的日期）
         */
        private LocalDateTime shipmentCreateTime;
        /**
         * 签收时间（拉取签收数据的日期）
         */
        private LocalDateTime shipmentReceiveTime;
        /**
         * 是否组合品 true 是
         */
        private Boolean isCombination;

        /**
         * 装箱清单（bool)
         */
        private Boolean isPackingDownload;
        /**
         * 装箱清单下载状态
         */
        private String packingDownload;
    }

    /**
     * 货件状态记录
     */
    @Data
    @NoArgsConstructor
    public static class ShipmentStatusRecordView {
        /**
         * 货件状态
         */
        private String shipmentStatus;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    /**
     * 查询收货记录返回值
     */
    @Data
    @NoArgsConstructor
    public static class ReceiveRecordView {
        /**
         * 签收时间
         */
        private LocalDateTime receiveTime;
        /**
         * 签收数量
         */
        private Integer receiveQty;

        @Override
        public String toString(){

            return "签收时间:"+LocalDateTimeUtil.formatNormal(receiveTime) + " 签收数量:" + receiveQty;
        }

    }

    /**
     * 下推发货单列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateDeliverView {
        /**
         * 明细id
         */
        private String id;
        /**
         * 主表id
         */
        private String mainId;
        /**
         * 货件单号
         */
        private String code;
        /**
         * 平台物流中心
         */
        private String fulfillmentCenter;
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 国家二字码
         */
        private String countryId;
        /**
         * 国家名称
         */
        private String countryName;
        /**
         * 发货仓id
         */
        private String deliveryWarehouseId;
        /**
         * 发货仓名称
         */
        private String deliveryWarehouseName;
        /**
         * 目的仓id
         */
        private String destWarehouseId;
        /**
         * 目的仓名称
         */
        private String destWarehouseName;
        /**
         * 平台产品id
         */
        private String asin;

        /**
         * 平台sku
         */
        private String msku;
        /**
         * fnSku
         */
        private String fnSku;
        /**
         * ERP的SKU
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 申报数量
         */
        private Integer declareQty;
        /**
         * 发货数量
         */
        private Integer deliveryQty;
        /**
         * 是否组合品 true 是
         */
        private Boolean isCombination;
        /**
         * 仓位
         */
        private String warehouseLocation;

    }

    /**
     * 下推发货单列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateRequisitionApplicationViewDTO {
        /**
         * 主表id
         */
        private String sourceId;

        /**
         * 明细id
         */
        private String sourceDetailId;

        /**
         * 发货计划单号
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源类型中文
         */
        private String sourceTypeName;

        /**
         * 要货类型
         */
        private String type;

        /**
         * 要货类型中文
         */
        private String typeName;

        /**
         * 渠道
         */
        private String channelId;

        /**
         * 渠道中文名
         */
        private String channelName;

        /**
         * 平台产品id
         */
        private String asin;

        /**
         * 平台sku
         */
        private String msku;

        /**
         * FNSKU
         */
        private String fnSku;

        /**
         * sku表id
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 是否组合品
         */
        private Boolean isCombination;

        /**
         * bom版本
         */
        private String bomVersion;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 申报数量
         */
        private Integer declareQty;

        /**
         * 要货仓库id
         */
        private String requisitionWarehouseId;

        /**
         * 可用库存
         */
        private Integer usableQty;

        /**
         * 要货数量
         */
        @NotNull(message = "要货数量不能为空")
        @DecimalMax(value = "999999999",message ="要货数量超出最大值" )
        @DecimalMin(value = "1",message ="要货数量最小值为1" )
        private Integer requisitionQty;
    }

}