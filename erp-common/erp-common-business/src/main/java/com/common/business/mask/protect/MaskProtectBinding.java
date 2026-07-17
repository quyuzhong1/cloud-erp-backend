package com.common.business.mask.protect;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一个读侧脱敏字段对应的保存入参 DTO 绑定。
 *
 * @author cloud-erp
 */
@Data
@NoArgsConstructor
public class MaskProtectBinding implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 保存接口入参 DTO 类路径。
     */
    private String paramClassPath;

    /**
     * 保存接口入参 DTO 字段名。
     */
    private String paramFieldName;

    /**
     * 保存接口入参 DTO 中用于定位记录主键的字段名。
     */
    private String paramRecordIdField;

}
