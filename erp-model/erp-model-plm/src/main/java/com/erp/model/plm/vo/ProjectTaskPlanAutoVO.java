package com.erp.model.plm.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


/**
 * @author Cloud
 */
@Data
@NoArgsConstructor
public class ProjectTaskPlanAutoVO implements Serializable {

    /**
     * 成功条数
     */
    private Integer succeedCount;
    /**
     * 失败条数
     */
    private Integer failureCount;
    /**
     * 错误信息
     */
    private List<ScheduleVO> errorList;

    @Data
    @NoArgsConstructor
    public static class ScheduleVO {
        /**
         * 任务名称
         */
        private String name;
        /**
         * 任务类型
         */

        private String type;
        /**
         * 错误原因
         */

        private String errorMsg;
    }


}
