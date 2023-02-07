package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 *  提交任务排期
 * @Classname SubmitTaskScheduleDTO
 * @Description TODO
 * @Date 2023-02-03 16:44
 * @Created by yl
 */
@NoArgsConstructor
@Data
public class HandleTaskScheduleDTO implements Serializable {


    /**
     * 产品id
     */
    private String productId;


    /**
     *  项目计划表id
     * @author yl
     * @date 2023-02-03 17:29
     * @param null
     * @return
     */
    private List<String>  projectPlanIdList;


    /**
     * 任务id 集合
     */
    private List<String> taskIdList;
}
