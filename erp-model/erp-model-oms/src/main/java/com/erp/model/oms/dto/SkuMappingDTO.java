package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import com.common.core.anno.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
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
            if (null == this.hasMappingAll) {
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
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class FindTabDTO extends PermissionsDTO {

        @StateEnumValue(strValues = {"platform","b2bPlatform", "warehouse","customer"}, message = "类型有误")
        private String type;


    }

    @Data
    @NoArgsConstructor
    public static class AddWarehouseSkuDTO extends BaseMapping {

        private String warehouseId;

        @NotBlank(message = "库存SKU不能为空")
        @Size(max = 200, message = "库存SKU最大100字符")
        private String warehouseSkuNo;

        @NotBlank(message = "库存产品名称不能为空")
        @Size(max = 200, message = "库存产品名称最大200字符")
        private String warehouseProductName;

        @NotBlank(message = "产品sku不能为空")
        private String productSkuId;
        /**
         * 生效时间
         */
        @NotNull(message = "生效时间不能为空")
        private LocalDateTime effectiveTime;
        /**
         * 授权Id
         */
        private String authId;
        /**
         * 产品条码（三方仓商品条码）
         */
        private String thirdBarcode;
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
        private Map<String, String> sqlMap;

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
        private Map<String, String> sqlMap;

        private String type;

    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class AddCustomerRequest {
        /**
         * json数据
         */
        @Valid
        private AddCustomerDTO dto;
        /**
         * 文件
         */
        private MultipartFile file;
    }
    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class AddCustomerDTO {

        /**
         * skuMappingId
         */
        @NotBlank(message = "skuMappingId不能为空",groups = {UpdateGroup.class})
        private String skuMappingId;
        /**
         * 客户id
         */
        @NotBlank(message = "客户id不能为空",groups = {UpdateGroup.class, AddGroup.class})
        private String customerId;

        /**
         * skuId
         */
        @NotBlank(message = "skuid不能为空",groups = {UpdateGroup.class, AddGroup.class})
        private String skuId;

        /**
         * 客户sku
         */
        @NotBlank(message = "客户sku不能为空",groups = {UpdateGroup.class, AddGroup.class})
        private String platformSkuNo;
        /**
         * 生效时间
         */
        @NotNull(message = "生效时间不能为空",groups = {UpdateGroup.class, AddGroup.class})
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime effectiveTime;

        /**
         * 客户产品名称
         */
        private String platformSkuName;
        /**
         * 图片url，更新时如果没变更传
         */
        private String productImageUrl;
        /**
         * 标签url
         */
        private String labelUrl;
        /**
         * 标签文件名称
         */
        private String labelFileName;
        /**
         * 标签来源
         * 字段值流转【后端使用】
         */
        private String labelSourceType;

        /**
         * 平台状态
         */
        private String platformStatus;
    }
    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class CustomerPagingParamDTO extends SortDTO {


        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        private String type;

        private boolean isExport;
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
        @Size(max = 200, message = "平台SKU最大100字符")
        private String platformSkuNo;
        /**
         * 平台产品ID
         */
        private String platformSpuNo;
        /**
         * 平台sku 名
         */
        private String platformProductName;


        private List<SkuMappingExtendListDTO> extendList;
        /**
         * 同账号同平台SKU批量更新 默认 true  false 不更新
         */
        private Boolean batchUpdateSamePlatform;
        /**
         * 税务信息
         */
        private TaxCodeDTO taxCodeDTO;

        /**
         * 平台状态
         */
        private String platformStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxCodeDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * listingId
         */
        private String listingId;
        /**
         * 发票海关编码（ncm）
         */
        private String invoiceHsCode;
        /**
         * 单位
         */
        private String unit;
        /**
         * 跨州税务编码（跨州cfop）
         */
        private String diffStateTaxCode;
        /**
         * 同州税务编码（同州cfop）
         */
        private String sameStateTaxCode;
        /**
         * 原产地
         */
        private String dictOrigin;
        /**
         * 开票产品名称
         */
        private String invoiceProductName;
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
        @Size(max = 200, message = "库存SKU最大100字符")
        private String warehouseSkuNo;

        /**
         * 库存产品名称
         */
        @NotBlank(message = "库存产品名称不能为空")
        @Size(max = 200, message = "库存产品名称最大200字符")
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
        /**
         * 产品条码（三方仓商品条码）
         */
        private String thirdBarcode;
        /**
         * 授权Id
         */
        private String authId;

        /**
         * 平台状态
         */
        private String platformStatus;

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
         * 系统创建时间
         */
        private LocalDateTime systemCreateTime;


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
         * 创建人名称
         */
        private String createUserName;

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
         * 来源类型  selfAdd系统新增，third第三方同步
         */
        private String sourceType;
        /**
         * 仓库发货配置
         */
        private List<SkuMappingExtendDTO.ListDTO> extendList = Collections.emptyList();
        /**
         * 发票海关编码
         */
        private String invoiceHsCode;
        /**
         * 单位
         */
        private String unit;
        /**
         * 跨州税务编码
         */
        private String diffStateTaxCode;
        /**
         * 同州税务编码
         */
        private String sameStateTaxCode;
        /**
         * 原产地
         */
        private String dictOrigin;
        /**
         * 原产地名称
         */
        private String dictOriginName;
        /**
         * 开票产品名称
         */
        private String invoiceProductName;
        /**
         * 平台状态
         */
        private String platformStatus;
        private String platformStatusName;
        private String platformParentSpuNo;

    }


    /**
     * 客户sku分页数据
     */
    @Data
    @NoArgsConstructor
    public static class CustomerPagingViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * listingId
         */
        private String listingId;

        /**
         * 平台名称
         */
        private String platformName;

        /**
         * 客户Id
         */
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;

        /**
         * 销售员
         */
        private String sellerName;

        /**
         * 图片
         */
        private String productImageUrl;
        /**
         * 图片byte
         */
        private byte[] imageByte;
        /**
         * 客户sku
         */
        private String platformSkuNo;

        /**
         * 客户产品名称
         */
        private String platformSkuName;

        /**
         * 产品skuId
         */
        private String productSkuId;

        /**
         * 产品sku
         */
        private String productSkuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 匹配结果
         */
        private String matchResult;

        /**
         * 匹配结果
         */
        private String matchResultStr;

        /**
         * 备注
         */
        private String remark;
        /**
         * 标签链接
         */
        private String labelUrl;
        private String labelUrlStr;
        /**
         * 标签文件名称
         */
        private String labelFileName;
        /**
         * 标签来源类型
         * LabelSourceTypeEnum
         */
        private String labelSourceType;
        /**
         * 是否 上传附件 true 是 false 否
         */
        private Boolean isUploadLabel;
        /**
         * 上传附件
         */
        private String uploadLabelStr;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        /**
         * 更新人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;

        private LocalDateTime effectiveTime;

        /**
         * 平台状态
         */
        private String platformStatus;
        private String platformStatusName;
        private String platformParentSpuNo;
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
         * authId
         */
        private String authId;

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
         * 库存产品id
         */
        private String platformSkuId;

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
         * 三方仓商品条码
         */
        private String thirdBarcode;

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

        /**
         * 平台状态
         */
        private String platformStatus;
        private String platformStatusName;
        private String platformParentSpuNo;
    }


    @Data
    @NoArgsConstructor
    public static class CustomerInventorySkuInfoDTO {

        /**
         * 客户sku
         */
        private String platformSkuNo;

        /**
         * 对应库存信息
         */
        private List<InnerCustomerInventorySkuInfoDTO> innerCustomerInventorySkuInfoDTOS;
    }

    @Data
    @NoArgsConstructor
    public static class InnerCustomerInventorySkuInfoDTO {

        /**
         * skumapping Id
         */
        private String id;

        /**
         * skuId
         */
        private String skuId;

        /**
         * skuId
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 是否有效
         */
        private Boolean isEffective;

        /**
         * 启用时间
         */
        private LocalDateTime effectiveTime;

        /**
         * 实体仓实际库存
         */
        private Integer actualQty;

        /**
         * 虚拟仓冻结库存
         */
        private Integer virtualFrozenQty;

        /**
         * 实体仓实际库存 - 虚拟仓冻结库存
         */
        private Integer stock;

    }
    @Data
    @NoArgsConstructor
    public static class ProductSkuInfoDTO {

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
        /**
         * 订单日期
         */
        private LocalDateTime billDate;

        public ListSkuParamDTO(String skuNo, String warehouseId, String dictPlatform) {
            this.skuNo = skuNo;
            this.warehouseId = warehouseId;
            this.dictPlatform = dictPlatform;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListingSkuParamDTO {
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
         * 平台产品sku id
         */
        private String platformSkuId;
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
         * 店铺id
         */
        private String shopId;
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

        /**
         * 成本价格来源
         */
        private String costSource;
        //材料成本
        private BigDecimal productCost;
        //头程运费
        private BigDecimal firstMileShippingCost;
        //清关税费
        private BigDecimal clearanceCustomsTax;
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
         * 库存sku id
         */
        private String platformSkuId;

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
    public static class CustomerInventorySkuParamDTO {


        /**
         * 客户id
         */
        @NotBlank(message = "客户id不能为空")
        private String customerId;

        /**
         * 客户Sku
         */
        @NotEmpty(message = "平台sku不能为空")
        private List<String> platformSkuNoList;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id不能为空")
        private String warehouseId;

        /**
         * 虚拟仓库id
         */
        private String virtualWarehouseId;
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
    public static class SkuMappingViewDTO {
        private String mappingId;
        private String dictPlatform;
        private String productSkuId;
        //产品SKU
        private String productSkuNo;
        private String productName;
        private String type;
        private String warehouseId;
        private String warehouseName;
        private String isExpire;
        private String listingId;
        //客户id
        private String customerId;
        //平台SKU
        private String platformSkuNo;
        private String platformSkuName;
        private String platformName;
        //条码
        private String thirdBarcode;
        private String platformSpuNo;
        private String platformSpuName;
        private String platformFnSku;
        /**
         * 服务商简称
         */
        private String shortName;
    }

    @Data
    @NoArgsConstructor
    public static class CustomerLabelDTO {
        /**
         * 客户sku
         */
        @NotBlank(message = "客户sku不能为空")
        private String platformSkuNo;
        /**
         * 标签url
         */
        @NotBlank(message = "标签url不能为空")
        private String labelUrl;
        /**
         * 标签文件名称
         */
        private String labelFileName;
    }


    /**
     * 分页数据
     */
    @Data
    @NoArgsConstructor
    public static class SyncSkuDTO {
        /**
         * 开始时间
         */
        @NotNull(message = "开始时间不能为空")
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        @NotNull(message = "结束时间不能为空")
        private LocalDateTime endTime;
    }
}
