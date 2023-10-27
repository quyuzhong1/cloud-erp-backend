package com.erp.model.dmp.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 中台同步任务表
 * @date 2023/10/13 12:19
 */
@Data
@NoArgsConstructor
public class DmpPullTaskDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 类型，all全部、2同步中、3同步成功、4同步失败
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamDTO extends PermissionsDTO{

        /**
         * 导出ids查询
         */
        private List<String> ids;

        /**
         * 来源类型
         */
        private List<String> sourceTypeList;

        /**
         * 来源编号
         */
        private String sourceCode;

        /**
         * 目标平台
         */
        private String targetPlatformName;

        /**
         * 来源平台
         */
        private String sourcePlatformName;

        /**
         * 同步状态
         */
        private List<String> statusList;

        /**
         * 推送失败原因
         */
        private String returnMsg;

        /**
         * 同步时间
         */
        private List<LocalDate> lastSyncTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源类型名称
         */
        private String sourceTypeName;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 目标平台名称
         */
        private String targetPlatformName;

        /**
         * 来源系统名称
         */
        private String sourcePlatformName;

        /**
         * 同步类型
         */
        private String syncTypeName;

        /**
         * 同步状态
         */
        private String status;

        /**
         * 同步状态名称
         */
        private String statusName;

        /**
         * 创建日期
         */
        private LocalDateTime createTime;

        /**
         * 最新推送时间
         */
        private LocalDateTime lastSyncTime;

        /**
         * 推送结果（推送失败原因）
         */
        private String returnMsg;
    }


}
