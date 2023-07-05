package com.common.business.enums;

public enum DataAttributeEnum {
    /**
     * 通过普通字段控制权限 normal
     */
    LIST,
    /**
     * 通过主键控制权限 primary_key
     */
    CHECK_BY_PARAM,

    /**
     * 通过ID字段控制权限 id
     */
    CHECK_BY_ID
}
