package com.common.business.mask.protect;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库当前敏感字段值。
 *
 * @author cloud-erp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaskProtectCurrentValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private String value;
    private boolean nullValue;
}
