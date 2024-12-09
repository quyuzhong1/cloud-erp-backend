package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 海外仓库存请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
*/
@Data
@NoArgsConstructor
public class OverseasInventoryDTO implements Serializable {

    /**
     * 列表查询入参
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {

        private List<String> ids;
    }


    /**
     * 列表查询入参
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * ids
         */
        private List<String> ids;

        /**
         * 平台仓库编码
         */
        private String warehouseCode;

        /**
         * 仓库名称
         */
        private String name;

        /**
         * ERP SKU列表
         */
        private List<String> skuNoList;

        /**
         * 库存SKU列表
         */
        private List<String> platformSkuNoList;

        /**
         * 更新时间
         */
        private List<LocalDate> updateTimeList;

        /**
         * 是否不显示0库存: true=不显示0库存, false=显示0库存
         */
        private Boolean hasNotZero;

        /**
         * 仓库ID列表
         */
        private List<String> warehouseIdList;

        /**
         * 平台仓库代号列表
         */
        private List<String> platformWarehouseCodeList;

        private Boolean sortFlag = false;
    }


    /**
     * 列表查询入参
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String  id;

        /**
         * 平台仓库编码
         */
        private String warehouseCode;

        /**
         * 平台类型: goodcang=谷仓，iml=艾姆勒
         */
        private String dictPlatform;

        /**
         * 仓库名称
         */
        private String name;

        /**
         * 库存sku
         */
        private String platformSku;

        /**
         * 库存产品名称
         */
        private String platformSkuName;

        /**
         * ERP系统产品名称
         */
        private String productName;

        /**
         * ERP的SKU
         */
        private String skuNo;

        /**
         * ERP的SKU ID
         */
        private String skuId;

        /**
         * 发货在途数量
         */
        private Integer deliverOnwayQty;

        /**
         * 待上架数量
         */
        private Integer pendingQty;

        /**
         * 可售数量
         */
        private Integer sellableQty;

        /**
         * 不可售数量
         */
        private Integer unsellableQty;

        /**
         * 待出库数量
         */
        private Integer reservedQty;

        /**
         * 尾程在途
         */
        private Integer onwayQty;

        /**
         * 缺货数量
         */
        private Integer lackQty;

        /**
         * 冻结数量
         */
        private Integer frozenQty;

        /**
         * 历史出库数量
         */
        private Integer shippedQty;

        /**
         * 平台下载更新时间 (更新时间)
         */
        private LocalDateTime downloadTime;

        /**
         * 库龄 0-30 天的可售商品数量
         */
        private Integer inventoryAge0To30Days = 0 ;

        /**
         * 库龄 31-60 天的可售商品数量
         */
        private Integer inventoryAge31To60Days = 0 ;

        /**
         * 库龄 61-90 天的可售商品数量
         */
        private Integer inventoryAge61To90Days = 0 ;

        /**
         * 库龄 91-180 天的可售商品数量
         */
        private Integer inventoryAge91To180Days = 0 ;

        /**
         * 库龄 181-270 天的可售商品数量
         */
        private Integer inventoryAge181To270Days = 0 ;

        /**
         * 库龄 271-365 天的可售商品数量
         */
        private Integer inventoryAge271To365Days = 0 ;

        /**
         * 库龄 365 天以上的可售商品数量
         */
        private Integer inventoryAge365PlusDays = 0 ;
    }


    /**
     * 列表查询入参
     */
    @Data
    @NoArgsConstructor
    public static class ListTotalDTO {

        /**
         * 发货在途数量
         */
        private Long deliverOnwayQty;

        /**
         * 待上架数量
         */
        private Long pendingQty;

        /**
         * 可售数量
         */
        private Long sellableQty;

        /**
         * 不可售数量
         */
        private Long unsellableQty;

        /**
         * 待出库数量
         */
        private Long reservedQty;

        /**
         * 尾程在途
         */
        private Long onwayQty;

        /**
         * 缺货数量
         */
        private Long lackQty;

        /**
         * 冻结数量
         */
        private Long frozenQty;

        /**
         * 历史出库数量
         */
        private Long shippedQty;
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
        * 平台仓库编码
        */
        private String warehouseCode;

        /**
        * 平台类型: goodcang=谷仓，iml=艾姆勒
        */
        private String dictPlatform;

        /**
        * 仓库名称
        */
        private String name;

        /**
        * 库存sku
        */
        private String platformSku;

        /**
        * 库存产品名称
        */
        private String platformSkuName;

        /**
        * ERP系统产品名称
        */
        private String productName;

        /**
        * ERP的SKU
        */
        private String skuNo;

        /**
        * ERP的SKU ID
        */
        private String skuId;

        /**
        * 发货在途数量
        */
        private Integer deliverOnwayQty;

        /**
        * 待上架数量
        */
        private Integer pendingQty;

        /**
        * 可售数量
        */
        private Integer sellableQty;

        /**
        * 不可售数量
        */
        private Integer unsellableQty;

        /**
        * 待出库数量
        */
        private Integer reservedQty;

        /**
        * 尾程在途
        */
        private Integer onwayQty;

        /**
        * 缺货数量
        */
        private Integer lackQty;

        /**
        * 冻结数量
        */
        private Integer frozenQty;

        /**
        * 历史出库数量
        */
        private Integer shippedQty;

        /**
        * 平台下载更新时间
        */
        private LocalDateTime downloadTime;


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
        * 平台仓库编码
        */
        @NotBlank(message = "平台仓库编码不能为空")
        @Size(max = 64,message = "平台仓库编码最大长度不能超过64位")
        private String warehouseCode;

        /**
        * 平台类型: goodcang=谷仓，iml=艾姆勒
        */
        @NotBlank(message = "平台类型: goodcang=谷仓，iml=艾姆勒不能为空")
        @Size(max = 30,message = "平台类型: goodcang=谷仓，iml=艾姆勒最大长度不能超过30位")
        private String dictPlatform;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 255,message = "仓库名称最大长度不能超过255位")
        private String name;

        /**
        * 库存sku
        */
        @NotBlank(message = "库存sku不能为空")
        @Size(max = 64,message = "库存sku最大长度不能超过64位")
        private String platformSku;

        /**
        * 库存产品名称
        */
        @NotBlank(message = "库存产品名称不能为空")
        @Size(max = 255,message = "库存产品名称最大长度不能超过255位")
        private String platformSkuName;

        /**
        * ERP系统产品名称
        */
        @NotBlank(message = "ERP系统产品名称不能为空")
        @Size(max = 255,message = "ERP系统产品名称最大长度不能超过255位")
        private String productName;

        /**
        * ERP的SKU ID
        */
        @NotBlank(message = "ERP的SKU ID不能为空")
        @Size(max = 255,message = "ERP的SKU ID最大长度不能超过255位")
        private String skuId;

        /**
        * 发货在途数量
        */
        @NotNull(message = "发货在途数量不能为空")
        private Integer deliverOnwayQty;

        /**
        * 待上架数量
        */
        @NotNull(message = "待上架数量不能为空")
        private Integer pendingQty;

        /**
        * 可售数量
        */
        @NotNull(message = "可售数量不能为空")
        private Integer sellableQty;

        /**
        * 不可售数量
        */
        @NotNull(message = "不可售数量不能为空")
        private Integer unsellableQty;

        /**
        * 待出库数量
        */
        @NotNull(message = "待出库数量不能为空")
        private Integer reservedQty;

        /**
        * 尾程在途
        */
        @NotNull(message = "尾程在途不能为空")
        private Integer onwayQty;

        /**
        * 缺货数量
        */
        @NotNull(message = "缺货数量不能为空")
        private Integer lackQty;

        /**
        * 冻结数量
        */
        @NotNull(message = "冻结数量不能为空")
        private Integer frozenQty;

        /**
        * 历史出库数量
        */
        @NotNull(message = "历史出库数量不能为空")
        private Integer shippedQty;

        /**
        * 平台下载更新时间
        */
        @NotNull(message = "平台下载更新时间不能为空")
        private LocalDateTime downloadTime;


    }


}