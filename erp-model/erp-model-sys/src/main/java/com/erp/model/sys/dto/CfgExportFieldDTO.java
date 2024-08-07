package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class CfgExportFieldDTO implements Serializable {

    /**
     * 字段归属menuCode
     */
    private String menuCode;

    /**
     * 字段
     */
    private String field;

    /**
     * 字段名称
     */
    private String fieldName;
}
