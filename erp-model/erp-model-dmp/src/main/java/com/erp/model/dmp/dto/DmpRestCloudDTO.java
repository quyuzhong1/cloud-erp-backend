package com.erp.model.dmp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * RestCloud请求响应实体
 * </p>
 *
 */
@Data
@NoArgsConstructor
public class DmpRestCloudDTO implements Serializable {

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 流程名称+流程路径
         */
        private String fullName;

        /**
         * 流程名称
         */
        private String flowName;

        /**
         * 流程执行全路径
         */
        private String execUrl;

        /**
         * 流程代号
         */
        private String flowCode;

        /**
         * 应用id
         */
        private String appId;



    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 流程明细(精准)
         */
        private String flowName;

        /**
         * 流程路径(精准)
         */
        private String flowUrl;

        /**
         * 搜索关键字(模糊)(流程名称或路径)
         */
        private String searchKey;

        /**
         * 任务配置类型: input=拉取配置,output=推送配置, etl=清洗调度(精准)
         */
        @Pattern(regexp = "input|output|etl", message = "任务配置类型只能为input、output或etl")
        @NotBlank(message = "任务配置类型不能为空")
        private String taskCfgType;

    }

}