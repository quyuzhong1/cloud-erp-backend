package com.erp.model.dmp.dto;

import java.time.LocalDateTime;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import java.util.List;
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
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * <p>
 * 推送数据配置明细请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@NoArgsConstructor
public class DmpCfgOutputDetailDTO implements Serializable {


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
         * 输出信息名称
         */
        private String cfgOutputName;

        /**
         * 系统代号
         */
        private String systemCode;

        /**
         * 系统名称
         */
        private String systemName;

        /**
         * 执行url
         */
        private String execUrl;

        /**
         * 执行系统
         */
        private String execSystem;

        /**
        * 主键id
        */
        private String  id;

        /**
        * 输出信息id
        */
        private String mainId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 最后成功时间
        */
        private LocalDateTime lastTime;

        /**
        * 下次执行结束时间
        */
        private LocalDateTime nextTime;

        /**
        * 间隔时间长度单位秒
        */
        private Integer intervalTime;

        /**
        * 覆盖时间单位秒
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
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 扩展json
        */
        private String extendJson;

        private String remark;

        /**
        * 延迟时间
        */
        private Integer dealyTime;

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
        * 输出信息id
        */
        private String mainId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 最后成功时间
        */
        private LocalDateTime lastTime;

        /**
        * 下次执行结束时间
        */
        private LocalDateTime nextTime;

        /**
        * 间隔时间长度单位秒
        */
        private Integer intervalTime;

        /**
        * 覆盖时间单位秒
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
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 扩展json
        */
        private String extendJson;

        private String remark;

        /**
        * 延迟时间
        */
        private Integer dealyTime;

        /**
        * 代号
        */
        private String code;

        /**
        * 代号
        */
        private String approveStatus;


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
        * 输出信息id
        */
        @NotBlank(message = "输出信息id不能为空")
        @Size(max = 50,message = "输出信息id最大长度不能超过50位")
        private String mainId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 50,message = "下一层级id最大长度不能超过50位")
        private String nextLevelId;

        /**
        * 最后成功时间
        */
        private LocalDateTime lastTime;

        /**
        * 下次执行结束时间
        */
        private LocalDateTime nextTime;

        /**
        * 间隔时间长度单位秒
        */
        @NotNull(message = "间隔时间长度单位秒不能为空")
        private Integer intervalTime;

        /**
        * 覆盖时间单位秒
        */
        @NotNull(message = "覆盖时间单位秒不能为空")
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
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

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
         * 任务类型:默认: normal 正常任务
         * 来源:/dmp/common/enumDropDown?type=DmpOutputTaskTaskType
         */
        private String taskType;

        /**
         * 下次执行任务时间
         */
        private LocalDateTime nextExecTime;

        public String getCheckAndDetailExtendJson() {
            if (StringUtils.isBlank(this.detailExtendJson)){
                return this.detailExtendJson;
            }
            try {
                JSONUtil.parse(this.detailExtendJson);
                return this.detailExtendJson;
            } catch (Exception e) {
                throw new IllegalArgumentException("detailExtendJson不是合法的json格式");
            }
        }
    }
}