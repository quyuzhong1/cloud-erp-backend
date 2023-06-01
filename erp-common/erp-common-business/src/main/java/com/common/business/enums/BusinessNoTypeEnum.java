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
    CODE_ZJGZ(11, "zjgz","质检规则单"),
    CODE_CGSH(12, "cgsh","采购收货单"),
    CODE_CGRK(13, "cgrk","采购收货单"),
    CODE_CGTH(14, "cgth","采购退货单"),
    CODE_USER(15, "user_no","用户编号"),
    CODE_QC(16, "qc","质检单"),
    CODE_DEPT(17, "bm","部门编号"),
    CODE_RKYB(18, "rkyb","入库预报"),
    CODE_DBSQ(19, "dbsq","调拨申请"),
    CODE_CUST(20, "cust","客户"),
    CODE_INIT_STOCK(21, "init_stock","期初库存"),
    CODE_THDD(22, "thdd","销售退货订单"),
    CODE_THTZ(23, "thtz","销售退货通知单"),
    CODE_XSD(24, "xsd","销售单"),
    CODE_ZJDB(25, "zjdb","直接调拨单"),
    CODE_ZZCX(26, "zzcx","加工单"),
    CODE_THQS(27, "thqs","退货签收单"),
    CODE_XSCK(28, "xsck","销售出库单"),
    CODE_QTRK(29, "qtrk","其他入库单"),
    CODE_QTCK(30, "qtck","其他出库单"),
    CODE_XSBG(31, "xsbg","销售变更单"),
    CODE_FHTZ(32, "fhtz","发货通知单"),
    CODE_FBDC(33, "fbdc","分步式调拨调出"),
    CODE_FBDR(34, "fbdr","分步式调拨调入"),
    CODE_XSTH(35, "xsth","销售退货入库单"),
    ;




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
