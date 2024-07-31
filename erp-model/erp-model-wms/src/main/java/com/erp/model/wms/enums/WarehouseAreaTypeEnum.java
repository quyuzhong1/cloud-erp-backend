package com.erp.model.wms.enums;

import com.common.core.constant.EnumMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库区类型枚举
 * @date 2024-07-17
 * @author tanmujin
 */
@Getter
@AllArgsConstructor
public enum WarehouseAreaTypeEnum implements EnumMessage {
    PICKING_AREA("pickingArea", "拣货区"),
    STOCKING_AREA("stockingArea", "备货区"),
    STAGING_AREA("stagingArea", "暂存区"),
    DEFECTIVE_AREA("defectiveArea", "次品区")
    ;
    private String code;
    private String name;
}
