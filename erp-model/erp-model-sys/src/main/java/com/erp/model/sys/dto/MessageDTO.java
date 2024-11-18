package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

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
        private LinkedHashMap<String, Object> dataJson;
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

    }
}