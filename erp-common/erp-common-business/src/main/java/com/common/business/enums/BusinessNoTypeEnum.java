package com.common.business.enums;

/**
 * @author Will
 * @version 1.0
 * @description: 系统编码
 * @date 2022/11/22 10:26
 */
public enum BusinessNoTypeEnum {

    SKU_NO(1, "sku_no","sku编号"),
    SPU_NO(2, "spu_no","spu编号"),
    Bom_NO(3, "bom_no","bom编号"),
    CODE_BH(4, "bh","备货申请单编号"),
    CODE_GYS(5, "gys","供应商编号"),
    CODE_PL(6, "pl","采购申请单编号"),
    CODE_CGJM(7, "cgjm","采购价目编号"),
    CODE_CGTJ(8, "cgtj","采购价目调价编号"),
    CODE_PO(9, "po","采购订单"),
    CODE_POC(10, "poc","采购变更单"),
    CODE_ZJGZ(11, "zjgz","质检规则单");




    private Integer code;
    private String name;
    private String desc;


    BusinessNoTypeEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(Integer code) {
        BusinessNoTypeEnum[] businessNoTypeEnums = values();
        for (BusinessNoTypeEnum businessNoTypeEnum : businessNoTypeEnums) {
            if (businessNoTypeEnum.getCode().equals(code)) {
                return businessNoTypeEnum.getName();
            }
        }
        return null;
    }

    public static BusinessNoTypeEnum getEnumByType(String code) {
        BusinessNoTypeEnum[] businessNoTypeEnums = values();
        for (BusinessNoTypeEnum businessNoTypeEnum : businessNoTypeEnums) {
            if (businessNoTypeEnum.getCode().equals(code)) {
                return businessNoTypeEnum;
            }
        }
        return null;
    }

    public static Integer getCodeByName(String name) {
        BusinessNoTypeEnum[] businessNoTypeEnums = values();
        for (BusinessNoTypeEnum businessNoTypeEnum : businessNoTypeEnums) {
            if (businessNoTypeEnum.getName().equals(name)) {
                return businessNoTypeEnum.getCode();
            }
        }
        return null;
    }
}
