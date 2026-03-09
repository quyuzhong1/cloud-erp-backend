package com.erp.model.tms.dto;

import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 异步任务记录请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-01-28
*/
@Data
@NoArgsConstructor
public class AsyncTaskRecordDTO implements Serializable {



     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TaskDTO {

         /**
         *
         */
         private List<String> ids;

         /**
          *核算日期
          */
         private String reportDate;
         /**
          * 主任务id
          */
         private String taskId;
         /**
          * 单据类型
          */
         private String businessType;
         /**
          *物流标签类型
          */
         private String type;
     }


    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
        * 单据名称
        */
        @NotBlank(message = "单据名称不能为空")
        @Size(max = 50,message = "单据名称最大长度不能超过50位")
        private String businessType;

        /**
        * 开始时间
        */
        @NotNull(message = "开始时间不能为空")
        private LocalDateTime startTime;

        /**
        * 结束时间
        */
        @NotNull(message = "结束时间不能为空")
        private LocalDateTime endTime;

        /**
        * 状态：success=成功,part_success=部分成功,  failed=失败
        */
        @NotBlank(message = "状态：success=成功,part_success=部分成功,  failed=失败不能为空")
        @Size(max = 50,message = "状态：success=成功,part_success=部分成功,  failed=失败最大长度不能超过50位")
        private String status;

        /**
        * json
        */
        private String dataJson;


    }


}