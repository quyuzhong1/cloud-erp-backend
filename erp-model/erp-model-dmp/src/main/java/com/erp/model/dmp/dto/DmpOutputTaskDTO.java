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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;

import java.util.List;
import java.util.Map;

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
        private Map<String,String> sqlMap;

    }
    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

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
         * 推送类型
         */
        private String taskType;

        /**
         * 错误次数
         */
        private Integer errorCount;

        /**
         * 输入任务id
         */
        private String inputTaskId;

        /**
         * 执行超时时间，单位秒
         */
        private Integer execTimeout;

        /**
         * 推送类型
         */
        private String errorMessage;

        /**
         * 执行系统
         */
        private String execSystem;

        /**
         * 实例id
         */
        private String instanceId;

        /**
         * 代号
         */
        private String code;

        /**
         * 代号
         */
        private String approveStatus;


        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人名称
         */
        private String createUserName;
    }

    /**
     * 导出Excel
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
         * 勾选的id集合
         */
        private List<String> ids;
    }

}