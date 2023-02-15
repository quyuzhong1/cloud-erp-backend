package com.erp.server.plm.enums;

import com.erp.model.plm.entity.BomSkuEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;

/**
 * @author Will
 * @version 1.0
 * @description: 操作日志类型枚举
 * @date 2023/1/7 11:46
 */
public enum SysLogClassPathEnum {

    PRODUCTDETAILENTITY(0, "SKU", String.valueOf(ProductDetailEntity.class)),
    PRODUCTINFOENTITY(1, "SPU", String.valueOf(ProductInfoEntity.class)),
    PROJECTTASKENTITY(2, "任务列表", String.valueOf(ProjectTaskEntity.class)),
    BOM_SKU_ENTITY(2, "bomSku", String.valueOf(BomSkuEntity.class));


    private Integer code;
    private String name;
    private String desc;

    SysLogClassPathEnum(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }

    public static String getName(Integer code) {
        for (SysLogClassPathEnum sysLogClassPathEnum : SysLogClassPathEnum.values()) {
            if (sysLogClassPathEnum.getCode().equals(code)) {
                return sysLogClassPathEnum.getName();
            }
        }
        return "";
    }

    public static Integer getCodeByName(String name) {
        SysLogClassPathEnum[] enums = values();
        for (SysLogClassPathEnum sysLogClassPathEnum : enums) {
            if (sysLogClassPathEnum.getName().equals(name)) {
                return sysLogClassPathEnum.getCode();
            }
        }
        return null;
    }

}
