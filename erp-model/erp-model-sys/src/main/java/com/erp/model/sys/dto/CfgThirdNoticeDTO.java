package com.erp.model.sys.dto;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 三方通知配置请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-05-23
*/
@Data
@NoArgsConstructor
public class CfgThirdNoticeDTO implements Serializable {




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
        * 单据类型
        */
        private String businessType;
        private String businessTypeName;


        /**
        * 通知方式：single=单条,summary=汇总
        */
        private String method;
        private String methodName;

        /**
        * 通知状态
        */
        private Boolean noticeStatus;

        /**
        * 通知标题
        */
        private String title;

        /**
        * 通知类型
        */
        private String noticeType;
        private String noticeTypeName;

        /**
        * 跳转链接
        */
        private String url;

        /**
        * cron
        */
        private String cron;

        /**
         * 岗位
         */
        private String post;
        private List<String> postIdList;
        private List<String> postNameList;
        /**
        * 通知人员
        */
        private String roleType;
        private List<String> roleTypeList;
        private List<String> roleTypeNameList;

        /**
        * 具体人员
        */
        private String specificPerson;
        private List<String> specificPersonList;
        private List<String> specificPersonNameList;


        /**
        * 推送方式
        */
        private String noticeMethod;
        private List<String> noticeMethodList;

        /**
         * 推送信息
         */
        private List<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> pushMsgList;

        /**
         * 规则条件
         */
        @Dict
        private List<CfgRuleConditionDTO.View> conditionList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 规则条件集合
         */
        private List<CfgRuleConditionDTO.Add> conditionList;
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

        /**
         * 规则条件
         */
        private List<CfgRuleConditionDTO.Update> conditionList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 单据类型
        */
        @NotBlank(message = "单据类型不能为空")
        private String businessType;


        /**
        * 通知方式：single=单条,summary=汇总
        */
        @NotBlank(message = "通知方式不能为空")
        private String method;

        /**
        * 通知状态
        */
        @NotNull(message = "通知状态不能为空")
        private Boolean noticeStatus;

        /**
        * 通知标题
        */
        @NotBlank(message = "通知标题不能为空")
        @Size(max = 200,message = "通知标题最大长度不能超过200位")
        private String title;

        /**
        * 通知类型
        */
        @NotBlank(message = "通知类型不能为空")
        @Size(max = 50,message = "通知类型最大长度不能超过50位")
        private String noticeType;

        /**
        * 跳转链接
        */
        @Size(max = 500,message = "跳转链接最大长度不能超过500位")
        private String url;

        /**
        * cron
        */
        private String cron;

        /**
        * 通知人员
        */
        private List<String> roleTypeList;

        /**
         * 岗位
         */
        private List<String> postIdList;

        /**
        * 具体人员
        */
        private List<String>  specificPersonList;

        /**
        * 推送方式
        */
        @NotEmpty(message = "推送方式不能为空")
        private List<String> noticeMethodList;

        /**
         * 推送信息
         */
        private List<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> pushMsgList;
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
         * 类型
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends BaseDTO {
        /**
         * 单据类型
         */
        private String businessType;
        /**
         * 单据名称
         */
        private String businessTypeName;

        /**
         * 通知方式：single=单条,summary=汇总
         */
        private String method;
        private String methodName;

        /**
         * 通知状态
         */
        private Boolean noticeStatus;
        private String noticeStatusName;

        /**
         * 通知类型
         */
        private String noticeType;

        /**
         * 岗位
         */
        private String post;
        private List<String> postIdList;
        private List<String> postNameList;

        /**
         * 项目角色
         */
        private String roleType;
        private List<String> roleTypeList;
        private List<String> roleTypeNameList;

        /**
         * 具体人员
         */
        private String specificPerson;
        private List<String> specificPersonList;
        private List<String> specificPersonNameList;

        /**
         * 通知标题
         */
        private String title;

        /**
         * 通知时间类型
         */
        private String cron;
        private String cronType;
    }

    @Data
    @NoArgsConstructor
    public static class BaseDTO {
        /**
         * id
         */
        private String id;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class EnableStatusDTO {

        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

        @NotNull(message = "通知状态不能为空")
        private Boolean noticeStatus;

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

        /**
         * 主键id
         */
        private List<String> ids;

    }



}