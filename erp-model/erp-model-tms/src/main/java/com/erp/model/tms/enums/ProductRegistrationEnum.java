package com.erp.model.tms.enums;

import cn.hutool.core.bean.BeanUtil;
import com.common.core.constant.EnumMessage;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

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
    public enum TabEnum implements EnumMessage {

        REGISTERING("notRegister","未备案"),
        DRAFT("registered","已备案"),
        ;
        private final String code;
        private final String name;
        TabEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }
    /**
     * 产品状态枚举
     */
    @Getter
    public enum StatusEnum implements EnumMessage {

        DRAFT("reject","备案不通过"),
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
        OPERATION("操作类型：1新增、2修改", "operationType", "默认字段", v -> "默认", null, null),
        SKU("产品SKU", "sku", "SKU", LogisticsProductDTO.ProductDTO::getSkuNo, ProductRegistrationEntity::getSkuNo, LogisticsProductDTO.ProductDTO::getSkuNo),
        NAME("产品中文名称", "sku", "产品品名(中文)", LogisticsProductDTO.ProductDTO::getCnName, ProductRegistrationEntity::getProductName, LogisticsProductDTO.ProductDTO::getCnName),
        ENGLISH_NAME("产品英文名称", "englishName", "产品品名(英文)", LogisticsProductDTO.ProductDTO::getEnName, ProductRegistrationEntity::getProductNameEn, LogisticsProductDTO.ProductDTO::getEnName),
        COMMODITY_ID("商品ID", "commodityId", "", null, ProductRegistrationEntity::getGoodId, null),
        SUPPLIER_CODE("供应商代码", "supplierCode", "", null, ProductRegistrationEntity::getSupplierCode, null),
        UNIT("计量单位，三位海关代码：006套，007个，011件，025双，032平方米，125包，140盒", "unit", "报关单位", LogisticsProductDTO.ProductDTO::getDeclareUnitName, ProductRegistrationEntity::getDeclareUnit, LogisticsProductDTO.ProductDTO::getDeclareUnitName),
        MODEL("产品型号", "model", "SPU", LogisticsProductDTO.ProductDTO::getDeclareModel, ProductRegistrationEntity::getSpu, LogisticsProductDTO.ProductDTO::getDeclareModel),
        BARCODE_TYPE("条码类型：0默认条码、1自定义条码、2序列号", "barcodeType", "", null, ProductRegistrationEntity::getBarcodeType, null),
        BARCODE("自定义条码（barcodeType=1时，必填）", "barcode", "", null, ProductRegistrationEntity::getCustomBarcode, null),
        CURRENCY_CODE("申报币种", "currencyCode", "报关币种", v-> "CNY".equals(v.getDeclareCurrency())?"RMB":v.getDeclareCurrency(), ProductRegistrationEntity::getCurrency, v-> "CNY".equals(v.getDeclareCurrency())?"RMB":v.getDeclareCurrency()),
        DECLARED_VALUE("申报价值", "declaredValue", "报关申报价", LogisticsProductDTO.ProductDTO::getDeclarePrice, ProductRegistrationEntity::getDeclarePrice, LogisticsProductDTO.ProductDTO::getDeclarePrice),
        WEIGHT("产品重量KG", "weight", "毛重（g）", LogisticsProductDTO.ProductDTO::getGrossWeight, ProductRegistrationEntity::getGrossWeight, LogisticsProductDTO.ProductDTO::getGrossWeight),
        LENGTH("产品长CM", "length", "包装尺寸(cm)-长", LogisticsProductDTO.ProductDTO::getBoxSizeLength, ProductRegistrationEntity::getLength, LogisticsProductDTO.ProductDTO::getBoxSizeLength),
        WIDTH("产品宽CM", "width", "包装尺寸(cm)-宽", LogisticsProductDTO.ProductDTO::getBoxSizeWide, ProductRegistrationEntity::getWidth, LogisticsProductDTO.ProductDTO::getBoxSizeWide),
        HEIGHT("产品高CM", "height", "包装尺寸(cm)-高", LogisticsProductDTO.ProductDTO::getBoxSizeHigh, ProductRegistrationEntity::getHeight, LogisticsProductDTO.ProductDTO::getBoxSizeHigh),
        HAS_INVOICE("是否带发票：1是、2否", "HasInvoice", "", null, ProductRegistrationEntity::getIsInvoice, null),
        HAS_BATTERY("是否带电池：0否、1是", "hasBattery", "属性【带电池**】显示为是，其他为否", LogisticsProductDTO.ProductDTO::getIsElectric, ProductRegistrationEntity::getIsBattery, LogisticsProductDTO.ProductDTO::getIsElectric),
        BATTERY_TYPE("电池类型（hasBattery=1必填）", "batteryType", "", null, ProductRegistrationEntity::getBatteryType, null),
        BATTERY_NOTE("二级分类", "batteryNote", "", null, ProductRegistrationEntity::getBatteryNote, null),
        IS_ACCESSORY("是否属于零件类：0否、1是", "isAccessory", "", null, ProductRegistrationEntity::getIsParts, null),
        HS_NAME("产品海关品名", "hsName", "报关中文名", LogisticsProductDTO.ProductDTO::getDeclareChineseName, ProductRegistrationEntity::getDeclareNameCn, LogisticsProductDTO.ProductDTO::getDeclareChineseName),
        HS_CODE("海关编码HS_CODE", "hsCode", "中国海关编码", LogisticsProductDTO.ProductDTO::getCustomsCode, ProductRegistrationEntity::getCustomsCode, LogisticsProductDTO.ProductDTO::getCustomsCode),
        FIRST_QAUNTITY("法定数量（第一数量）", "firstQauntity", "第一数量[新增字段]", LogisticsProductDTO.ProductDTO::getFirstQty, ProductRegistrationEntity::getFirstNumber, LogisticsProductDTO.ProductDTO::getFirstQty),
        SECOND_QAUNTITY("第二数量，当对应海关编码存在第二单位时必填", "secondQauntity", "第二数量[新增字段]", LogisticsProductDTO.ProductDTO::getSecondQty, ProductRegistrationEntity::getSecondNumber, LogisticsProductDTO.ProductDTO::getSecondQty),
        HS_ELEMENT("申报要素，各个申报要素项使用竖线|分隔", "hsElement", "申报要素", LogisticsProductDTO.ProductDTO::getDeclareElement, ProductRegistrationEntity::getDeclareElement, LogisticsProductDTO.ProductDTO::getDeclareElement),
        PICTURE_URL("产品图片URL地址", "pictureUrl", "产品图片URL", LogisticsProductDTO.ProductDTO::getImagesUrl, ProductRegistrationEntity::getUrl, LogisticsProductDTO.ProductDTO::getImagesUrl),
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
        private final Function<LogisticsProductDTO.ProductDTO, ?> pushMethod;
        /**
         * 拉取信息
         */
        private final Function<ProductRegistrationEntity, ?> pullMethod;

        /**
         * 最新信息
         */
        private final Function<LogisticsProductDTO.ProductDTO, ?> latestMethod;

        DetailDescEnum(String interfaceFieldCn, String interfaceFieldEn, String erpField, Function<LogisticsProductDTO.ProductDTO, ?> pushMethod, Function<ProductRegistrationEntity, ?> pullMethod,Function<LogisticsProductDTO.ProductDTO, ?> latestMethod) {
            this.interfaceFieldCn = interfaceFieldCn;
            this.interfaceFieldEn = interfaceFieldEn;
            this.erpField = erpField;
            this.pushMethod = pushMethod;
            this.pullMethod = pullMethod;
            this.latestMethod = latestMethod;
        }

        public static List<ProductRegistrationDTO.ViewDetailVO> convertToViewList(LogisticsProductDTO.ProductDTO productDTO, ProductRegistrationEntity entity,LogisticsProductDTO.ProductDTO latestDTO){
            List<ProductRegistrationDTO.ViewDetailVO> viewDetailVOList = new ArrayList<>();
            for(DetailDescEnum detailDescEnum : DetailDescEnum.values()){
                ProductRegistrationDTO.ViewDetailVO viewDetailVO = new ProductRegistrationDTO.ViewDetailVO();
                viewDetailVO.setInterfaceFieldCn(detailDescEnum.getInterfaceFieldCn());
                viewDetailVO.setInterfaceFieldEn(detailDescEnum.getInterfaceFieldEn());
                viewDetailVO.setErpField(detailDescEnum.getErpField());
                viewDetailVO.setPushValue(Optional.ofNullable(detailDescEnum.pushMethod).map(method -> method.apply(productDTO)).map(Object::toString).orElse(""));
                viewDetailVO.setPullValue(Optional.ofNullable(detailDescEnum.pullMethod).map(method -> method.apply(entity)).map(Object::toString).orElse(""));
                viewDetailVO.setLatestValue(Optional.ofNullable(detailDescEnum.latestMethod).map(method -> method.apply(latestDTO)).map(Object::toString).orElse(""));
                viewDetailVOList.add(viewDetailVO);
            }
            return viewDetailVOList;
        }

    }
}
