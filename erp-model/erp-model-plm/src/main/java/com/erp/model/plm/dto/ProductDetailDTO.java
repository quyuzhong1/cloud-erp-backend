package com.erp.model.plm.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 多规格sku信息请求参数
 * @Author: Luo_WG
 * @Date: 2022/9/21 15:46
 **/
@Data
@NoArgsConstructor
public class ProductDetailDTO implements Serializable {
    /**
     * 产品sku表id 无id：新增 有id：修改
     */
    private String id;

    /**
     * 产品表id
     */
    private String productId;

    /**
     * skuNo
     */
    private String skuNo;

    /**
     * 产品sku名称
     */
    @Size(max = 50, message = "产品名称最大50字符")
    private String name;

    /**
     * 产品sku名称(英文)
     */
    @Size(max = 500, message = "产品名称最大500字符")
    private String nameEn;

    /**
     * 变体属性
     */
    private String variantProperty;

    /**
     * 计划上市时间
     */
//    @NotNull(message = "预计上市时间不能为空")
    private LocalDate planListingTime;

    /**
     * 首批量产入库时间
     */
    private LocalDate firstMassProductDate;

    /**
     * 单位表id
     */
    private String unitId;

    /**
     * 产品状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    private Integer productState;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 修改人id
     */
    private String updateUserId;

    /**
     * sku图片
     */
    private String imagesUrl;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 产品负责人id
     */
    private String chargeId;

    /**
     * 产品负责人姓名
     */
    private String chargeName;

    /**
     * 是否已完成任务
     * 0 没有 1 已完成
     */
    private Integer isFinishTask;

    /**
     * 任务状态 0待审核，1审核中，2审核通过，3审核不通过
     */
    private Integer status;

    /**
     * 流程id
     */
    private String processId;

    /**
     * 流程表id
     */
    private String businessProcessId;

    private static final long serialVersionUID = 1L;


    /**
     * 搜索sku
     */
    @Data
    @NoArgsConstructor
    public static class SearchDTO {


        /**
         * 搜索关键字
         */
        private String searchKeyword;


        /**
         * 状态
         * 任务状态 0待审核，1审核中，2审核通过，3审核不通过，4待提交
         */
        private  Integer status;

    }

    @Data
    @NoArgsConstructor
    public static class SearchSkuDTO {

        /**
         * 搜索关键字
         */
        @NotBlank(message = "搜索关键字不能为空")
        private String searchKeyword;

        /**
         * 客户id
         */
        private String customerId;

    }

    /**
     * 采购员、供应商信息
     */
    @Data
    @NoArgsConstructor
    public static class PurchaseSupplierInfoDTO {


        /**
         * SKU ID
         */
        private String skuId;

        /**
         * 采购员id
         */
        private  String purchaseUserId;

        /**
         * 采购员名称
         */
        private  String purchaseUserName;

        /**
         * 供应商id
         */
        private  String supplierId;

        /**
         * 供应商名称
         */
        private  String supplierName;


    }


    /**
     * 搜索sku
     */
    @Data
    @NoArgsConstructor
    public static class PdaSearchDTO {

        /**
         * 搜索关键字
         */
        private String searchKeyword;

        /**
         * 精准sku查询
         */
        private String skuNo;

        /**
         * 状态
         */
        private Integer status;

    }


    /**
     * 产品物流信息
     */
    @Data
    @NoArgsConstructor
    public static class ProductLogisticDTO {

        /**
         * sku id
         */
        private String skuId;
        /**
         * sku no
         */
        private String skuNo;
        /**
         * 中国海关编码(商品编号)
         */
        private String customsCode;
        /**
         * 报关中文名（商品名称）
         */
        private String declareChineseName;
        /**
         * 申报要素
         */
        private String declareElement;
        /**
         * 报关单位
         */
        private String declareUnit;

        /**
         * 报关单位名称
         */
        private String declareUnitName;
        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 子sku数量(默认1),BOM用量
         */
        private Integer childQty = 1;

        /**
         * 子SKU集合
         */
        private List<ProductLogisticDTO> childList = new ArrayList<>();

        /**
         * 净重
         */
        private BigDecimal netWeight;
        /**
         * 报关币别
         */
        private String declareCurrency;

        /**
         * 报关币别名称
         */
        private String declareCurrencyName;
        /**
         * 报关币别符号
         */
        private String declareCurrencySymbol;
        /**
         * 原产国
         */
        private String sourceCountry;
        /**
         * 原产国名称
         */
        private String sourceCountryName;

        /**
         * 境内货源地
         */
        private String sourceCargo;
        /**
         * 征免
         */
        private String exemption;

        /**
         * 组合品申报类型
         */
        private String combinationDeclareType;

        /**
         * 是否组合品
         */
        private Boolean isCombination;

        /**
         * 保险属性
         */
        private String insuranceProperty;
    }

    /**
     * 产品信息
     */
    @Data
    @NoArgsConstructor
    public static class ProductDTO {

        /**
         * 产品id
         */
        private String productId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;
        /**
         * sku Url
         */
        private String imagesUrl;

        /**
         * 分类
         */
        private String category;
        /**
         * 分类id
         */
        private String categoryId;

        /**
         * 属性
         */
        private String property;

        /**
         * 物流属性id
         */
        private String logisticsPropertyId;
    }
    @Data
    @NoArgsConstructor
    public static class SkuDTO {
        /**
         * 产品id
         */
        private String id;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;
        /**
         * spu编号
         */
        private String spuNo;
        /**
         * ean码
         */
        private String eanNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 标准零售价
         */
        private BigDecimal retailPrice;
        /**
         * 状态
         */
        private  Integer status;
    }
    @Data
    @NoArgsConstructor
    public static class SkuSearchDTO {
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * ean码
         */
        private String ean;
        /**
         * 客户sku
         */
        private String platformSkuNo;
        /**
         * sku类型
         */
        private String skuType;
    }
    @Data
    public static class ServiceToWavePickingDTO {
        /**
         * sku id
         */
        private String skuId;

        /**
         * sku 编号
         */
        private String skuNo;

        /**
         * 产品图片url
         */
        private String imageUrl;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 颜色
         */
        private String variantProperty;

        /**
         * 产品EAN码
         */
        private String eanNo;
    }


    @Data
    @NoArgsConstructor
    public static class SkuChangeFieldsDTO {

        private String productId;

        private String name;

        private String skuNo;

        private String chargeId;

        private String chargeName;

        private List<SkuChangeInfoDTO> productBasicChangeField;

        private List<SkuChangeInfoDTO> productPackChangeField;

    }

    @Data
    @NoArgsConstructor
    public static class SkuChangeInfoDTO {

        private String fieldName;

        private String oldValue;

        private String newValue;


    }

    /**
     * 通知DTO
     */
    @Data
    @NoArgsConstructor
    public static class NoticeDTO {
        private String productId;
        private String name;
        private String chargeId;
        private String chargeName;
        private String skuNo;
        private List<ProductDetailDTO.SkuChangeInfoDTO> productBasicChangeField;
        private List<ProductDetailDTO.SkuChangeInfoDTO> productPackChangeField;
    }

    /**
     * @Description sku图片信息请求参数
     * @Author jack
     * @Date 2025-07-25
     */
    @Data
    @NoArgsConstructor
    public static class ProductImagesDTO {
        /**
         * 产品sku明细表id
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
         * 图片地址
         */
        private List<String> imagesUrls;
    }

    /**
     * @Description sku图片信息请求参数
     * @Author jack
     * @Date 2025-07-25
     */
    @Data
    @NoArgsConstructor
    public static class ProductImagesZipDTO {
        /**
         * 文件URL
         */
        @NotBlank(message = "zip压缩文件不能为空")
        private String fileUrl;

        /**
         * 导入类型
         */
        @NotBlank(message = "导入类型不能为空")
        private String importType;

        //ProductDetailImprotTypeEnum
        private String taskId;
    }

}
