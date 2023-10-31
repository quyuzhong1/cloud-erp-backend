package com.erp.model.oms.dto;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname SkuMapingDTO

 * @Date 2023-06-28 17:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SkuMappingDTO implements Serializable {


    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {

        private String tabFlag;

        private Integer count;

    }

    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class FindTabDTO extends PermissionsDTO {

        @StateEnumValue(strValues = {"platform","warehouse"}, message = "类型有误")
        private String type;


    }

    @Data
    @NoArgsConstructor
    public static class AddWarehouseSkuDTO{

        @NotBlank(message = "仓库id不能为空")
        private String warehouseId;

        @NotBlank(message = "库存SKU不能为空")
        @Size(max=200,message = "库存SKU最大100字符")
        private String warehouseSkuNo;

        @NotBlank(message = "库存产品名称不能为空")
        @Size(max=200,message = "库存产品名称最大200字符")
        private String warehouseProductName;

        @NotBlank(message = "产品sku不能为空")
        private String productSkuId;

    }

    @Data
    @NoArgsConstructor
    public static class MatchCountDTO {
        /**
         * 匹配结果
         */
        private Boolean matchResult;

        /**
         * 数量
         */
        private Integer count;
    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {


        /**
         * 搜索关键字
         */
        private String searchKeyword;

        private String type;


        /**
         * 店铺id集合
         */
        private List<String> shopIdList;

        /**
         * sku集合
         */
        private List<String> skuNoList;

        /**
         * 搜索类型
         * alL 全部
         * already 已匹配
         * not 未匹配
         */
        @StateEnumValue(strValues = {"all", "already", "not"}, message = "搜索类型有误")
        @NotBlank(message = "搜索类型不能为空")
        private String tabFlag;


        /**
         * 平台code 集合
         */
        private List<String> platformList;


        /**
         * 创建人 id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;


    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class WarehousePagingParamDTO extends SortDTO {

        private String type;

        /**
         * 库存产品名称
         */
        private String warehouseProductName;

        /**
         * 仓库id 集合
         */
        private List<String> warehouseIdList;


        /**
         * 库存sku no 集合
         */
        private List<String> warehouseSkuNoList;


        /**
         * 产品skuNO集合
         */
        private List<String> productSkuNoList;

        /**
         * sku id list
         */
        private List<String> skuIdList;


        /**
         * 产品名称
         */
        private String productName;

        /**
         * 搜索类型
         * alL 全部
         * already 已匹配
         * not 未匹配
         */
        @StateEnumValue(strValues = {"all", "already", "not"}, message = "搜索类型有误")
        @NotBlank(message = "搜索类型不能为空")
        private String tabFlag;





        /**
         * 创建人 id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;


    }


    /**
     * 更改sku
     */
    @Data
    @NoArgsConstructor
    public static class UpdatePlatformDTO {

        /**
         * id
         */
        @NotBlank(message = "sku对照不存在")
        private String id;


        /**
         * 平台
         */
        @NotBlank(message = "平台不能为空")
        private String dictPlatform;

        /**
         * 店铺
         */
        private String shopId;

        /**
         * 产品sku
         */
        @NotBlank(message = "产品sku不能为空")
        private String productSkuId;

        /**
         * 平台sku no
         */
        @NotBlank(message = "平台sku不能为空")
        @Size(max=200,message = "平台SKU最大100字符")
        private String platformSkuNo;

        /**
         * 平台sku 名
         */
        private String platformProductName;



    }

    /**
     * 更改库存SKU
     */
    @Data
    @NoArgsConstructor
    public static class UpdateWarehouseSkuDTO {

        /**
         * id
         */
        @NotBlank(message = "sku对照不存在")
        private String id;



        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 库存sku no
         */
        @NotBlank(message = "库存sku不能为空")
        @Size(max=200,message = "库存SKU最大100字符")
        private String warehouseSkuNo;

        /**
         * 库存产品名称
         */
        @NotBlank(message = "库存产品名称不能为空")
        @Size(max=200,message = "库存产品名称最大200字符")
        private String warehouseProductName;


        /**
         * 产品sku
         */
        @NotBlank(message = "产品sku不能为空")
        private String productSkuId;


    }

    /**
     * 导出sku 对照表
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        private List<String> ids;
    }

    /**
     * 导出sku 对照表
     */
    @Data
    @NoArgsConstructor
    public static class ExportWarehouseSkuDTO extends WarehousePagingParamDTO {
        private List<String> ids;
    }

    /**
     * 分页数据
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 平台名称
         */
        private String platformName;


        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * listingId
         */
        private String listingId;

        /**
         * 产品图片url
         */
        private String productImageUrl;

        /**
         * 产品规格
         */
        private String productSpec;

        /**
         * 产品包装
         */
        private String productPacking;


        /**
         * 平台sku no
         */
        private String platformSkuNo;

        /**
         * 平台产品名称
         */
        private String platformProductName;

        /**
         * 平台更新时间
         */
        private LocalDateTime platformUpdateTime;


        /**
         * 产品skuId
         */
        private String productSkuId;


        /**
         * 产品sku no
         */
        private String productSkuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 卖家sku no
         */
        private String sellerSkuNo;

        /**
         * 卖家sku 名称  或者 库存sku 名称
         */
        private String sellerProductName;

        /**
         * 匹配结果
         */
        private Boolean matchResult;

        /**
         * 匹配结果
         */
        private String matchResultStr;



        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;


    }


    /**
     * 分页数据
     */
    @Data
    @NoArgsConstructor
    public static class WarehousePagingViewDTO {

        /**
         * id
         */
        private String id;




        /**
         * listingId
         */
        private String listingId;

        /**
         * 仓库id
         */
        private String warehouseId;


        /**
         * 仓库名称
         */
        private String warehouseName;


        /**
         * 产品skuId
         */
        private String productSkuId;


        /**
         * 产品sku no
         */
        private String productSkuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 库存sku
         */
        private String warehouseSkuNo;

        /**
         * 库存产品名称
         */
        private String warehouseProductName;

        /**
         * 匹配结果
         */
        private Boolean matchResult;

        /**
         * 匹配结果
         */
        private String matchResultStr;



        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;


    }

    @Data
    @NoArgsConstructor
    public static class ProductSkuInfoDTO{

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        private String skuName;

        /**
         * 图片地址
         */
        private String imagesUrl;

        /**
         * 平台sku
         */
        private String platformSkuNo;

        /**
         * 平台sku
         */
        private String platformSkuName;


    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class ListParamDTO extends SortDTO {


        /**
         * sku编号
         */
        private String no;


        @NotBlank(message = "客户不能为空")
        private String customerId;


        private List<String> skuNoList;


    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListSkuParamDTO {
        /**
         * SKU编号
         */
        @NotBlank(message = "SKU不能为空")
        private String skuNo;

        /**
         * 出库id
         */
        private String warehouseId;

    }


    @Data
    @NoArgsConstructor
    public static class ListSkuDTO {
       /**
        * 产品skuId
        */
       private String productSkuId;

        /**
         * 产品skuNo
         */
        private String productSkuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 库存sku
         */
        private String warehouseSkuNo;

        /**
         * 库存产品名称
         */
        private String warehouseProductName;

        /**
         * 平台sku no
         */
        private String platformSkuNo;

        /**
         * 平台产品名称
         */
        private String platformProductName;

        /**
         * 建议售价（本位币）
         */
        private BigDecimal advicePrice;
        /**
         * 含税成本（本位币）
         */
        private BigDecimal taxCost;

        /**
         * 变体信息
         */
        private String variantProperty;
        /**
         * 图片url
         */
        private String imageUrl;

        /**
         * 仓库id
         */
        private String warehouseId;
    }


    @Data
    @NoArgsConstructor
    public static class SkuDTO {
        /**
         * 产品skuId
         */
        private String productSkuId;

        /**
         * 产品skuNo
         */
        private String productSkuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 库存sku
         */
        private String flagSkuNo;

        /**
         * 库存产品名称
         */
        private String flagProductName;

        /**
         * 平台sku no
         */
        private String platformSkuNo;

        /**
         * 平台产品名称
         */
        private String platformProductName;


    }
}
