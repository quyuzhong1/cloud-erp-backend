package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
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


}