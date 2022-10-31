package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname TaskSearchDTO
 * @Description TODO
 * @Date 2022-09-21 14:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
@Validated
public class TaskSearchDTO implements Serializable {

    //搜索关键字
    /**
     * 搜索关键字
     */
    private String searchKeyword;

    //搜索类型

    /**
     * 搜索类型
     * name 任务名
     * priority 任务优先级
     * chargeId 负责人id
     * createTime 创建时间
     * status 状态
     */
    @StateEnumValue(strValues = {"name", "priority", "chargeId", "status",  "createTime"}, message = "搜索类型有误")
    private String searchType;

    /**
     * 任务优先级
     */
    private Integer priority;

    /**
     * 负责人id
     */
    private String chargeId;

    //状态

    /**
     * status 状态值 0:待发布 1:未开始 2:进行中 3 已完成, 4.完成待确认 5.审核中  6 审核通过 7 审核不通过
     */
    private Integer status;

    /**
     * 开始时间
     */
    private Date startTime;


    /**
     * 结束时间
     * @return
     */


    private Date endTime;




}
