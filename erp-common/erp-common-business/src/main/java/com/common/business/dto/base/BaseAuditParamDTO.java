package com.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/15 18:16
 */
@Data
@NoArgsConstructor
public class BaseAuditParamDTO {

    /**
     * 主键id集合
     */
    private List<String> ids;

    /**
     * 类型（auditPass、审核通过，auditNoPass、审核不通过）
     */
    private String type;

    /**
     * 意见
     */
    private String comment;
}
