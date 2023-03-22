package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTaskSearchDTO {
    /**
     * 模板id
     */
    private String templateId;

    /**
     * 任务阶段
     */
    private List<String> phaseNames;

    /**
     * 任务负责人
     */
    private List<String> chargeNames;

    /**
     * 任务类型 0 一般任务 1：审核任务
     */
    private Integer TaskType;

    /**
     * 目标交付文档
     */
    private  List<String> docsNames;

    /**
     * 是否是固定任务 1 是  0 不是
     */
    private Integer isFixed;

    /**
     * 是否是固定任务 1 是  0 不是
     */
    private String taskName;
}
