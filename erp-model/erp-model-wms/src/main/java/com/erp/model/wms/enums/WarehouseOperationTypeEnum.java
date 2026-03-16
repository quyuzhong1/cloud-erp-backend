package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 仓库操作类型
 */
public enum WarehouseOperationTypeEnum implements EnumMessage {
    NO_OPEN_RELABLE("NO_OPEN_RELABLE", "不开箱换SKU标"),
    OPEN_RELABLE("OPEN_RELABLE", "开箱换SKU标"),
    PASTE_LABEL("PASTE_LABEL", "贴板标"),
    PASTE_PACKAGE("PASTE_PACKAGE", "贴箱唛"),
    OTHER("OTHER", "其他"),
    //zhongbao
    IS_CHANGE_PACKAGE("isChangePackage", "不开箱换SKU标"),
    CHANGE_BARCODE_TYPE("changeBarcodeType", "开箱换SKU标"),
    IS_COVER_BARCODE("isCoverBarcode", "贴板标"),
    CHANGE_SHIPPING_MARK_TYPE("changeShippingMarkType", "贴箱唛"),
    IS_COVER_SHIPPING_MARK("isCoverShippingMark", "其他"),
    IS_PALLET("isPallet", "打托"),
    IS_DOUBLE_PALLET("isDoublePallet", "双板打托"),
    IS_MIXED_PALLET("isMixedPallet", "混托"),
    PASTE_CARTON_MARK_TYPE("pasteCartonMarkType", "贴托唛"),
    IS_PALLET_SCHEME("isPalletScheme", "主动SKU打托方案"),
    LIMIT_PLATE_NUM("limitPlateNum", "限板数"),
    LIMIT_PLATE_HEIGHT("limitPlateHeight", "限板高"),
    LIMIT_PLATE_WEIGHT("limitPlateWeight", "限板重"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;



    WarehouseOperationTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }


    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (WarehouseOperationTypeEnum billTypeEnum : WarehouseOperationTypeEnum.values()) {
            if (code.equals(billTypeEnum.getCode())) {
                return billTypeEnum.getName();
            }
        }
        return "";
    }

    public static WarehouseOperationTypeEnum fromCode(String code) {
        for (WarehouseOperationTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
