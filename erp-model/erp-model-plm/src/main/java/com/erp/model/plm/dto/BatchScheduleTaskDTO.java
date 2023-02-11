package com.erp.model.plm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Classname BatchScheduleTaskDTO
 * @Description TODO
 * @Date 2023-02-10 16:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BatchScheduleTaskDTO  implements Serializable {

    @NotBlank(message = "产品id不能为空")
    private String productId;
    /**
     * 任务id
     */
    @NotNull(message = "任务id 不能为空")
    @Size(min = 1,message = "任务至少有一个")
    private List<String> taskIdList;


    @NotNull(message = "任务负责人集合不能为空")
    @Size(min = 1,message = "负责人至少有一个")
    private List<String> chargeIds;


    /**
     * 任务阶段id
     */
    @NotBlank(message = "阶段id 不能为空")
    private String phaseId;


    /**
     * 前置任务id
     */
    private List<String> preTaskIdList;


    /**
     * 任务优先级 1 低级 2 中级 3 高级
     */
    private Integer priority;


    /**
     * 设置里程碑(0否，1是)
     */
    private Integer isMilepost;


    /**
     * 关联sku 表id集合
     */
    private List<String> refSkuIdList;

    /**
     * 计划开始时间
     */

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planStartTime;

    /**
     * 计划结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planEndTime;


    /**
     * 任务描述
     */
    private String description;

}
