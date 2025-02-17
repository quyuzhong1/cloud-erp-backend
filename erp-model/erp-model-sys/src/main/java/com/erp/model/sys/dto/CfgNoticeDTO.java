package com.erp.model.sys.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 通知配置表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-02-13
*/
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
        private String noticeRule;
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
        private String noticeRule;

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
        private List<NoticeTimeDTO> noticeRuleDTOList;
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
         * 通知类型
         */
        private String noticeType;

        /**
         * 通知对象集合
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
         * 通知类型
         */
        private String noticeType;

        /**
         * 周选项
         */
        private String weekOption;

        /**
         * 时间
         */
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
        * 通知节点
        */
        @NotBlank(message = "通知节点不能为空")
        @Size(max = 32,message = "通知节点最大长度不能超过32位")
        private String noticeNode;

        /**
        * 通知规则
        */
        @NotBlank(message = "通知规则不能为空")
        @Size(max = 32,message = "通知规则最大长度不能超过32位")
        private String noticeRule;

        /**
        * 通知平台
        */
        @NotBlank(message = "通知平台不能为空")
        @Size(max = 32,message = "通知平台最大长度不能超过32位")
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
}