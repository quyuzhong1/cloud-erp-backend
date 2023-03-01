package com.erp.model.plm.dto;

import com.erp.model.plm.enums.TaskRelationshipEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author Cloud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreTaskUpdateDTO {


    /**
     * 关系Id
     */
    @NotBlank(message = "关系ID不能为空")
    private String id;
    /**
     * 依赖关系
     */
    @NotBlank(message = "依赖关系不能为空")
    private String relationshipCode;
    /**
     * 间隔工期
     */
    private Integer intervalWorkPeriod;
}