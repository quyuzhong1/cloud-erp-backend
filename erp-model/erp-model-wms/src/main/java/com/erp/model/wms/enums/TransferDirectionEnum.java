package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Will
 * @version 1.0

 * @date 2023/5/15 16:43
 */
public enum TransferDirectionEnum implements EnumMessage {


    ORDINARY ("ordinary", "普通","GENERAL"),
    RETURN_GOODS("returnGoods", "退货","RETURN");

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

    /**
     * 金蝶编码
     */
    private String kingdeeCode;

    TransferDirectionEnum(String code, String name,String kingdeeCode) {
        this.code = code;
        this.name = name;
        this.kingdeeCode = kingdeeCode;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getKingdeeCode() {
        return kingdeeCode;
    }

    public static String getName(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (TransferDirectionEnum item : TransferDirectionEnum.values()) {
                if (code.equals(item.getCode())) {
                    return item.getName();
                }
            }
        }
        return "";
    }

    /**
     * 根据代码获取
     * @param code
     * @return
     */
    public static TransferDirectionEnum getByCode(String code) {
        return Arrays.stream(TransferDirectionEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据金蝶编码获取
     * @param kingdeeCode
     * @return
     */
    public static TransferDirectionEnum ofKingdeeCode(String kingdeeCode) {
        return Arrays.stream(TransferDirectionEnum.values()).filter(r -> Objects.equals(r.getKingdeeCode(), kingdeeCode)).findFirst().orElse(null);
    }

    /**
     * 根据代码获取名称
     * @param kingdeeCode
     * @return
     */
    public static String getCodeByKingdeeCode(String kingdeeCode) {
        TransferDirectionEnum transferDirectionEnum =  ofKingdeeCode(kingdeeCode);
        return Optional.ofNullable(transferDirectionEnum).map(TransferDirectionEnum::getCode).orElse("");
    }
}
