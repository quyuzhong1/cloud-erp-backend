package com.erp.model.sys.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
//        /**
//         * 参数json
//         */
//        private LinkedHashMap<String, Object> dataJson;
        /**
         //         * 参数json
         //         */
        private String dataJson;
    }

    /**
     * 是否存在新的未读消息
     */
    @Data
    @NoArgsConstructor
    public static class IsMessageDTO {
        public String remark;
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
         * 通知类型
         */
        private String type;

        /**
         * 数据集json
         */
        private String dataJson;

        /**
         * 应用类型：PDA、PC
         */
        private String application;

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
    public static class UpdateDTO{

        private String id;

        /**
         * 通知类型
         */
        private String type;

        /**
         * 数据集json
         */
        private String dataJson;

        /**
         * 应用类型：PDA、PC
         */
        private String application;

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
         * 通知类型
         */
        private String type;

        /**
         * 通知类型名称
         */
        private String typeName;

        /**
         * 应用类型：PDA、PC
         */
        private String application;

        /**
         * 应用类型：PDA、PC
         */
        private String applicationName;

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
    public static class HistoryMessagePagingParamDTO{
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
    public static class ListHistoryMessageDTO{

    }

    @Data
    @NoArgsConstructor
    public static class ReadHistoryMessageDTO{

        /**
         * 消息id
         */
        private String messageId;
    }
}