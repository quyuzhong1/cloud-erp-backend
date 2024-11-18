package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
     * 通用映射基础
     */
    @Data
    @NoArgsConstructor
    public static class BaseMapping {

        /**
         * 对照关系是否映射到改服务商所有仓库(当前只有谷仓支持): f=否(), t=是
         */
        private Boolean hasMappingAll;

        /**
         * 检查和获取：
         * 对照关系是否映射到改服务商所有仓库(当前只有谷仓支持): f=否(), t=是
         */
        public Boolean checkAndGetHasMappingAll() {
            if (null == this.hasMappingAll){
                return false;
            }
            return hasMappingAll;
        }

    }




    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {

        private String tabFlag;
        /**
         * 类型名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
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
    public static class AddWarehouseSkuDTO extends BaseMapping {

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
        /**
         * 生效时间
         */
        @NotNull(message = "生效时间不能为空")
        private LocalDateTime effectiveTime;
//        /**
//         * 平台类型: goodcang=谷仓，iml=艾姆勒
//         */
//        @NotBlank(message = "平台类型: goodcang=谷仓，iml=艾姆勒不能为空")
//        @Size(max = 30,message = "平台类型: goodcang=谷仓，iml=艾姆勒 最大长度不能超过30位")
//        private String dictPlatform;

    }

    @Data
    @NoArgsConstructor
    public static class MatchCountDTO {
        /**
         * 匹配结果
         */
        private String matchResult;

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
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        private String type;

    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class WarehousePagingParamDTO extends SortDTO {


        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        private String type;

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
         * 生效时间
         */
        @NotNull(message = "生效时间不能为空")
        private LocalDateTime effectiveTime;
        /**
         * 平台sku no
         */
//        @NotBlank(message = "平台sku不能为空")
        @NotNull(message = "平台sku不能为null")
        @Size(max=200,message = "平台SKU最大100字符")
        private String platformSkuNo;

        /**
         * 平台sku 名
         */
        private String platformProductName;


        private List<SkuMappingExtendListDTO> extendList;



    }

    /**
     * 列表DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuMappingExtendListDTO {
        /**
         * 仓库经营类型:selfBuild=自建,thirdParty=第三方
         * 对应来源{@link /api/wms/common/enumDropDown?type=WarehouseManageTypeEnum}
         */
        private String warehouseManageType;

        /**
         * 发货类型:single=子件发货,combine=捆绑Sku发货
         * 对应来源{@link /api/wms/common/enumDropDown?type=WarehouseDeliveryType}
         */
        private String warehouseDeliveryType;
    }


    /**
     * 更改库存SKU
     */
    @Data
    @NoArgsConstructor
    public static class UpdateWarehouseSkuDTO extends BaseMapping {

        /**
         * id
         */
        @NotBlank(message = "sku对照不存在")
        private String id;



        /**
         * 仓库id
         */
//        @NotBlank(message = "仓库不能为空")
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
        /**
         * 生效时间
         */
        @NotNull(message = "生效时间不能为空")
        private LocalDateTime effectiveTime;

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
         * 销售员
         */
        private String sellerName;

        /**
         * 备注
         */
        private String remark;
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
        private String matchResult;

        /**
         * 匹配结果
         */
        private String matchResultStr;



        /**
         * 更新人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;
        /**
         * 启用时间
         */
        private LocalDateTime effectiveTime;
        /**
         * 失效时间
         */
        private LocalDateTime expireTime;

        /**
         * 平台产品SPU编号
         */
        private String platformSpuNo;

        /**
         * 平台产品(SPU)名称
         */
        private String platformSpuName;

        /**
         * 平台SKU额外关联的FNSKU
         */
        private String platformFnSku;
        /**
         * 是否是捆绑商品:true=是，false=否
         */
        private Boolean isCombination;
        /**
         * 仓库发货配置
         */
        private List<SkuMappingExtendDTO.ListDTO> extendList = Collections.emptyList();

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
         * 备注
         */
        private String remark;
        /**
         * 仓库简称
         */
        private String shortName;

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
        private String matchResult;

        /**
         * 匹配结果
         */
        private String matchResultStr;


        /**
         * 更新人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;

        /**
         * 平台
         */
        private String dictPlatform;

        /**
         * 平台名称
         */
        private String platformName;

        /**
         * 对照关系是否映射到改服务商所有仓库
         */
        private String hasMappingAllStr;

        /**
         * 对照关系是否映射到改服务商所有仓库
         */
        private Boolean hasMappingAll;
        /**
         * 启用时间
         */
        private LocalDateTime effectiveTime;
        /**
         * 失效时间
         */
        private LocalDateTime expireTime;
    }

    @Data
    @NoArgsConstructor
    public static class ProductSkuInfoDTO{

        /**
         *
         */
        private String id;
        /**
         *
         */
        private String shopId;
        /**
         *
         */
        private String customerId;
        /**
         *
         */
        private String listingId;
        /**
         *
         */
        private String dictPlatform;
        /**
         *
         */
        private String platformName;

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
    @Builder
    public static class ListSkuParamDTO {


        /**
         * SKU编号
         */
        private String skuNo;

        /**
         * 出库id
         */
        private String warehouseId;

        /**
         * 平台字典
         */
        private String dictPlatform;
        /**
         * 店铺Id
         */
        private String shopId;

        public ListSkuParamDTO(String skuNo, String warehouseId, String dictPlatform) {
            this.skuNo = skuNo;
            this.warehouseId = warehouseId;
            this.dictPlatform = dictPlatform;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListingSkuParamDTO{
        private String type;

        private String skuId;

        /**
         * SKU编号
         */
        private String skuNo;

        /**
         * 出库id
         */
        private String warehouseId;

        /**
         * 平台字典
         */
        private String dictPlatform;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListSkuResultDTO {

        private String skuId;

        /**
         * SKU编号
         */
        private String skuNo;

        /**
         * 出库id
         */
        private String warehouseId;

        /**
         * 平台字典
         */
        private String dictPlatform;

        private String listingId;



        private String type;



        /**
         * 平台产品sku
         */
        private String platformSkuNo;

        /**
         * 平台产品sku
         */
        private String platformSkuName;

        /**
         * 平台产品id
         */
        private String platformSpuNo;

        /**
         * 平台产品名
         */
        private String platformSpuName;
    }




    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MappingSkuParam {

        /**
         * ERP的skuId列表
         */
        private List<String> skuIdList;
    }


    @Data
    @NoArgsConstructor
    public static class WarehouseSkuDTO {
        private String tableId;
        private String dictPlatform;
        private String productSkuId;
        private String productSkuNo;
        private String productName;
        private String type;
        private String warehouseId;
        private String warehouseName;
        private String isExpire;
        private String listingId;
        private String platformSkuNo;
        private String platformSkuName;
        private String platformSpuNo;
        private String platformSpuName;
        private String platformFnSku;
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
         * 平台产品id
         */
        private String platformSpuNo;


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

        /**
         * 平台字典
         */
        private String dictPlatform;
        /**
         * 产品尺寸（长）
         */
        private BigDecimal productLength;

        /**
         * 产品尺寸（宽）
         */
        private BigDecimal productWidth;

        /**
         * 产品尺寸（高）
         */
        private BigDecimal productHeight;

        /**
         * 毛重
         */
        private BigDecimal grossWeight;
        /**
         * 净重
         */
        private BigDecimal netWeight;
    }


    @Data
    @NoArgsConstructor
    public static class MappingSkuViewDTO {

        private String id;

        private String authId;
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
        private String platformSkuNo;

        /**
         * 库存sku名称
         */
        private String platformProductName;

        /**
         * 平台产品id
         */
        private String platformSpuNo;

        /**
         * 平台产品名称
         */
        private String platformSpuName;
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

        private String type;


    }

    /**
     * 库存sku对照产品sku返回值
     */
    @Data
    @NoArgsConstructor
    public static class ListStockSkuNoByProductSkuIdView {
        /**
         * 产品skuId
         */
        private String listingId;
        /**
         * 产品skuId
         */
        private String productSkuId;
        /**
         * 产品sku编号
         */
        private String productSkuNo;
        /**
         * 产品sku名称
         */
        private String productSkuName;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 平台代号
         */
        private String dictPlatform;
        /**
         * 库存sku
         */
        private String stockSku;
        /**
         * 库存产品名称
         */
        private String stockSkuName;
        /**
         * 对照关系是否映射到服务商平台所有仓库: f=否, t=是
         */
        private Boolean hasMappingAll;
    }

    /**
     * 添加对照表
     */
    @Data
    @NoArgsConstructor
    public static class UpdateSkuMappingDTO {
        /**
         * 产品sku id
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
         *
         */
        private String listingId;

        private Boolean isExpire;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateNotMatchDTO {
        /**
         * listing id
         */
        @NotEmpty(message = "数据不能为空")
        private List<String> listingIds;
        /**
         * 备注
         */
        @NotBlank(message = "备注不能为空")
        private String remark;
    }


    @Data
    @NoArgsConstructor
    public static class SyncPlatformProductView {
        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 平台
         */
        private String dictPlatform;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 授权状态
         */
        private String authStatus;
        /**
         * 授权状态
         */
        private String authStatusName;
        /**
         * 最近同步时间
         */
        private LocalDateTime lastSyncTime;
        /**
         * 同步结果
         */
        private String syncResult;
    }

    @Data
    @NoArgsConstructor
    public static class SyncWarehouseProductView {
        /**
         * 授权id
         */
        private String authId;
        /**
         * 三方仓服务商code
         */
        private String warehouseProvideCode;
        /**
         * 三方仓服务商名称
         */
        private String warehouseProvideName;
        /**
         * 账号
         */
        private String account;

        /**
         * 授权状态
         */
        private String authStatus;

        /**
         * 授权状态Name
         */
        private String authStatusName;
        /**
         * 最近同步时间
         */
        private LocalDateTime lastSyncTime;
        /**
         * 同步结果
         */
        private String syncResult;
    }


    @Data
    @NoArgsConstructor
    public static class SkuParamDTO {
        /**
         * 平台sku
         */
        private List<String> platformSkuNoList;
        /**
         * sku编号
         */
        private List<String> skuNoList;
        /**
         * skuid
         */
        private List<String> skuIdList;
        /**
         * 客户id
         */
        private String cutomerId;
    }

}
