package com.erp.model.wms.dto.third;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class ThirdWarehouseSkuResp implements Serializable {

    //商品条码
    @JSONField(name = "product_barcode")
    private String productBarcode;

    //商品sku
    @JSONField(name = "product_sku")
    private String productSku;

    //客户参考号
    @JSONField(name = "reference_no")
    private String referenceNo;

    //产品状态
    @JSONField(name = "product_status")
    private String productStatus;

    //中文名称
    @JSONField(name = "product_title_cn")
    private String productTitleCn;

    //英文名称
    @JSONField(name = "product_title_en")
    private String productTitleEn;

    //进口国清关信息
    @JSONField(name = "import_country")
    private List<ImportCountry> importCountryList;

    //商品关税信息
    @JSONField(name = "tax_info")
    private List<TaxInfo> taxInfoList;

    @Data
    @ToString
    public static class ImportCountry implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;
        //国家/地区简称
        @JSONField(name = "country_code")
        private String countryCode;

        //申报价值
        @JSONField(name = "declared_value")
        private Float declaredValue;
    }

    @Data
    @ToString
    public static class TaxInfo implements Serializable{
        private static final long serialVersionUID = 1905122041950251208L;

        //可发国家/地区
        @JSONField(name = "exportable_country")
        private String exportableCountry;

        //海外仓是否可存 Y/N
        @JSONField(name = "allow_save")
        private String allowSave;
    }

}
