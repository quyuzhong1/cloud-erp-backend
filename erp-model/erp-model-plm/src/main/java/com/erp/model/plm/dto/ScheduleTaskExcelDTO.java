package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname ScheduleTaskExcelDTO
 * @Description TODO
 * @Date 2023-02-23 19:51
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ScheduleTaskExcelDTO  implements Serializable {


    private String productId;


    private String taskId;


    private Date planStartTime;


    private Date planEndTime;


    private String chargeId;
}
