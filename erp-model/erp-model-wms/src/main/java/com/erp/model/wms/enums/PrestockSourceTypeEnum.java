package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * 预入库单数据来源类型枚举
 * 对应主表 source_type 字段；该字段名与通用来源单据类型字段同名，
 * 但本表语义扩展为"数据录入渠道"，开发时注意区分业务含义。
 */
public enum PrestockSourceTypeEnum implements EnumMessage {

    /**
     * 手动创建：运营人员在界面手动新增
     */
    MANUAL("MANUAL", "手动创建"),

    /**
     * 海外仓拉取：系统定时任务或接口回调时自动创建
     */
    OVERSEAS_WH("OVERSEAS_WH", "海外仓拉取"),
    ;

    @EnumValue
    @JsonValue
    private final String status;
    private final String name;

    PrestockSourceTypeEnum(String status, String name) {
        this.status = status;
        this.name = name;
    }

    /**
     * 根据 status 获取名称
     */
    public static String getName(String status) {
        if (StringUtils.isBlank(status)) {
            return "";
        }
        for (PrestockSourceTypeEnum item : PrestockSourceTypeEnum.values()) {
            if (status.equals(item.getStatus())) {
                return item.getName();
            }
        }
        return "";
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return status;
    }

    @Override
    public String getName() {
        return name;
    }
}
