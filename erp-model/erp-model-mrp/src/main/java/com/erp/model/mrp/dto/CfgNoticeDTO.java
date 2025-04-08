package com.erp.model.mrp.dto;

import cn.hutool.json.JSONObject;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class CfgNoticeDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

    }


    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 通知节点
         */
        private String noticeNode;
        /**
         * 通知节点名称
         */
        private String noticeNodeName;
        /**
         * 通知规则
         */
        private JSONObject noticeRule;
        /**
         * 通知规则名称
         */
        private String noticeRuleName;
        /**
         * 通知平台
         */
        private String noticePlatform;
        /**
         * 通知平台名称
         */
        private String noticePlatformName;

        /**
         * 是否禁用
         */
        private Boolean disabled;
        /**
         * 通知对象
         */
        private List<Map<String,Object>> noticeObjectList;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 创建人
         */
        private String createUserName;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 更新人
         */
        private String updateUserName;
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
         * 通知节点
         */
        private String noticeNode;

        /**
         * 通知规则
         */
        private NoticeRuleDTO noticeRule;

        /**
         * 通知平台
         */
        private String noticePlatform;

        /**
         * 是否禁用
         */
        private Boolean disabled;
        /**
         * 通知对象DTO
         */
        private List<NoticeObjectDTO> noticeObjectDTOList;
        /**
         * 通知时间DTO
         */
        private List<NoticeTimeDTO> noticeTimeDTOList;
    }

    /**
     * 通知对象DTO
     */
    @Data
    @NoArgsConstructor
    public static class NoticeObjectDTO {
        /**
         * id
         */
        private String id;
        /**
         * 通知类型, noticeShopCharge 店铺负责人，noticeUser通知人员
         */
        @NotBlank(message = "通知对象类型不能为空")
        private String noticeType;

        /**
         * 通知对象集合/api/plm/common/findUserList（按人员）
         */
        private List<String> noticeObjectList;
    }

    /**
     * 通知时间DTO
     */
    @Data
    @NoArgsConstructor
    public static class NoticeTimeDTO {
        /**
         * id
         */
        private String id;
        /**
         * 通知类型,/sys/dictBasic/list?type=noticeTimeType
         */
        @NotBlank(message = "通知对象类型不能为空")
        private String noticeType;

        /**
         * 周选项,/sys/dictBasic/list?type=weekOption
         */
        private String weekOption;

        /**
         * 时间
         */
        @NotNull(message = "发送时间不能为空")
        private LocalTime time;
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
         * 通知节点,/sys/dictBasic/list?type=noticeNodeType
         */
        @NotBlank(message = "通知节点不能为空")
        @Size(max = 32,message = "通知节点最大长度不能超过32位")
        private String noticeNode;

        /**
         * 通知规则
         */
        @NotNull(message = "通知规则不能为空")
        private NoticeRuleDTO noticeRule;

        /**
         * 通知平台
         */
        private String noticePlatform;

        /**
         * 通知对象DTO
         */
        @NotEmpty(message = "通知对象不能为空")
        @Valid
        private List<NoticeObjectDTO> noticeObjectDTOList;

        /**
         * 通知时间DTO
         */
        @NotEmpty(message = "通知时间不能为空")
        @Valid
        private List<NoticeTimeDTO> noticeTimeDTOList;
    }

    /**
     * 更新启禁用DTO
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDisabledDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 是否禁用
         */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

    }

    /**
     * 通知规则
     */
    @Getter
    @Setter
    public static class NoticeRuleDTO {

        /**
         * 通知类型
         * platform 平台
         * shop 店铺
         */
        @NotBlank(message = "通知类型不能为空")
        private String ruleType;

        /**
         * 店铺id或平台字典
         */
        @Size(min = 1, message = "店铺id或平台字典不能为空")
        private List<String> channelId;
    }
}
