package com.sdk.wms.zhongbao.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author zdy
 * @ClassName WarehouseResponse
 * @description: TODO
 * @date 2026年03月02日
 * @version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProductResponse extends PageResponse {
    private List<Product> list;

    @Data
    public static class Product {
        //产品SKU
        private String productSku;
        //产品名称
        private String name;
        //英文名称
        private String nameEn;
        //自定义编号
        private String referenceNo;
        //制造商
        private String manufacturer;
        //产品描述
        private String productDesc;
        //产品单价
        private Double price;
        //产品长度单位:1=>米,2=>厘米,3=>英寸
        private Integer productLengthType;
        //产品长度
        private Double productLength;
        //产品宽度
        private Double productWidth;
        //产品高度
        private Double productHeight;
        //重量单位:1=>KG,2=>LB
        private Integer productWeightType;
        //产品重量(毛重)
        private Double productWeight;
        //产品体积(CM³)
        private Double productVolume;
        //产品品牌
        private String productBrand;
        //产品产地
        private String productOrigin;
        //产品材质
        private String productMaterial;
        //产品型号
        private String productModel;
        //是否电池:1=>是,-1=>否
        private Integer isBattery;
        //是否易碎品:1=>是,-1=>否
        private Integer isFragile;
        //是否自包装:1=>是,-1=>否
        private Integer isOwnPackage;
        //是否拆包上架:1=>是,-1=>否
        private Integer isUnpacking;
        //产品类型:1=>普通,2=>小件,3=>大件,4=>超大件,5=>超重件
        private Integer productType;
        //德国包装法要求的注册编码LUCID,非德国仓库sku可忽略(多个用英文逗号分隔)
        private Integer productLucid;
        //产品状态:-1=>废弃,1=>草稿,2=>待审核,3=>已审核,4=>驳回
        private Integer status;
        //复核状态:-1=>待审核,1=>已审核
        private Integer reviewStatus;
        //复核差异:-1=>未复核,1=>有差异,2=>无差异
        private Integer reviewDiff;
        private Category openCategory;
        private Battery openBattery;
        private PlatformSku openPlatformSkus;
        private List<Attachment> openAttachments;
    }

    @Data
    public static class Category {
        //分类名称
        private String name;
        //英文名称
        private String nameEn;
    }
    @Data
    public static class Battery {
        //电池类型:1=>干电池,2=>纽扣电池,3=>镍氢电池,4=>铅酸蓄电池,5=>锂金属电池,6=>锂离子电池(组)
        private Integer batteryType;
        //电池容量
        private String batteryCapacity;
        //电池数量
        private String batteryModel;
        //电池电压(V)
        private String batteryVoltage;
        //出厂电池包电压(V)
        private String batteryPackVoltage;
        //电池功率
        private String batteryPower;
        //电池能量
        private String batteryEnergy;
        //电池材质
        private String batteryMaterial;
        //电池供应商
        private String batterySupplier;
        //包装(PACK)供应商
        private String packSupplier;
        //是否有化学品安全说明书(MSDS):1=>是,-1=>否
        private Integer isMsds;
        //是否有运输鉴定报告:1=>是,-1=>否
        private Integer isTransportReport;
        //电池资料有效期（为空则永久有效）
        private String batteryDataValidity;
        //UL认证:1=>是,-1=>否
        private Integer ulCertification;
        //UL发证机构
        private String ulAuthority;
        //UL编号
        private String ulNumber;
    }
    @Data
    public static class PlatformSku {
        //平台类型:1=>亚马逊,2=>速卖通,3=>wish,4=>ebay
        private Integer platformType;
        //条码类型:1=>CODE128,2=>CODE39,3=>ISBN,4=>JAN,5=>ITF,6=>EAN,7=>UPC,8=>QR,9=>PDF417,10=>DATAMATRIX
        private Integer barcodeType;
        //平台SKU
        private String platformSku;
        //平台SKU条形码
        private String platformSkuBarcode;
    }
    @Data
    public static class Attachment {
        //附件类型
        private String attachmentType;
        //文件类型(PNG/JPG/JPEG)
        private String fileType;
        private String base64;
        //文件名称
        private String fileName;
    }
}
