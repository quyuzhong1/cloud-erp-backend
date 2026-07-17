package com.erp.model.plm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 产品违禁词检测请求响应实体
 */
@Data
@NoArgsConstructor
public class ProductForbiddenWordCheckDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetectDTO implements Serializable {

        /**
         * 检测记录id
         */
        private String id;

        /**
         * 报告名称
         */
        private String reportName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        /**
         * 勾选的id集合
         */
        private List<String> ids;

        /**
         * 检测状态
         */
        private Integer status;

        /**
         * 检测完成开始时间
         */
        private LocalDateTime finishStartTime;

        /**
         * 检测完成结束时间
         */
        private LocalDateTime finishEndTime;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建开始时间
         */
        private LocalDateTime createStartTime;

        /**
         * 创建结束时间
         */
        private LocalDateTime createEndTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListDTO implements Serializable {

        private String id;

        private String reportName;

        private String reportUrl;

        private Integer status;

        private String statusName;

        private LocalDateTime finishTime;

        private Integer totalCount;

        private Integer hitCount;

        private String failReason;

        private String createUserId;

        private String createUserName;

        private LocalDateTime createTime;

        private String updateUserId;

        private String updateUserName;

        private LocalDateTime updateTime;

        private Integer version;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductScanDTO implements Serializable {

        private String id;

        private String skuNo;

        private String name;

        private Integer status;
    }
}
