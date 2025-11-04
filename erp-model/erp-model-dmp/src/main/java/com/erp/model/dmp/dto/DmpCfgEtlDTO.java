package com.erp.model.dmp.dto;

import java.time.LocalDateTime;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * etl配置信息请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
*/
@Data
@NoArgsConstructor
public class DmpCfgEtlDTO implements Serializable {




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
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 应用id
        */
        private String appId;

        /**
        * 流程编号
        */
        private String flowCode;

        /**
        * 流程名称
        */
        private String flowName;

        /**
        * 最后成功时间
        */
        private LocalDateTime lastTime;

        /**
        * 下次执行结束时间
        */
        private LocalDateTime nextTime;

        /**
        * 间隔时间长度，单位秒
        */
        private Integer intervalTime;

        /**
        * 覆盖时间，单位秒
        */
        private Integer overrideTime;

        /**
        * 最大重试次数
        */
        private Integer maxRetryCount;

        /**
        * 执行超时时间，单位秒
        */
        private Integer execTimeout;

        /**
        * 延迟时间，单位秒
        */
        private Integer dealyTime;

        /**
        * 最大间隔时间长度单位:秒, 0=按interval_time，-1=按当前时间-延迟时间
        */
        private Integer maxIntervalTime;

        /**
        * 扩展json
        */
        private String extendJson;


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
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 应用id
        */
        @NotBlank(message = "应用id不能为空")
        @Size(max = 50,message = "应用id最大长度不能超过50位")
        private String appId;

        /**
        * 流程编号
        */
        @NotBlank(message = "流程编号不能为空")
        @Size(max = 50,message = "流程编号最大长度不能超过50位")
        private String flowCode;

        /**
        * 流程名称
        */
        @NotBlank(message = "流程名称不能为空")
        @Size(max = 100,message = "流程名称最大长度不能超过100位")
        private String flowName;

        /**
        * 最后成功时间
        */
        private LocalDateTime lastTime;

        /**
        * 下次执行结束时间
        */
        private LocalDateTime nextTime;

        /**
        * 间隔时间长度，单位秒
        */
        @NotNull(message = "间隔时间长度，单位秒不能为空")
        private Integer intervalTime;

        /**
        * 覆盖时间，单位秒
        */
        @NotNull(message = "覆盖时间，单位秒不能为空")
        private Integer overrideTime;

        /**
        * 最大重试次数
        */
        @NotNull(message = "最大重试次数不能为空")
        private Integer maxRetryCount;

        /**
        * 执行超时时间，单位秒
        */
        @NotNull(message = "执行超时时间，单位秒不能为空")
        private Integer execTimeout;

        /**
        * 延迟时间，单位秒
        */
        @NotNull(message = "延迟时间，单位秒不能为空")
        private Integer dealyTime;

        /**
        * 最大间隔时间长度单位:秒, 0=按interval_time，-1=按当前时间-延迟时间
        */
        @NotNull(message = "最大间隔时间长度单位:秒, 0=按interval_time，不能为空")
        private Integer maxIntervalTime;

        /**
        * 扩展json
        */
        private String extendJson;


    }

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;
    }


    /**
     * ETL配置列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 是否禁用
         */
        private Boolean disabled;

        /**
         * 应用id
         */
        private String appId;

        /**
         * 流程编号
         */
        private String flowCode;

        /**
         * 流程名称
         */
        private String flowName;

        /**
         * 流程分类
         */
        private String appCategory;

        /**
         * 最后成功时间
         */
        private LocalDateTime lastTime;

        /**
         * 下次执行结束时间
         */
        private LocalDateTime nextTime;

        /**
         * 间隔时间长度，单位秒
         */
        private Integer intervalTime;

        /**
         * 覆盖时间，单位秒
         */
        private Integer overrideTime;

        /**
         * 最大重试次数
         */
        private Integer maxRetryCount;

        /**
         * 执行超时时间，单位秒
         */
        private Integer execTimeout;

        /**
         * 延迟时间，单位秒
         */
        private Integer dealyTime;

        /**
         * 最大间隔时间, 0=按interval_time，-1=按当前时间-延迟时间
         */
        private Integer maxIntervalTime;

        /**
         * 扩展json
         */
        private String extendJson;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 修改时间【可排序】
         */
        private LocalDateTime updateTime;

        /**
         * 执行路径
         */
        private String execUrl;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

    }

    /**
     * ETL配置导出
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 是否禁用
         */
        private Boolean disabled;

        /**
         * 应用id
         */
        private String appId;

        /**
         * 流程编号
         */
        private String flowCode;

        /**
         * 流程名称
         */
        private String flowName;

        /**
         * 最后成功时间
         */
        private LocalDateTime lastTime;

        /**
         * 下次执行结束时间
         */
        private LocalDateTime nextTime;

        /**
         * 间隔时间长度，单位秒
         */
        private Integer intervalTime;

        /**
         * 覆盖时间，单位秒
         */
        private Integer overrideTime;

        /**
         * 最大重试次数
         */
        private Integer maxRetryCount;

        /**
         * 执行超时时间，单位秒
         */
        private Integer execTimeout;

        /**
         * 延迟时间，单位秒
         */
        private Integer dealyTime;

        /**
         * 最大间隔时间长度单位:秒, 0=按interval_time，-1=按当前时间-延迟时间
         */
        private Integer maxIntervalTime;

        /**
         * 扩展json
         */
        private String extendJson;


    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoTaskDTO extends PermissionsDTO {

        /**
         * 输入明细信息id列表
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

        /**
         * 拉取接口条件的开始时间
         */
        @NotNull(message = "开始时间不能为空")
        private LocalDateTime startTime;
        /**
         * 拉取接口条件的结束时间
         */
        @NotNull(message = "结束时间不能为空")
        private LocalDateTime endTime;

        /**
         * 执行超时时间，单位秒
         */
        private Integer execTimeout;

        /**
         * 是否切割时间(默认否)
         */
        private boolean splitFlag = false;

        /**
         * dmp_cfg_input_detail明细扩展参数
         */
        private String detailExtendJson;

        /**
         * 任务类型:
         * 来源:/dmp/common/enumDropDown?type=DmpInputTaskTaskType
         */
        private String taskType;

        /**
         * 下次执行任务时间
         */
        private LocalDateTime nextExecTime;

    }
}