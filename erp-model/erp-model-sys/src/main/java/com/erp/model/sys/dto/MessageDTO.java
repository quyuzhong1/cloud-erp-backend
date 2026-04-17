package com.erp.model.sys.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 消息通知表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-10
*/
@Data
@NoArgsConstructor
public class MessageDTO implements Serializable {

    /**
     * 未读消息数DTO
     */
    @Data
    @NoArgsConstructor
    public static class NotReadMessageNum {
        /**
         * 消息来源类型
         */
        public String type;
        /**
         * 类型名称
         */
        public String typeName;
        /**
         * 描述
         */
        public String typeRemark;
        /**
         * 单据数量
         */
        public Integer count;
        /**
         * 最新的消息时间
         */
        public LocalDateTime latestTime;
    }

    /**
     * 未读消息详情DTO
     */
    @Data
    @NoArgsConstructor
    public static class NotReadMessageNumDetail {
        /**
         * 消息id
         */
        public String id;
        /**
         * 是否已读
         */
        public Boolean isRead;
        /**
         * 创建数据
         */
        public LocalDateTime createTime;
        /**
         * 参数json
         */
        private String dataJson;
    }

    /**
     * 是否存在新的未读消息
     */
    @Data
    @NoArgsConstructor
    public static class IsMessageDTO {
        public String remark;
        private Boolean hasNewNotice;
        private NoticeDTO latestNotice;
    }

    @Data
    @NoArgsConstructor
    public static class NoticeDTO implements Serializable {
        private String id;
        private String releaseType;
        private String messageType;
        private String application;
        private String noticeTitle;
        private String content;
        private LocalDateTime noticeTime;
        private LocalDateTime expireTime;
    }

    /**
     * 条件查询
     */
    @Data
    @NoArgsConstructor
    public static class PdaParamDTO {
        /**
         * 用户id
         */
        public String userId;

        /**
         * 类型
         */
        public String type;

        /**
         * 应用：PDA、PC
         */
        public List<String> application;
        
        public Integer pageSize;
        
        public Integer offSet;

    }

    @Data
    @NoArgsConstructor
    public static class AddDTO{

        /**
         * 通知类型(当前字段为message的type字段，对应pda_version的release_type字段)
         *
         */
        private String releaseType;

        /**
         * 通知内容
         */
        private String dataJson;

        /**
         * 通知系统：PDA、PC(当前字段为message的application字段，对应pda_version的type字段)
         *
         */
        private String type;

        /**
         * 通知标题
         */
        public String noticeTitle;

        /**
         * 通知时间类型 /sys/dictBasic/list?type=noticeTiming
         */
        public String noticeTimeType;

        /**
         * 通知时间
         */
        public LocalDateTime noticeTime;

        /**
         * 升级版本号
         */
        public String upgradeVersion;

        /**
         * 过期时间
         */
        public LocalDateTime expireTime;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO{

        private String id;

        /**
         * 通知类型(当前字段为message的type字段，对应pda_version的release_type字段)
         *
         */
        private String releaseType;

        /**
         * 数据集json
         */
        private String dataJson;

        /**
         * 应用类型：PDA、PC(当前字段为message的application字段，对应pda_version的type字段)
         *
         */
        private String type;

        /**
         * 通知标题
         */
        public String noticeTitle;

        /**
         * 通知时间类型
         */
        public String noticeTimeType;

        /**
         * 通知时间
         */
        public LocalDateTime noticeTime;

        /**
         * 升级版本号
         */
        public String upgradeVersion;

        /**
         * 过期时间
         */
        public LocalDateTime expireTime;
    }

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

    @Data
    @NoArgsConstructor
    public static class ViewDTO{

        /**
         * id
         */
        private String id;

        /**
         * 通知类型(当前字段为message的type字段，对应pda_version的release_type字段)
         * ReleaseTypeEnum
         */
        private String releaseType;

        /**
         * 通知类型名称
         * ReleaseTypeEnum
         */
        private String releaseTypeName;

        /**
         * 应用类型：PDA、PC(当前字段为message的application字段，对应pda_version的type字段)
         */
        private String type;

        /**
         * 应用类型：PDA、PC MessageTypeEnum
         */
        private String typeName;

        /**
         * 通知标题
         */
        public String noticeTitle;

        /**
         * 通知时间类型
         */
        public String noticeTimeType;

        /**
         * 通知时间类型名称
         */
        public String noticeTimeTypeName;

        /**
         * 通知时间
         */
        public LocalDateTime noticeTime;

        /**
         * 过期时间
         */
        public LocalDateTime expireTime;

        /**
         * 数据集json
         */
        private String dataJson;

    }

    @Data
    @NoArgsConstructor
    public static class HistoryMessagePagingParamDTO extends SortDTO {
        /**
         * id
         */
        private String id;

        /**
         * 标题
         */
        private String noticeTitle;

        /**
         * 通知类型
         */
        private String type;

        /**
         * 通知类型名称
         */
        private String typeName;

        /**
         * 通知时间
         */
        private String noticeTime;

        /**
         * 通知内容
         */
        private String dataJson;

        /**
         * 是否已读
         */
        private Boolean isRead;
        
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class ListHistoryMessageDTO{
        /**
         * id
         */
        private String id;

        /**
         * 标题
         */
        private String noticeTitle;

        /**
         * 通知类型
         */
        private String type;

        /**
         * 通知类型名称
         */
        private String typeName;

        /**
         * 通知时间
         */
        private String noticeTime;

        /**
         * 通知内容
         */
        private String dataJson;

        /**
         * 是否已读
         */
        private Boolean isRead;
    }

    @Data
    @NoArgsConstructor
    public static class ReadHistoryMessageDTO{

        /**
         * 消息id
         */
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class DeleteDTO{

        /**
         * id
         */
        @NotBlank(message = "id不能为空")
        private String id;

        @NotBlank(message = "通知类型不能为空")
        private String releaseType;
    }
}
