package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.entity.FbaInventoryEntity;
import com.erp.model.wms.entity.FbaInventoryReservedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * FBI库存请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
*/
@Data
@NoArgsConstructor
public class FbaInventoryDTO implements Serializable {




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
        * 平台唯一id
        */
        private String platformCode;

        /**
        * 仓库名称
        */
        private String name;

        /**
        * 平台sku
        */
        private String asin;

        /**
        * 卖家sku
        */
        private String msku;

        /**
        * FNSKU
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
         * 配送渠道：/wms/common/enumDropDown?type=DeliveryChannels
         *    self_delivery：卖家自配送
         *    amazon_delivery：亚马逊配送
         */
        private String deliveryChannels;

        /**
        * FBM可售
        */
        private Integer fbmFulfillableQty;

        /**
        * 计划入库数量
        */
        private Integer inboundWorkingQty;

        /**
        * 已发货数量
        */
        private Integer inboundShippedQty;

        /**
        * 入库中数量
        */
        private Integer inboundReceivingQty;

        /**
        * FBI可售
        */
        private Integer fulfillableQty;

        /**
        * 预留
        */
        private Integer reservedQty;

        /**
        * 调查中数量
        */
        private Integer researchingQty;

        /**
        * 不可售数量
        */
        private Integer unsellableQty;

        /**
        * 库龄
        */
        private String inventoryAge;


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
        * 平台唯一id
        */
        @NotBlank(message = "平台唯一id不能为空")
        @Size(max = 255,message = "平台唯一id最大长度不能超过255位")
        private String platformCode;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 255,message = "仓库名称最大长度不能超过255位")
        private String name;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        @Size(max = 64,message = "平台sku最大长度不能超过64位")
        private String asin;

        /**
        * 卖家sku
        */
        @NotBlank(message = "卖家sku不能为空")
        @Size(max = 64,message = "卖家sku最大长度不能超过64位")
        private String msku;

        /**
        * FNSKU
        */
        @NotBlank(message = "FNSKU不能为空")
        @Size(max = 64,message = "FNSKU最大长度不能超过64位")
        private String fnSku;

        /**
        * ERP的SKU
        */
        @NotBlank(message = "ERP的SKU不能为空")
        @Size(max = 64,message = "ERP的SKU最大长度不能超过64位")
        private String skuNo;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 500,message = "产品名称最大长度不能超过500位")
        private String productName;

        /**
        * 配送渠道
        */
        @NotBlank(message = "配送渠道不能为空")
        @Size(max = 64,message = "配送渠道最大长度不能超过64位")
        private String deliveryChannels;

        /**
        * FBM可售
        */
        @NotNull(message = "FBM可售不能为空")
        private Integer fbmFulfillableQty;

        /**
        * 计划入库数量
        */
        @NotNull(message = "计划入库数量不能为空")
        private Integer inboundWorkingQty;

        /**
        * 已发货数量
        */
        @NotNull(message = "已发货数量不能为空")
        private Integer inboundShippedQty;

        /**
        * 入库中数量
        */
        @NotNull(message = "入库中数量不能为空")
        private Integer inboundReceivingQty;

        /**
        * FBI可售
        */
        @NotNull(message = "FBI可售不能为空")
        private Integer fulfillableQty;

        /**
        * 预留
        */
        @NotNull(message = "预留不能为空")
        private Integer reservedQty;

        /**
        * 调查中数量
        */
        @NotNull(message = "调查中数量不能为空")
        private Integer researchingQty;

        /**
        * 不可售数量
        */
        @NotNull(message = "不可售数量不能为空")
        private Integer unsellableQty;

        /**
        * 库龄
        */
        @NotBlank(message = "库龄不能为空")
        @Size(max = 255,message = "库龄最大长度不能超过255位")
        private String inventoryAge;


    }

    /**
     * 列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 仓库id
         */
        private List<String> warehouseIdList;
        /**
         * 平台sku
         */
        private String asin;
        /**
         * 卖家sku
         */
        private String msku;
        /**
         * fnSku
         */
        private String fnSku;
        /**
         * 配送渠道：/wms/common/enumDropDown?type=DeliveryChannels
         *    self_delivery：卖家自配送
         *    amazon_delivery：亚马逊配送
         */
        private List<String> deliveryChannelsList;
        /**
         * 更新时间
         */
        private List<String> updateTimeList;
        /**
         * 是否显示0库存
         */
        private Boolean isShowZeroInventory;
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
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 平台唯一编号
         */
        private String fbaShipmentId;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 平台sku
         */
        private String asin;

        /**
         * 卖家sku
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
         * 配送渠道：/wms/common/enumDropDown?type=DeliveryChannels
         *    self_delivery：卖家自配送
         *    amazon_delivery：亚马逊配送
         */
        private String deliveryChannels;

        /**
         * 销售渠道名称
         */
        private String deliveryChannelsName;

        /**
         * FBM可售
         */
        private String fbmFulfillableQty;

        /**
         * 计划入库数量
         */
        private Integer inboundWorkingQty;

        /**
         * 已发货数量
         */
        private Integer inboundShippedQty;

        /**
         * 入库中数量
         */
        private Integer inboundReceivingQty;

        /**
         * FBI可售
         */
        private Integer fulfillableQty;

        /**
         * 预留
         */
        private Integer reservedQty;

        /**
         * 调查中数量
         */
        private Integer researchingQty;

        /**
         * 不可售数量
         */
        private Integer unsellableQty;

        /**
         * 库龄 0-30 天的可售商品数量
         */
        private Integer inventoryAge0To30Days;

        /**
         * 库龄 31-60 天的可售商品数量
         */
        private Integer inventoryAge31To60Days;

        /**
         * 库龄 61-90 天的可售商品数量
         */
        private Integer inventoryAge61To90Days;

        /**
         * 库龄 91-180 天的可售商品数量
         */
        private Integer inventoryAge91To180Days;

        /**
         * 库龄 181-270 天的可售商品数量
         */
        private Integer inventoryAge181To270Days;

        /**
         * 库龄 271-365 天的可售商品数量
         */
        private Integer inventoryAge271To365Days;

        /**
         * 库龄 365 天以上的可售商品数量
         */
        private Integer inventoryAge365PlusDays;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

    }

    /**
     * 列表汇总数量
     */
    @Data
    @NoArgsConstructor
    public static class SummaryNumber {
        /**
         * FBM可售
         */
        private String fbmFulfillableQty;

        /**
         * 计划入库数量
         */
        private Integer inboundWorkingQty;

        /**
         * 已发货数量
         */
        private Integer inboundShippedQty;

        /**
         * 入库中数量
         */
        private Integer inboundReceivingQty;

        /**
         * FBI可售
         */
        private Integer fulfillableQty;

        /**
         * 预留
         */
        private Integer reservedQty;

        /**
         * 调查中数量
         */
        private Integer researchingQty;

        /**
         * 不可售数量
         */
        private Integer unsellableQty;
    }

    /**
     * 导出Excel
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends FbaInventoryDTO.PagingParamDTO {
        /**
         * 勾选的id集合
         */
        private List<String> ids;
    }

    /**
     * 查询预留详情
     */
    @Data
    @NoArgsConstructor
    public static class InventoryReservedView {
        /**
         * 主键id
         */
        private String id;
        /**
         * 待调仓数量
         */
        private Integer reservedTransfersQty;
        /**
         * 调仓中数量
         */
        private Integer reservedProcessingQty;
        /**
         * 买家订单数量
         */
        private Integer reservedOrderQty;
    }

    /**
     * 所有批量新增
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllBatchSaveDTO {

        /**
         * FBA库存主信息
         */
        private List<FbaInventoryEntity> fbaInventoryEntityList;

        /**
         * 预留信息列表
         */
        private List<FbaInventoryReservedEntity> fbaInventoryReservedEntityList;
    }

    @Data
    @NoArgsConstructor
    public static class QueryDTO{
        /**
         * 仓库id列表
         */
        private List<String> warehouseIds;
        /**
         * sku列表
         */
        private List<String> skuNos;
    }
    @Data
    @NoArgsConstructor
    public static class InventoryDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * sku
         */
        private String skuNo;
        /**
         * 平台sku
         */
        private String asin;
        /**
         * 卖家sku
         */
        private String msku;
        /**
         * FNSKU
         */
        private String fnSku;
        /**
         * 产品名称
         */
        private String platformProductName;
        /**
         * FBM可售
         */
        private Integer fbmFulfillableQty;
        /**
         * FBA可售
         */
        private Integer fulfillableQty;

    }
}
