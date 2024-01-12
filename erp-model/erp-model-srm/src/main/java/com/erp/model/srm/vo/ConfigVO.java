package com.erp.model.srm.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ConfigVO
 * @description: TODO
 * @date 2024年01月10日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfigVO implements Serializable {

    private String id;
    /**
     * 编码
     */
    private String key;
    /**
     * 供应商id
     */
    private String supplierId;
    /**
     * 时长
     */
    private Integer duration;
    /**
     * 单位
     */
    private String unit;
    /**
     * 选中状态 true 启用 false 停用
     */
    private Boolean enable;
}
