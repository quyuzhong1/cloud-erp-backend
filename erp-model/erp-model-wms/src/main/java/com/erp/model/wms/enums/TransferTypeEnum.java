package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Will
 * @version 1.0

 * @date 2023/5/15 16:55
 */
public enum TransferTypeEnum implements EnumMessage {

    IN_ORG ("inOrg", "组织内调拨","InnerOrgTransfer"),
    CROSS_ORG("crossOrg", "跨组织调拨","OverOrgTransfer");

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

    TransferTypeEnum(String code, String name,String kingdeeCode) {
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

    /**
     * 根据代码获取
     * @param code
     * @return
     */
    public static TransferTypeEnum of(String code) {
        return Arrays.stream(TransferTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据金蝶编码获取
     * @param kingdeeCode
     * @return
     */
    public static TransferTypeEnum ofKingdeeCode(String kingdeeCode) {
        return Arrays.stream(TransferTypeEnum.values()).filter(r -> Objects.equals(r.getKingdeeCode(), kingdeeCode)).findFirst().orElse(null);
    }

    /**
     * 根据代码获取名称
     * @param code
     * @return
     */
    public static String getNameByCode(String code) {
        TransferTypeEnum transferTypeEnum =  of(code);
        return Optional.ofNullable(transferTypeEnum).map(TransferTypeEnum::getName).orElse("");
    }

    /**
     * 根据代码获取名称
     * @param kingdeeCode
     * @return
     */
    public static String getCodeByKingdeeCode(String kingdeeCode) {
        TransferTypeEnum transferTypeEnum =  ofKingdeeCode(kingdeeCode);
        return Optional.ofNullable(transferTypeEnum).map(TransferTypeEnum::getCode).orElse("");
    }
}
