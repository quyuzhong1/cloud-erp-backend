package com.erp.model.wms.enums.inventory;

import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 虚拟仓库库存业务
 * @author will
 * @date 2024/6/4 10:58
 */
public enum VirtualInventoryBusinessTypeEnum {

    /**
     * 调拨
     */
    TRANSFER_USABLE("transfer_usable", "01","调拨，当前仓可用减少，目的仓增加"),
    /**
     * 出库
     */
    OUT_USABLE("out_usable", "02","出库，当前仓可用减少"),
    /**
     * 入库
     */
    IN_USABLE("in_usable", "03","入库，当前仓可用增加"),

    /**
     * b2c发货单，减可用，加冻结
     */
    SO_B2C_DELIVERY("so_b2c_delivery", "04","b2c发货单"),

    /**
     * 发货通知单，减可用
     */
    SO_DELIVERY_NOTICE("so_delivery_notice", "05","发货通知单"),

    /**
     * 销售出库单，减冻结
     */
    SO_OUT_STOCK("so_out_stock", "06","销售出库单"),

    /**
     * B2B销售订单锁定库存，添加冻结减少可用
     */
    SO_INFO_LOCK_ADD("so_info_lock_add", "07","B2B销售订单添加"),

    /**
     * B2B销售订单锁定库存，添加可用减少冻结
     */
    SO_INFO_LOCK_LESS("so_info_lock_less", "08","B2B销售订单减少"),

    /**
     * B2B销售订单释放库存，减冻结加可用
     */
    SO_INFO_UNLOCK("so_info_unlock", "09","B2B销售订单"),

    /**
     * b2c发货单取消发货，减冻结，加可用
     */
    SO_B2C_DELIVERY_CANCEL("so_b2c_delivery_cancel", "10","b2c发货单"),

    /**
     * 要货申请处理，减可用，加冻结
     */
    REQUISITION_APPLICATION_HANDLE("requisition_application_handle", "11","要货申请处理"),

    /**
     * 要货申请回退处理，减冻结，加可用
     */
    REQUISITION_APPLICATION_RETURN_HANDLE("requisition_application_return_handle", "12","要货申请回退处理"),

    /**
     * 直接调拨单审核，减冻结
     */
    TRANSFER_INFO_APPROVE("transfer_info_approve", "13","直接调波单审核"),

    /**
     * 发货通知单，减冻结，加可用
     */
    SO_DELIVERY_NOTICE_APPROVE("so_delivery_notice_approve", "14","发货通知单审核"),

    /**
     * 销售订单减冻结
     */
    SO_INFO_SUBTRACT_FREEZE("so_info_subtract_freeze", "15","销售订单减冻结"),

    /**
     * 发货通知单新增，加冻结
     */
    SO_DELIVERY_NOTICE_ADD("so_delivery_notice_add", "16","发货通知单新增加冻结"),

    /**
     * 加工单子件出库，减冻结
     */
    MACHINE_INFO_CHILD_OUT("machine_info_child_out", "17","加工单子件出库"),

    /**
     * 发货通知单处理类型
     */
    SO_DELIVERY_NOTICE_HANDLE("so_delivery_notice_handle", "18","发货通知单处理减少可用、添加冻结"),

    FREEZE_OUT_USABLE("freeze_out_usable", "19","出库，当前仓冻结减少"),
    FREEZE_IN_USABLE("freeze_in_usable", "20","入库，当前仓冻结增加"),
    ;

    private String code;

    @Setter
    private String type;

    private String name;

    VirtualInventoryBusinessTypeEnum(String type, String code, String name) {
        this.type = type;
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    /**
     * 根据仓库交易单据代码获取
     * @param code
     * @return
     */
    public static VirtualInventoryBusinessTypeEnum getByCode(String code) {
        return Arrays.stream(VirtualInventoryBusinessTypeEnum.values()).filter(r -> Objects.equals(r.getCode(), code)).findFirst().orElse(null);
    }

    /**
     * 根据仓库交易单据类型获取
     * @param type
     * @return
     */
    public static VirtualInventoryBusinessTypeEnum getByType(String type) {
        return Arrays.stream(VirtualInventoryBusinessTypeEnum.values()).filter(r -> Objects.equals(r.getType(), type)).findFirst().orElse(null);
    }



}
