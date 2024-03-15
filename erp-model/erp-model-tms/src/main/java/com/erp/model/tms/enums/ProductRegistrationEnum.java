package com.erp.model.tms.enums;

import cn.hutool.core.bean.BeanUtil;
import com.common.core.constant.EnumMessage;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 产品备案枚举
 */
@Getter
public enum ProductRegistrationEnum {

    ;

    /**
     * 产品状态枚举
     */
    @Getter
    public enum StatusEnum implements EnumMessage {

        DRAFT("draft","草稿"),
        REGISTERING("registering","备案中"),
        REGISTERED("registered","已备案"),
        FREEZE("freeze","冻结"),
        CANCEL("cancel","已取消"),
        ;
        private final String code;
        private final String name;
        StatusEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }


    /**
     * 详情枚举
     */
    @Getter
    public enum DetailDescEnum {
        OPERATION("操作类型：1新增、2修改","operationType","默认字段","新增",""),
        SKU("产品SKU","sku","SKU","",""),
        NAME("产品中文名称","sku","产品品名(中文)","",""),
        ENGLISH_NAME("产品英文名称","englishName","产品品名(英文)","",""),
        COMMODITY_ID("商品ID","commodityId","","",""),
        SUPPLIER_CODE("供应商代码","supplierCode","","",""),
        UNIT("计量单位，三位海关代码：006套，007个，011件，025双，032平方米，125包，140盒","unit","报关单位","",""),
        MODEL("产品型号","model","SPU","",""),
        BARCODE_TYPE("条码类型：0默认条码、1自定义条码、2序列号","barcodeType","","",""),
        BARCODE("自定义条码（barcodeType=1时，必填）","barcode","","",""),
        CURRENCY_CODE("申报币种","currencyCode","报关币种","",""),
        DECLARED_VALUE("申报价值","declaredValue","报关申报价","",""),
        WEIGHT("产品重量KG","weight","毛重（g）","",""),
        LENGTH("产品长CM","length","包装尺寸(cm)-长","",""),
        WIDTH("产品宽CM","width","包装尺寸(cm)-宽","",""),
        HEIGHT("产品高CM","height","包装尺寸(cm)-高","",""),
        HAS_INVOICE("是否带发票：1是、2否","HasInvoice","","",""),
        HAS_BATTERY("是否带电池：0否、1是","hasBattery","属性【带电池**】显示为是，其他为否","",""),
        BATTERY_TYPE("电池类型（hasBattery=1必填）","batteryType","","",""),
        BATTERY_NOTE("二级分类","batteryNote","","",""),
        IS_ACCESSORY("是否属于零件类：0否、1是","isAccessory","","",""),
        HS_NAME("产品海关品名","hsName","报关中文名","",""),
        HS_CODE("海关编码HS_CODE","hsCode","中国海关编码","",""),
        FIRST_QAUNTITY("法定数量（第一数量）","firstQauntity","第一数量[新增字段]","",""),
        SECOND_QAUNTITY("第二数量，当对应海关编码存在第二单位时必填","secondQauntity","第二数量[新增字段]","",""),
        HS_ELEMENT("申报要素，各个申报要素项使用竖线|分隔","hsElement","申报要素","",""),
        PICTURE_URL("产品图片URL地址","pictureUrl","中国海关编码","",""),
        ;

        /**
         * 接口字段中文
         */
        private final String interfaceFieldCn;
        /**
         * 接口字段英文
         */
        private final String interfaceFieldEn;

        /**
         * 数大臣字段
         */
        private final String erpField;

        /**
         * 推送信息
         */
        private final String pushValue;

        /**
         * 拉取信息
         */
        private final String pullValue;

        DetailDescEnum(String interfaceFieldCn, String interfaceFieldEn, String erpField, String pushValue, String pullValue) {
            this.interfaceFieldCn = interfaceFieldCn;
            this.interfaceFieldEn = interfaceFieldEn;
            this.erpField = erpField;
            this.pushValue = pushValue;
            this.pullValue = pullValue;
        }

        public static List<ProductRegistrationDTO.ViewDetailVO> convertToViewList(){
           return BeanUtil.copyToList(Arrays.asList(DetailDescEnum.values()),ProductRegistrationDTO.ViewDetailVO.class);
        }

    }
}
