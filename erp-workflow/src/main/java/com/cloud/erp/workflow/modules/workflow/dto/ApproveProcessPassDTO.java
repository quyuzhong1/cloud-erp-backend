package com.cloud.erp.workflow.modules.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Classname 审核任务
 * @Description TODO
 * @Date 2022-08-11 11:36
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ApproveProcessPassDTO extends ProcessBaseDTO {




    //参数
    private Map<String, Object> parameterMap;
}
