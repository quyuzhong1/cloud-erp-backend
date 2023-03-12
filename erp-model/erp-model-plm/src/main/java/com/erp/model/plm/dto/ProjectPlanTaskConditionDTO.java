package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 项目计划任务列表条件
 *
 * @Classname
 * @Description TODO
 * @Date 2023-02-03 15:31
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProjectPlanTaskConditionDTO extends SortDTO {


    /**
     * 产品id
     */
    @NotBlank(message = "产品id 不能为空")
    private String productId;

    /**
     * 阶段
     * all 所有
     * projectApproval 立项阶段
     * project 项目阶段
     */
    @StateEnumValue(strValues = {"all", "projectApproval", "project"}, message = "阶段有误")
    private String phase;


    /**
     * 搜索关键字
     */
    private String searchKeyword;

    /**
     * 任务负责人
     */
    private String taskChargeId;

    /**
     * 任务排期审核状态
     */
    private String planAuditState;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime planStartTime;


    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime planEndTime;


    private List<String> taskIdList;


}
