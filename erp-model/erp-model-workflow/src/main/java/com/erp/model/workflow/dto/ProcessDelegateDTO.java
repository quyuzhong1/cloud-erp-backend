package com.erp.model.workflow.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 委托审批请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class ProcessDelegateDTO implements Serializable {


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
         * 类型
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
         * 主键id【可排序】
         */
        private String  id;

        /**
         * 委托编号【可排序】
         */
        private String  code;

        /**
         * 委托状态【可排序】
         */
        private String status;
        /**
         * 委托状态名称
         */
        private String statusName;

        /**
         * 单据类型【可排序】
         */
        private String businessKey;

        /**
         * 单据类型名称
         */
        private String businessKeyName;

        /**
         * 发起人id【可排序】
         */
        private String startUserId;
        /**
         * 发起人名称
         */
        private String startUserName;

        /**
         * 委托人ID【可排序】
         */
        private String delegateUserId;

        /**
         * 委托人名称
         */
        private String delegateUserName;

        /**
         * 委托生效时间
         */
        private LocalDateTime effectiveTime;

        /**
         * 委托失效时间【可排序】
         */
        private LocalDateTime expireTime;

        /**
         * 创建人ID【可排序】
         */
        private String createUserId;

        /**
         * 创建人姓名【可排序】
         */
        private String createUserName;
        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;
        /**
         * 是否自动终止
         */
        private Boolean isAuto;
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
        * 委托编号
        */
        private String code;

        /**
        * 委托状态
        */
        private String status;

        /**
        * 委托流程id(流程定义id)
        */
        private String businessKey;

        /**
        * 发起人ID
        */
        private String startUserId;

        /**
        * 委托人ID
        */
        private String delegateUserId;

        /**
        * 委托生效时间
        */
        private LocalDateTime effectiveTime;

        /**
        * 委托失效时间
        */
        private LocalDateTime expireTime;

        /**
        * 终止时间
        */
        private LocalDateTime closedTime;


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
    public static class UpdateDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;


        /**
         * 委托流程（单据类型）
         */
        @NotBlank(message = "委托流程（单据类型）不能为空")
        private String businessKey;

        /**
         * 发起人ID
         */
        @NotBlank(message = "发起人ID不能为空")
        @Size(max = 19,message = "发起人ID最大长度不能超过19位")
        private String startUserId;

        /**
         * 委托人ID
         */
        @NotBlank(message = "委托人ID不能为空")
        @Size(max = 19,message = "委托人ID最大长度不能超过19位")
        private String delegateUserId;

        /**
         * 委托生效时间
         */
        @NotNull(message = "委托生效时间不能为空")
        private LocalDateTime effectiveTime;

        /**
         * 委托失效时间
         */
        @NotNull(message = "委托失效时间不能为空")
        private LocalDateTime expireTime;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 委托流程（单据类型）
        */
        @NotEmpty(message = "委托流程（单据类型）不能为空")
        private List<String> businessKeyList;

        /**
        * 发起人ID
        */
        @NotBlank(message = "发起人ID不能为空")
        @Size(max = 19,message = "发起人ID最大长度不能超过19位")
        private String startUserId;

        /**
        * 委托人ID
        */
        @NotBlank(message = "委托人ID不能为空")
        @Size(max = 19,message = "委托人ID最大长度不能超过19位")
        private String delegateUserId;

        /**
        * 委托生效时间
        */
        @NotNull(message = "委托生效时间不能为空")
        private LocalDateTime effectiveTime;

        /**
        * 委托失效时间
        */
        @NotNull(message = "委托失效时间不能为空")
        private LocalDateTime expireTime;

    }


}