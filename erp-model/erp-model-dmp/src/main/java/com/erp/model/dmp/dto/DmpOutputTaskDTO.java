package com.erp.model.dmp.dto;

import java.time.LocalDateTime;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 推送任务请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-07-01
*/
@Data
@NoArgsConstructor
public class DmpOutputTaskDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 推送数据配置id
        */
        private String cfgOutputId;

        /**
        * 推送下一层级id
        */
        private String nextLevelId;

        /**
        * 推送接口条件的开始时间
        */
        private LocalDateTime startTime;

        /**
        * 推送接口条件的结束时间
        */
        private LocalDateTime endTime;

        /**
        * 推送状态：init=待推送,finish=已推送,error=推送失败
        */
        private String status;

        /**
        * 推送报文
        */
        private String requestData;

        /**
        * 响应报文
        */
        private String responseData;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 推送数据配置id
        */
        @NotBlank(message = "推送数据配置id不能为空")
        @Size(max = 19,message = "推送数据配置id最大长度不能超过19位")
        private String cfgOutputId;

        /**
        * 推送下一层级id
        */
        @NotBlank(message = "推送下一层级id不能为空")
        @Size(max = 50,message = "推送下一层级id最大长度不能超过50位")
        private String nextLevelId;

        /**
        * 推送接口条件的开始时间
        */
        private LocalDateTime startTime;

        /**
        * 推送接口条件的结束时间
        */
        private LocalDateTime endTime;

        /**
        * 推送状态：init=待推送,finish=已推送,error=推送失败
        */
        @NotBlank(message = "推送状态：init=待推送,finish=已推送,error=推送失败不能为空")
        @Size(max = 50,message = "推送状态：init=待推送,finish=已推送,error=推送失败最大长度不能超过50位")
        private String status;

        /**
        * 推送报文
        */
        private String requestData;

        /**
        * 响应报文
        */
        private String responseData;


    }
}