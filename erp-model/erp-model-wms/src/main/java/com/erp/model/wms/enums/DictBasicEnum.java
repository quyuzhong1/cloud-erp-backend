package com.erp.model.wms.enums;

/**
 * @author Lambda
 * @Classname DictBasicEnum

 * @Date 2023-03-20 14:15
 * @Created by yl
 */
public enum DictBasicEnum {

    WAREHOUSE_TYPE("warehouseType", "", "仓库类型"),
    QC_REPORT_RESULT("qcReportResult", "", "质检单质检结果"),
    HANDLE_MODE_TYPE("handleModeType", "", "质检单处理措施"),
    TRANSFER_TYPE("transferType", "", "调拨类型"),
    TRANSFER_DIRECTION("transferDirection", "", "调拨方向"),
    INVENTORY_DIRECTION("inventoryDirection", "", "库存方向"),
    INSTOCK_TYPE("instockType", "", "入库类型"),
    OUTSTOCK_TYPE("outstockType", "", "出库类型"),
    WORK_TYPE("workType", "", "事务类型"),
    MACHINE_TYPE("machineType", "", "加工单类型"),
    ISSUE_TYPE("issueType", "", "发料类型"),
    CFG_SETTING("cfgSetting", "", "系统配置"),
    WAREHOUSE_MANAGE_TYPE("warehouseManageType", "", "仓库经营类型"),
    GEOGRAPHY_LOCATION("geographyLocation", "", "仓库地库位置"),

    SKU_MAPPING_DEFAULT_MANAGE_DELIVERY_TYPE("defaultManageDeliveryType", "", "默认SKU映射仓库发货配置"),
    ;


    private String key;
    private String remark;
    private String desc;


    DictBasicEnum(String key, String remark, String desc) {
        this.key = key;
        this.remark = remark;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getRemark() {
        return remark;
    }

    public String getDesc() {
        return desc;
    }
}
