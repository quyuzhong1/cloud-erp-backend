package com.erp.model.sys.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.business.dto.base.SortDTO;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;

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
        public String latestTime;
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
         * 参数json
         */
        private LinkedList<LinkedHashMap<String, Object>> dataJson;
    }

    /**
     * 是否存在新的未读消息
     */
    @Data
    @NoArgsConstructor
    public static class IsMessageDTO {
        private String remark;
    }
}