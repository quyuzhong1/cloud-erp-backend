package com.erp.model.dmp.enums;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/3 11:46
 */
public enum KingdeePushModuleEnum {

    BD_MATERIAL("BD_MATERIAL","物料"),
    ENG_BOM("ENG_BOM","物料清单"),

    STK_TRANSFER_DIRECT("STK_TransferDirect","直接调拨单"),
    PLM_CFG_PREFERRED_ORGANIZATION_CFG("PLM_CFG_PreferredOrganizationCFG","下推首选组织配置"),

    BOS_ASSISTANTDATA_DETAIL("BOS_ASSISTANTDATA_DETAIL","辅助资料列表"),
    BD_EMPINFO("BD_Empinfo","员工"),

    ;
    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    KingdeePushModuleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
