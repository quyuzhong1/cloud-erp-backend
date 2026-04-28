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
    IS_CHANGE_PACKAGE("isChangePackage", "是否更换包装"),
    CHANGE_BARCODE_TYPE("changeBarcodeType", "换条码类型"),
    IS_COVER_BARCODE("isCoverBarcode", "是否覆盖条码"),
    CHANGE_SHIPPING_MARK_TYPE("changeShippingMarkType", "换箱唛类型"),
    IS_COVER_SHIPPING_MARK("isCoverShippingMark", "是否覆盖箱唛"),
    IS_PALLET("isPallet", "是否打托"),
    IS_DOUBLE_PALLET("isDoublePallet", "是否双板打托"),
    IS_MIXED_PALLET("isMixedPallet", "是否混托"),
    PASTE_CARTON_MARK_TYPE("pasteCartonMarkType", "贴托唛类型"),
    IS_PALLET_SCHEME("isPalletScheme", "是否主动SKU打托方案"),
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
