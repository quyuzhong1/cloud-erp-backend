package com.erp.model.sys.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Author: wtr
 * @Date: 2026/4/10 12:14
 * @Param:
 * @Return:
 * @Description:
 **/
public class SysVersionDTO {

    @Data
    @NoArgsConstructor
    public static class AddDTO{

        /**
         * 消息类型
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
    public static class ListDTO {

        /**
         * id
         */
        private String id;

        /**
         * 版本号
         */
        private String upgradeVersion;

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
         * 过期时间
         */
        private LocalDateTime expireTime;
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
    public static class ListHistoryVersionDTO{

        /**
         * id
         */
        private String id;

        /**
         * 版本号
         */
        private String upgradeVersion;

        /**
         * 通知时间
         */
        private LocalDateTime noticeTime;

        /**
         * 内容
         */
        private String dataJson;

        /**
         * 是否已读
         */
        private Boolean isRead;
    }

    @Data
    @NoArgsConstructor
    public static class HistoryVersionPagingParamDTO{

    }

    @Data
    @NoArgsConstructor
    public static class ReadHistoryVersionDTO{
        /**
         * 消息id
         */
        private String messageId;
    }

    @Data
    @NoArgsConstructor
    public static class LatestVersionDTO{

        /**
         * id
         */
        private String id;

        /**
         * 版本号
         */
        private String upgradeVersion;

        /**
         * 内容
         */
        private String dataJson;
    }
}
