package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    @NotBlank(message = "产品id不能为空")
    private String productId;





    /**
     * 任务id 集合
     */
    @NotNull(message = "任务id集合不能为空")
    private List<String> taskIdList;
}
