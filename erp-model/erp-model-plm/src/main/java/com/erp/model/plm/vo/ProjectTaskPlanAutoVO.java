package com.erp.model.plm.vo;

import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.plm.dto.ProjectPlanTaskDTO;
import com.erp.model.plm.entity.ProjectPlanTaskEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


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
    /**
     * 错误信息
     */
    private List<ScheduleDateVO> sucessList;

    public ProjectTaskPlanAutoVO(List<ScheduleVO> errorList, List<ScheduleDateVO> sucessList) {
        this.succeedCount = sucessList.size();
        this.failureCount = errorList.size();
        this.sucessList = sucessList;
        this.errorList = errorList;
    }

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

        private String type = "任务";
        /**
         * 错误原因
         */

        private String errorMsg;

        public ScheduleVO(String taskName, String msg) {
            this.name = taskName;
            this.errorMsg = msg;
        }
    }
    @Data
    @NoArgsConstructor
    public static class ScheduleDateVO {

        private String id;
        /**
         * 任务名称
         */
        private LocalDate startDate;
        /**
         * 任务名称
         */
        private LocalDate endDate;

        public ScheduleDateVO(String id, Date startTime, Date endTime) {
            this.id = id;
            this.startDate = LocalDateUtil.date2LocalDate(startTime);
            this.endDate = LocalDateUtil.date2LocalDate(endTime);

        }
    }


}
